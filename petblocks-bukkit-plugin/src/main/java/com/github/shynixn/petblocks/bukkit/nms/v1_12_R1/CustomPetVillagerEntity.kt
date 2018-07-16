package com.github.shynixn.petblocks.bukkit.nms.v1_12_R1

import com.github.shynixn.petblocks.api.business.service.PetActionService
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.bukkit.logic.business.helper.setFinalFieldAccessible
import com.google.common.collect.Sets
import net.minecraft.server.v1_12_R1.*
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * Created by Shynixn 2018.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2018 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class CustomPetVillagerEntity : EntityVillager {
    private var petActionService: PetActionService? = null
    private var petMeta: PetMeta? = null
    private var plugin: Plugin? = null

    /**
     * Default constructor for correct entity registration.
     */
    constructor(world: World) : super(world)

    /**
     * Default constructor for correct entity registration.
     */
    constructor(world: World, i: Int) : super(world, i)

    /**
     * Villager Constructor.
     */
    constructor(location: Location, owner: LivingEntity, petMeta: PetMeta, plugin: Plugin, petActionService: PetActionService) : super((location.world as CraftWorld).handle) {
        this.petMeta = petMeta
        this.isSilent = true
        this.petActionService = petActionService
        this.plugin = plugin

        val bField = PathfinderGoalSelector::class.java.getDeclaredField("b")
        val cField = PathfinderGoalSelector::class.java.getDeclaredField("c")
        this.setFinalFieldAccessible(bField)
        this.setFinalFieldAccessible(cField)
        cField.isAccessible = true
        bField.set(this.goalSelector, Sets.newLinkedHashSet<Any>())
        bField.set(this.targetSelector, Sets.newLinkedHashSet<Any>())
        cField.set(this.goalSelector, Sets.newLinkedHashSet<Any>())
        cField.set(this.targetSelector, Sets.newLinkedHashSet<Any>())

        this.goalSelector.a(0, PathfinderGoalFloat(this))
        this.goalSelector.a(1, CustomOwnerPathfinder(this, (owner as CraftLivingEntity).handle))
    }

    /**
     * Lets this entity respawn at the given [location].
     */
    fun respawn(location: Location) {
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).value = 0.30000001192092896 * petMeta!!.walkingSpeed
        this.P = petMeta!!.climbingHeight.toFloat()

        this.setPositionRotation(location.x, location.y, location.z, location.yaw, location.pitch)
        val world = (location.world as CraftWorld).handle
        world.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)

        val livingEntity = (getBukkitEntity() as LivingEntity)
        livingEntity.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 9999999, 1))
        livingEntity.setMetadata("keep", FixedMetadataValue(plugin, true))
        livingEntity.isCustomNameVisible = false
        livingEntity.customName = "PetBlockIdentifier"
    }

    /**
     * Override implementations.
     */
    override fun a(blockposition: BlockPosition, block: Block) {
        super.a(blockposition, block)

        petActionService!!.playSound(this.petMeta!!.movementSound)
    }

    /**
     * Override implementations.
     */
    override fun recalcPosition() {
        val axisAssignment = this.boundingBox
        val hitbox = petMeta!!.hitbox

        this.locX = (axisAssignment.a + axisAssignment.d) / 2.0 + hitbox.x
        this.locY = axisAssignment.b + hitbox.y
        this.locZ = (axisAssignment.c + axisAssignment.f) / 2.0 + hitbox.z
    }
}