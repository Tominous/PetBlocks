package com.github.shynixn.petblocks.core.logic.persistence.entity

import com.github.shynixn.petblocks.api.business.annotation.YamlSerialize
import com.github.shynixn.petblocks.api.persistence.entity.AIHealth

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
class AIHealthEntity : AIBaseEntity(), AIHealth {
    /**
     * Name of the type.
     */
    override var type: String = "health"
    /**
     * Max health of a ai item.
     */
    @YamlSerialize(value = "max-health", orderNumber = 1)
    override var maxHealth: Double = 20.0
    /**
     * Current health of a ai item.
     */
    @YamlSerialize(value = "health", orderNumber = 2)
    override var health: Double = 20.0

    /**
     * Amount of seconds until the pet can respawn after it has died.
     */
    @YamlSerialize(value = "respawn-delay", orderNumber = 3)
    override var respawningDelay: Int = 5

    /**
     * Current delay for respawning.
     */
    @YamlSerialize(value = "current-respawn-delay", orderNumber = 4)
    override var currentRespawningDelay: Int = 0
}