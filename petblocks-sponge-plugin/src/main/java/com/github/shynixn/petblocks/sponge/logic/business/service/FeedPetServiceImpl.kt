package com.github.shynixn.petblocks.sponge.logic.business.service

import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.business.service.FeedingPetService
import com.github.shynixn.petblocks.api.business.service.ParticleService
import com.github.shynixn.petblocks.api.business.service.SoundService
import com.github.shynixn.petblocks.api.persistence.entity.Particle
import com.github.shynixn.petblocks.api.persistence.entity.Sound
import com.github.shynixn.petblocks.sponge.logic.business.PetBlocksManager
import com.github.shynixn.petblocks.sponge.logic.business.helper.getItemStackInHand
import com.github.shynixn.petblocks.sponge.logic.business.helper.setItemStackInHand
import com.github.shynixn.petblocks.sponge.logic.business.helper.sync
import com.google.inject.Inject
import org.spongepowered.api.entity.Transform
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.ItemTypes
import org.spongepowered.api.plugin.PluginContainer
import java.util.*

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
class FeedPetServiceImpl @Inject constructor(private val plugin: PluginContainer, private val configurationService: ConfigurationService, private val soundService: SoundService, private val particleService: ParticleService) : FeedingPetService {
    private val jumpCache = HashSet<UUID>()

    /**
     * Feeds the pet of the given [player] with the current item in hand.
     */
    override fun <P> feedPet(player: P): Boolean {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        val feedingPetEnabled = configurationService.findValue<Boolean>("pet.feeding.enabled")

        if (!feedingPetEnabled) {
            return false
        }

        val pet = PetBlocksManager.petBlocksManager!!.petBlockController.getFromPlayer(player)
        val itemInHand = player.inventory.getItemStackInHand()

        if (!pet.isPresent) {
            return false
        }

        if (!itemInHand.isPresent || itemInHand.get().type != ItemTypes.CARROT) {
            return false
        }

        if (pet.get().meta.isSoundEnabled) {
            val feedingSound = configurationService.findValue<Sound>("pet.feeding.click-sound")
            this.soundService.playSound((pet.get().location as Transform<*>).location, feedingSound, player)
        }

        val feedingParticle = configurationService.findValue<Particle>("pet.feeding.click-particle")
        particleService.playParticle((pet.get().location as Transform<*>).location, feedingParticle, player)

        if (itemInHand.get().quantity == 1) {
            player.inventory.setItemStackInHand(null)
        } else {
            itemInHand.get().quantity = itemInHand.get().quantity - 1
            player.inventory.setItemStackInHand(itemInHand.get())
        }

        if (!jumpCache.contains(player.uniqueId)) {
            jumpCache.add(player.uniqueId)
            pet.get().jump()
            sync(plugin, 20L) {
                jumpCache.remove(player.uniqueId)
            }
        }

        return true
    }
}