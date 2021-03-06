package com.github.shynixn.petblocks.api.business.service

import com.github.shynixn.petblocks.api.business.proxy.PetProxy
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
interface PetService {
    /**
     * Gets or spawns the pet of the given player.
     * An empty optional gets returned if the pet cannot spawn by one of the following reasons:
     * Current world, region is disabled for pets, PreSpawnEvent was cancelled or Pet is not available due to Ai State.
     * For example HealthAI defines pet ai as 0 which results into impossibility to spawn.
     */
    fun <P> getOrSpawnPetFromPlayer(player: P): Optional<PetProxy>

    /**
     * Tries to find the pet from the given entity.
     * Returns null if the pet does not exist.
     */
    fun <E> findPetByEntity(entity: E): PetProxy?

    /**
     * Gets if the given [player] has got an active pet.
     */
    fun <P> hasPet(player: P): Boolean
}