package com.github.shynixn.petblocks.bukkit.nms.v1_13_R2;

import com.github.shynixn.petblocks.api.bukkit.event.PetBlockSpawnEvent;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.business.entity.PetBlockPartEntity;
import com.github.shynixn.petblocks.api.business.enumeration.RideType;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.logic.persistence.configuration.Config;
import com.github.shynixn.petblocks.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.bukkit.nms.helper.PetBlockHelper;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.logging.Level;

final class CustomGroundArmorstand extends EntityArmorStand implements PetBlock {
    private PetMeta petMeta;
    private Player owner;
    private PetBlockPartEntity rabbit;

    private boolean isSpecial;
    private boolean isGround;
    private boolean firstRide = true;
    private boolean isGroundRiding;
    private boolean hasHitFlor;
    private boolean isDieing;

    private int counter;
    private double health = 20.0;

    private Vector bumper;

    private static final Field[] axisAlignmentFields = new Field[5];

    static {
        try {
            axisAlignmentFields[0] = AxisAlignedBB.class.getDeclaredField("minX");
            axisAlignmentFields[1] = AxisAlignedBB.class.getDeclaredField("minY");
            axisAlignmentFields[2] = AxisAlignedBB.class.getDeclaredField("minZ");
            axisAlignmentFields[3] = AxisAlignedBB.class.getDeclaredField("maxX");
            axisAlignmentFields[4] = AxisAlignedBB.class.getDeclaredField("maxZ");
        } catch (final NoSuchFieldException ex) {
            try {
                axisAlignmentFields[0] = AxisAlignedBB.class.getDeclaredField("a");
                axisAlignmentFields[1] = AxisAlignedBB.class.getDeclaredField("b");
                axisAlignmentFields[2] = AxisAlignedBB.class.getDeclaredField("c");
                axisAlignmentFields[3] = AxisAlignedBB.class.getDeclaredField("d");
                axisAlignmentFields[4] = AxisAlignedBB.class.getDeclaredField("f");
            } catch (final NoSuchFieldException e1) {
                throw new RuntimeException("Fields could not get located.", e1);
            }
        }
    }

    public CustomGroundArmorstand(World world) {
        super(world);
    }

    public CustomGroundArmorstand(Location location, PetMeta meta) {
        super(((CraftWorld) location.getWorld()).getHandle());
        this.isSpecial = true;
        this.petMeta = meta;
        this.owner = this.petMeta.getPlayerMeta().getPlayer();
        if (this.petMeta.getEngine().getEntityType().equalsIgnoreCase("RABBIT"))
            this.rabbit = new CustomRabbit(this.owner, this);
        else if (this.petMeta.getEngine().getEntityType().equalsIgnoreCase("ZOMBIE"))
            this.rabbit = new CustomZombie(this.owner, this);
        else
            throw new RuntimeException("Cannot find engine!");

        this.spawn(location);
    }

    public CustomGroundArmorstand(World world, double d0, double d1, double d2) {
        super(world, d0, d1, d2);
    }

    private boolean isJumping() {
        final Field jump;
        try {
            jump = EntityLiving.class.getDeclaredField("bg");
            jump.setAccessible(true);
            return !this.passengers.isEmpty() && jump.getBoolean(this.passengers.get(0));
        } catch (final NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
            PetBlocksPlugin.logger().log(Level.WARNING, "EntityNMS exception.", e1);
        }
        return false;
    }

    private EntityHuman hasHumanPassenger() {
        if (this.passengers != null) {
            for (final Entity entity : this.passengers) {
                if (entity instanceof EntityHuman)
                    return (EntityHuman) entity;
            }
        }
        return null;
    }

    @Override
    public void move(EnumMoveType enummovetype, double d0, double d1, double d2) {
        super.move(enummovetype, d0, d1, d2);
        this.recalculatePosition();
    }

    private void recalculatePosition() {
        if (this.hasHumanPassenger() != null) {
            final AxisAlignedBB localAxisAlignedBB = this.getBoundingBox();

            try {
                final double minXA = axisAlignmentFields[0].getDouble(localAxisAlignedBB);
                final double minXB = axisAlignmentFields[1].getDouble(localAxisAlignedBB);
                final double minXC = axisAlignmentFields[2].getDouble(localAxisAlignedBB);
                final double maxXD = axisAlignmentFields[3].getDouble(localAxisAlignedBB);
                final double maxXF = axisAlignmentFields[4].getDouble(localAxisAlignedBB);

                this.locX = ((minXA + maxXD) / 2.0D);
                this.locZ = ((minXC + maxXF) / 2.0D);
                this.locY = (minXB + Config.INSTANCE.getHitBoxYAxeModification());
            } catch (final IllegalAccessException e1) {
                throw new RuntimeException(e1);
            }

            this.isGroundRiding = true;
        }
    }

    @Override
    protected void doTick() {
        if (this.isSpecial) {
            this.counter = PetBlockHelper.doTick(this.counter, this, location -> {
                CustomGroundArmorstand.this.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
                final PacketPlayOutEntityTeleport animation = new PacketPlayOutEntityTeleport(CustomGroundArmorstand.this);
                for (final Player player : ((ArmorStand) this.getArmorStand()).getWorld().getPlayers()) {
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(animation);
                }
            });
            if (this.isGroundRiding && this.hasHumanPassenger() == null) {
                ((org.bukkit.entity.Entity) this.getEngineEntity()).teleport(((org.bukkit.entity.Entity) this.getEngineEntity()).getLocation().add(0, 2, 0));
                this.isGroundRiding = false;
            }
        }
        super.doTick();
    }

    @Override
    public void a(float sideMot, float f2, float forMot) {
        if (this.isSpecial) {
            if (this.hasHumanPassenger() != null) {
                if (this.petMeta.getEngine().getRideType() == RideType.RUNNING) {
                    this.lastYaw = (this.yaw = this.hasHumanPassenger().yaw);
                    this.pitch = (this.hasHumanPassenger().pitch * 0.5F);
                    this.setYawPitch(this.yaw, this.pitch);
                    this.aS = (this.aQ = this.yaw);
                    sideMot = this.hasHumanPassenger().bh * 0.5F;
                    forMot = this.hasHumanPassenger().bj;
                    if (forMot <= 0.0F) {
                        forMot *= 0.25F;
                    }
                    if (this.onGround && this.isJumping()) {
                        this.motY = 0.5D;
                    }

                    this.Q = (float) Config.INSTANCE.getModifier_petclimbing();
                    this.aU = (this.cK() * 0.1F);

                    if (!this.world.isClientSide) {
                        this.o(0.35F);
                        super.a(sideMot * (float) Config.INSTANCE.getModifier_petriding(), f2, forMot * (float) Config.INSTANCE.getModifier_petriding());
                    }

                    this.aI = this.aJ;
                    final double d0 = this.locX - this.lastX;
                    final double d1 = this.locZ - this.lastZ;
                    float f4 = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F;

                    if (f4 > 1.0F) {
                        f4 = 1.0F;
                    }

                    this.aJ += (f4 - this.aJ) * 0.4F;
                    this.aK += this.aJ;
                } else {
                    final float side = this.hasHumanPassenger().bh * 0.5F;
                    final float forw = this.hasHumanPassenger().bj;
                    final Vector v = new Vector();
                    final Location l = new Location(this.world.getWorld(), this.locX, this.locY, this.locZ);
                    if (side < 0.0F) {
                        l.setYaw(this.hasHumanPassenger().yaw - 90);
                        v.add(l.getDirection().normalize().multiply(-0.5));
                    } else if (side > 0.0F) {
                        l.setYaw(this.hasHumanPassenger().yaw + 90);
                        v.add(l.getDirection().normalize().multiply(-0.5));
                    }

                    if (forw < 0.0F) {
                        l.setYaw(this.hasHumanPassenger().yaw);
                        v.add(l.getDirection().normalize().multiply(0.5));
                    } else if (forw > 0.0F) {
                        l.setYaw(this.hasHumanPassenger().yaw);
                        v.add(l.getDirection().normalize().multiply(0.5));
                    }

                    this.lastYaw = this.yaw = this.hasHumanPassenger().yaw - 180;
                    this.pitch = this.hasHumanPassenger().pitch * 0.5F;
                    this.lastYaw = (this.yaw = this.hasHumanPassenger().yaw);
                    this.setYawPitch(this.yaw, this.pitch);
                    if (this.firstRide) {
                        this.firstRide = false;
                        v.setY(1F);
                    }
                    if (this.isJumping()) {
                        v.setY(0.5F);
                        this.isGround = true;
                        this.hasHitFlor = false;
                    } else if (this.isGround) {
                        v.setY(-0.2F);
                    }
                    if (this.hasHitFlor) {
                        v.setY(0);
                        l.add(v.multiply(2.25).multiply(Config.INSTANCE.getModifier_petriding()));
                        this.setPosition(l.getX(), l.getY(), l.getZ());
                    } else {
                        l.add(v.multiply(2.25).multiply(Config.INSTANCE.getModifier_petriding()));
                        this.setPosition(l.getX(), l.getY(), l.getZ());
                    }
                    final Vec3D vec3d = new Vec3D(this.locX, this.locY, this.locZ);
                    final Vec3D vec3d1 = new Vec3D(this.locX + this.motX, this.locY + this.motY, this.locZ + this.motZ);
                    final MovingObjectPosition movingobjectposition = this.world.rayTrace(vec3d, vec3d1);
                    if (movingobjectposition == null) {
                        this.bumper = l.toVector();
                    } else {
                        if (this.bumper != null && Config.INSTANCE.isFollow_wallcolliding())
                            this.setPosition(this.bumper.getX(), this.bumper.getY(), this.bumper.getZ());
                    }
                }
            } else
                this.firstRide = true;
        } else {
            super.a(sideMot, f2, forMot);
        }
    }

    public void spawn(Location location) {
        final PetBlockSpawnEvent event = new PetBlockSpawnEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCanceled()) {
            NMSRegistry.accessWorldGuardSpawn(location);
            this.rabbit.spawn(location);
            final World mcWorld = ((CraftWorld) location.getWorld()).getHandle();
            this.setPosition(location.getX(), location.getY(), location.getZ());
            mcWorld.addEntity(this, SpawnReason.CUSTOM);
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setBoolean("invulnerable", true);
            compound.setBoolean("Invisible", true);
            compound.setBoolean("PersistenceRequired", true);
            compound.setBoolean("ShowArms", true);
            compound.setBoolean("NoBasePlate", true);
            this.a(compound);
            ((ArmorStand) this.getArmorStand()).setBodyPose(new EulerAngle(0, 0, 2878));
            ((ArmorStand) this.getArmorStand()).setLeftArmPose(new EulerAngle(2878, 0, 0));
            ((ArmorStand) this.getArmorStand()).setMetadata("keep", this.getKeepField());
            NMSRegistry.rollbackWorldGuardSpawn(location);
            ((ArmorStand) this.getArmorStand()).setCustomNameVisible(true);
            ((ArmorStand) this.getArmorStand()).setCustomName(this.petMeta.getPetDisplayName());
            ((ArmorStand) this.getArmorStand()).setRemoveWhenFarAway(false);
            ((LivingEntity) this.getEngineEntity()).setRemoveWhenFarAway(false);
            this.health = Config.INSTANCE.getCombat_health();
            if (this.petMeta == null)
                return;
            PetBlockHelper.setItemConsideringAge(this);
        }
    }

    @Override
    public void teleportWithOwner(Object mLocation) {
        final Location location = (Location) mLocation;
        final EntityPlayer player = ((CraftPlayer) this.owner).getHandle();
        player.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        final PacketPlayOutEntityTeleport teleport = new PacketPlayOutEntityTeleport(player);
        for (final Player player1 : this.owner.getWorld().getPlayers()) {
            ((CraftPlayer) player1).getHandle().playerConnection.sendPacket(teleport);
        }
    }

    @Override
    public void damage(double amount) {
        if (amount < -1.0) {
            this.hasHitFlor = true;
        } else {
            this.health = PetBlockHelper.setDamage(this, this.health, amount, location -> {
                final PacketPlayOutAnimation animation = new PacketPlayOutAnimation(CustomGroundArmorstand.this, 1);
                for (final Player player : ((ArmorStand) this.getArmorStand()).getWorld().getPlayers()) {
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(animation);
                }
            });
        }
    }

    @Override
    public void respawn() {
        PetBlockHelper.respawn(this, CustomGroundArmorstand.this::spawn);
    }

    @Override
    public void setDieing() {
        this.isDieing = PetBlockHelper.setDieing(this);
    }

    @Override
    public boolean isDieing() {
        return this.isDieing;
    }

    @Override
    public void setSkin(String skin) {
        PetBlockHelper.setSkin(this, skin);
    }

    @Override
    public void setSkin(Object material, byte data) {
        PetBlockHelper.setSkin(this, (org.bukkit.Material) material, data);
    }

    /**
     * Lets the petblock perform a jump
     */
    @Override
    public void jump() {
        PetBlockHelper.jump(this);
    }

    /**
     * Returns if the petblock is already removed or dead
     *
     * @return dead
     */
    @Override
    public boolean isDead() {
        return PetBlockHelper.isDead(this);
    }

    /**
     * Lets the given player ride on the petblock
     *
     * @param player player
     */
    @Override
    public void ride(Object player) {
        PetBlockHelper.setRiding(this, (Player) player);
    }

    /**
     * Lets the given player wear the petblock
     *
     * @param player oplayer
     */
    @Override
    public void wear(Object player) {
        if (this.getBukkitEntity().getPassenger() == null && ((Player) player).getPassenger() == null) {
            final NBTTagCompound compound = new NBTTagCompound();
            this.b(compound);
            compound.setBoolean("Marker", true);
            this.a(compound);
            this.setCustomNameVisible(false);

            if (this.rabbit != null) {
                ((EntityInsentient) this.rabbit).setNoAI(true);
            }

            PetBlockHelper.wear(this, (Player) player, null);
        }
    }

    /**
     * Ejects the given player riding from the petblock
     *
     * @param player player
     */
    @Override
    public void eject(Object player) {
        final NBTTagCompound compound = new NBTTagCompound();
        this.b(compound);
        compound.setBoolean("Marker", false);
        this.a(compound);
        this.setCustomNameVisible(true);

        if (this.rabbit != null) {
            ((EntityInsentient) this.rabbit).setNoAI(false);
        }

        PetBlockHelper.eject(this, (Player) player, null);
    }

    /**
     * Sets the displayName of the petblock
     *
     * @param name name
     */
    @Override
    public void setDisplayName(String name) {
        PetBlockHelper.setDisplayName(this, name);
    }

    /**
     * Returns the displayName of the petblock.
     *
     * @return name
     */
    @Override
    public String getEntityDisplayName() {
        return ((ArmorStand) this.getArmorStand()).getCustomName();
    }

    /**
     * Returns the armorstand of the petblock
     *
     * @return armorstand
     */
    @Override
    public Object getArmorStand() {
        return this.getBukkitEntity();
    }

    /**
     * Sets the velocity of the petblock
     *
     * @param vector vector
     */
    @Override
    public void setVelocity(Object vector) {
        PetBlockHelper.launch(this, (Vector) vector);
    }

    /**
     * Teleports the the petblock to the given location
     *
     * @param location location
     */
    @Override
    public void teleport(Object location) {
        PetBlockHelper.teleport(this, (Location) location);
    }

    /**
     * Returns the meta of the petblock
     *
     * @return meta
     */
    @Override
    public PetMeta getMeta() {
        return this.petMeta;
    }

    /**
     * Returns the owner of the petblock
     *
     * @return player
     */
    @Override
    public Object getPlayer() {
        return this.owner;
    }

    /**
     * Removes the petblock
     */
    @Override
    public void remove() {
        PetBlockHelper.remove(this);
    }

    /**
     * Returns the entity being used as engine
     *
     * @return entity
     */
    @Override
    public Object getEngineEntity() {
        if (this.rabbit == null)
            return null;
        return this.rabbit.getEntity();
    }

    /**
     * Returns the location of the entity
     *
     * @return position
     */
    @Override
    public Object getLocation() {
        return ((ArmorStand) this.getArmorStand()).getLocation();
    }

    /**
     * Returns the fixedMetaDataValue
     *
     * @return value
     */
    private FixedMetadataValue getKeepField() {
        return new FixedMetadataValue(Bukkit.getPluginManager().getPlugin("PetBlocks"), true);
    }
}
