package com.github.shynixn.petblocks.bukkit.nms.v1_8_R3;

import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.business.entity.PetBlockPartEntity;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.logic.persistence.configuration.Config;
import com.github.shynixn.petblocks.bukkit.nms.helper.PetBlockHelper;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.logging.Level;

public final class CustomRabbit extends EntityRabbit implements PetBlockPartEntity {
    private PetBlock petBlock;

    private long playedMovingSound = 100000;

    public CustomRabbit(World world) {
        super(world);
    }

    public CustomRabbit(Player player,PetBlock petBlock) {
        super(((CraftWorld) player.getWorld()).getHandle());
        this.b(true);
        try {
            final Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
            bField.setAccessible(true);
            final Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
            cField.setAccessible(true);
            bField.set(this.goalSelector, new UnsafeList<PathfinderGoalSelector>());
            bField.set(this.targetSelector, new UnsafeList<PathfinderGoalSelector>());
            cField.set(this.goalSelector, new UnsafeList<PathfinderGoalSelector>());
            cField.set(this.targetSelector, new UnsafeList<PathfinderGoalSelector>());
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.30000001192092896D * Config.INSTANCE.getModifier_petwalking());

            this.goalSelector.a(0, new PathfinderGoalFloat(this));
            this.goalSelector.a(1, new OwnerPathfinder(this,petBlock));
        } catch (final Exception exc) {
            PetBlocksPlugin.logger().log(Level.WARNING, "EntityNMS exception.", exc);
        }
        this.petBlock = petBlock;
        this.S = (float) Config.INSTANCE.getModifier_petclimbing();
    }

    @Override
    protected String cm() {
        try {
            if (this.petBlock == null) {
                return super.cm();
            }

            this.playedMovingSound = PetBlockHelper.executeMovingSound(this.petBlock, this.playedMovingSound);
        } catch (final Exception ex) {
            this.remove();
            PetBlocksPlugin.logger().log(Level.WARNING, "Detected invalid rabbit entity. Removed entity.",ex);
        }
        return "mob.rabbit.hop";
    }

    /**
     * Returns the entity hidden by this object
     *
     * @return spigotEntity
     */
    @Override
    public Object getEntity() {
        return this.getBukkitEntity();
    }

    /**
     * Spawns the entity at the given location
     *
     * @param mLocation location
     */
    @Override
    public void spawn(Object mLocation) {
        final Location location = (Location) mLocation;
        final LivingEntity entity = (LivingEntity) this.getEntity();
        final net.minecraft.server.v1_8_R3.World mcWorld = ((CraftWorld) location.getWorld()).getHandle();
        this.setPosition(location.getX(), location.getY(), location.getZ());
        mcWorld.addEntity(this, SpawnReason.CUSTOM);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 9999999, 1));
        entity.setMetadata("keep", this.getKeepField());
        entity.setCustomNameVisible(false);
        entity.setCustomName("PetBlockIdentifier");
    }

    /**
     * Removes the entity from the world
     */
    @Override
    public void remove() {
        ((LivingEntity) this.getEntity()).remove();
    }

    /**
     * Returns the keepField
     *
     * @return keepField
     */
    private FixedMetadataValue getKeepField() {
        return new FixedMetadataValue(Bukkit.getPluginManager().getPlugin("PetBlocks"), true);
    }
}
