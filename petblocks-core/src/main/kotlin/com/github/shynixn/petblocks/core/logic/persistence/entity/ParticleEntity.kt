package com.github.shynixn.petblocks.core.logic.persistence.entity

import com.github.shynixn.petblocks.api.business.annotation.YamlSerialize
import com.github.shynixn.petblocks.api.business.enumeration.ParticleColor
import com.github.shynixn.petblocks.api.business.enumeration.ParticleType
import com.github.shynixn.petblocks.api.persistence.entity.Particle
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
class ParticleEntity(
    @YamlSerialize(value = "name", orderNumber = 1, customserializer = ParticleTypeSerializer::class)
    override var type: ParticleType = ParticleType.NONE) : Particle {
    /**
     * Database Id.
     */
    override var id: Long = 0
    /**
     * Amount of particles.
     */
    @YamlSerialize(value = "amount", orderNumber = 2)
    override var amount: Int = 1
    /**
     * Particle speed.
     */
    @YamlSerialize(value = "speed", orderNumber = 3)
    override var speed: Double = 1.0
    /**
     * Offset for the x coordinate.
     */
    @YamlSerialize(value = "offx", orderNumber = 4)
    override var offSetX: Double = 1.0
    /**
     * Offset for the y coordinate.
     */
    @YamlSerialize(value = "offy", orderNumber = 5)
    override var offSetY: Double = 1.0
    /**
     * Offset for the z coordinate.
     */
    @YamlSerialize(value = "offz", orderNumber = 6)
    override var offSetZ: Double = 1.0

    /**
     * Material value.
     */
    @YamlSerialize(value = "material", orderNumber = 7)
    override var materialName: String? = null
    /**
     * Data value.
     */
    @YamlSerialize(value = "data", orderNumber = 8)
    override var data: Int = 0

    /**
     * Custom note color code.
     */
    override var noteColor: Int
        get() = this.offSetX.toInt()
        set(value) {
            this.offSetX = value.toDouble()
        }

    /**
     * Color of the particle effect.
     */
    override var color: ParticleColor
        get() = ParticleColor.WHITE
        set(value) {
            colorRed = value.red
            colorGreen = value.green
            colorBlue = value.blue
        }

    /**
     * RGB Color code of red.
     */
    override var colorRed: Int
        get() = this.offSetX.toInt()
        set(value) {
            this.offSetX = value.toDouble()
        }

    /**
     * RGB Color code of green.
     */
    override var colorGreen: Int
        get() = this.offSetY.toInt()
        set(value) {
            this.offSetY = value.toDouble()
        }
    /**
     * RGB Color code of blue.
     */
    override var colorBlue: Int
        get() = this.offSetZ.toInt()
        set(value) {
            this.offSetZ = value.toDouble()
        }
}