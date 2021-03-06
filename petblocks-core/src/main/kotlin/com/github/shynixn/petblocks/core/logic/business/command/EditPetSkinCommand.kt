@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.core.logic.business.command

import com.github.shynixn.petblocks.api.business.command.SourceCommand
import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.business.service.*
import com.google.inject.Inject

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
class EditPetSkinCommand @Inject constructor(
    private val proxyService: ProxyService,
    private val petMetaService: PersistencePetMetaService,
    private val configurationService: ConfigurationService,
    private val commandService: CommandService,
    private val messageService: MessageService
) : SourceCommand {
    /**
     * Gets called when the given [source] executes the defined command with the given [args].
     */
    override fun <S> onExecuteCommand(source: S, args: Array<out String>): Boolean {
        if (args.size < 2 || !args[0].equals("skin", true)) {
            return false
        }

        val result = commandService.parseCommand<Any?>(source as Any, args, 2)

        if (result.first == null) {
            return false
        }

        val playerProxy = proxyService.findPlayerProxyObject(result.first)
        val petMeta = petMetaService.getPetMetaFromPlayer(playerProxy)

        try {
            val configuration = configurationService.findValue<Map<String, Any>>(args[1])

            this.setItem<Int>("id", configuration) { value -> petMeta.skin.typeName = value.toString() }
            this.setItem<Int>("damage", configuration) { value -> petMeta.skin.dataValue = value }
            this.setItem<Boolean>("unbreakable", configuration) { value -> petMeta.skin.unbreakable = value }
            this.setItem<String>("skin", configuration) { value -> petMeta.skin.owner = value }

            messageService.sendSourceMessage(source, "Changed the skin of the pet of player ${playerProxy.name}.")
        } catch (e: Exception) {
            messageService.sendSourceMessage(source, ChatColor.RED.toString() + e.message)
        }

        return true
    }

    /**
     * Sets optional gui items to a instance.
     */
    private fun <T> setItem(key: String, map: Map<String, Any?>?, f: (T) -> Unit) {
        if (map != null && map.containsKey(key)) {
            f.invoke(map[key]!! as T)
        }
    }
}