package com.github.shynixn.petblocks.core.logic.persistence.entity

import com.github.shynixn.petblocks.api.business.annotation.YamlSerialize
import com.github.shynixn.petblocks.api.business.enumeration.ParticleType
import com.github.shynixn.petblocks.api.business.service.ParticleService
import com.github.shynixn.petblocks.api.persistence.entity.AIMovement
import com.github.shynixn.petblocks.api.persistence.entity.Particle
import com.github.shynixn.petblocks.api.persistence.entity.Sound
import com.github.shynixn.petblocks.core.logic.business.serializer.ParticleTypeSerializer

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
abstract class AIMovementEntity : AIBaseEntity(), AIMovement {
    /**
     * Movement sound.
     */
    @YamlSerialize(value = "sound", orderNumber = 5, implementation = SoundEntity::class)
    override val movementSound: Sound = SoundEntity("CHICKEN_WALK")
    /**
     * Movement particle.
     */
    @YamlSerialize(value = "particle", orderNumber = 4, implementation = ParticleEntity::class)
    override val movementParticle: Particle = ParticleEntity()

    /**
     * Climbing height.
     */
    @YamlSerialize(value = "climbing-height", orderNumber = 1)
    override var climbingHeight: Double = 1.0
    /**
     * Movement speed modifier.
     */
    @YamlSerialize(value = "speed", orderNumber = 2)
    override var movementSpeed: Double = 1.0
    /**
     * Movement offset from ground.
     */
    @YamlSerialize(value = "offset-y", orderNumber = 3)
    override var movementYOffSet: Double = 1.0
}