@file:Suppress("UNCHECKED_CAST", "RemoveExplicitTypeArguments")

package com.github.shynixn.petblocks.sponge.logic.business.service

import com.github.shynixn.petblocks.api.business.proxy.PlayerProxy
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.sponge.logic.business.proxy.PlayerProxyImpl
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.Player
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
class ProxyServiceImpl : ProxyService {
    private val playerCache = HashMap<Player, PlayerProxy>()

    /**
     * Gets if the given instance can be converted to a player.
     */
    override fun <P> isPlayer(instance: P): Boolean {
        return instance is Player
    }

    /**
     * Returns a player proxy object for the given instance.
     * Throws a [IllegalArgumentException] if the proxy could not be generated.
     */
    override fun <P> findPlayerProxyObject(instance: P): com.github.shynixn.petblocks.api.business.proxy.PlayerProxy {
        val mInstance = (if (instance is PlayerProxy) {
            instance.handle
        } else {
            instance
        }) as? Player ?: throw IllegalArgumentException("Instance has to be a SpongePlayer!")

        if (!playerCache.containsKey(mInstance)) {
            playerCache[mInstance] = PlayerProxyImpl(mInstance)
        }

        return playerCache[mInstance]!!
    }

    /**
     * Tries to return a player proxy for the given player uuid.
     */
    override fun findPlayerProxyObjectFromUUID(uuid: String): PlayerProxy {
        val player = Sponge.getServer().getPlayer(UUID.fromString(uuid))

        if (player.isPresent && player.get().isOnline) {
            return findPlayerProxyObject(player.get())
        }

        throw IllegalArgumentException("Player is no longer online!")
    }

    /**
     * Clears any resources the given instance has allocated.
     */
    override fun cleanResources(instance: Any) {
        if (instance is Player) {
            if (playerCache.containsKey(instance)) {
                playerCache.remove(instance)
            }

            return
        }

        throw IllegalArgumentException("Instance $instance is not supported!")
    }
}