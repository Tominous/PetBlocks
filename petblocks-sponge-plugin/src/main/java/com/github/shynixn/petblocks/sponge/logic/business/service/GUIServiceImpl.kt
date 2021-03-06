@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.sponge.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.ChatClickAction
import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.business.enumeration.ScriptAction
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.GuiIcon
import com.github.shynixn.petblocks.api.persistence.entity.GuiPlayerCache
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.core.logic.business.extension.chatMessage
import com.github.shynixn.petblocks.core.logic.business.extension.sync
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
import com.github.shynixn.petblocks.core.logic.persistence.entity.GuiIconEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.GuiPlayerCacheEntity
import com.github.shynixn.petblocks.sponge.logic.business.extension.*
import com.google.inject.Inject
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.ItemTypes
import org.spongepowered.api.item.inventory.Inventory
import org.spongepowered.api.item.inventory.InventoryArchetypes
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.item.inventory.property.InventoryTitle
import org.spongepowered.api.item.inventory.type.CarriedInventory
import org.spongepowered.api.item.inventory.type.GridInventory
import org.spongepowered.api.plugin.PluginContainer
import java.util.*
import kotlin.collections.HashMap

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
class GUIServiceImpl @Inject constructor(
    private val configurationService: ConfigurationService,
    private val petActionService: PetActionService,
    private val scriptService: GUIScriptService,
    private val persistenceService: PersistencePetMetaService,
    private val itemService: ItemService,
    private val plugin: PluginContainer,
    private val concurrencyService: ConcurrencyService,
    private val loggingService: LoggingService,
    private val messageService: MessageService
) : GUIService {

    private val clickProtection = ArrayList<Player>()
    private val pageCache = HashMap<Player, GuiPlayerCache>()

    private var collectedMinecraftHeadsMessage = chatMessage {
        text {
            configurationService.findValue<String>("messages.prefix") + "Pets collected by "
        }
        component {
            color(ChatColor.YELLOW) {
                text {
                    ">>Minecraft-Heads.com<<"
                }
            }
            clickAction {
                ChatClickAction.OPEN_URL to "http://minecraft-heads.com"
            }
            hover {
                text {
                    "Goto the Minecraft-Heads website!"
                }
            }
        }
    }

    private var suggestHeadMessage = chatMessage {
        text {
            configurationService.findValue<String>("messages.prefix") + "Click here: "
        }
        component {
            color(com.github.shynixn.petblocks.api.business.enumeration.ChatColor.YELLOW) {
                text {
                    ">>Submit skin<<"
                }
            }
            clickAction {
                ChatClickAction.OPEN_URL to "http://minecraft-heads.com/custom/heads-generator"
            }
            hover {
                text {
                    "Goto the Minecraft-Heads website!"
                }
            }
        }
        text { " " }
        component {
            color(com.github.shynixn.petblocks.api.business.enumeration.ChatColor.YELLOW) {
                text {
                    ">>Suggest new pet<<"
                }
            }
            clickAction {
                ChatClickAction.OPEN_URL to "http://minecraft-heads.com/forum/suggesthead"
            }
            hover {
                text {
                    "Goto the Minecraft-Heads website!"
                }
            }
        }
    }

    private var skullNamingMessage = chatMessage {
        text {
            configurationService.findValue<String>("messages.prefix") + configurationService.findValue("messages.customhead-suggest-prefix")
        }
        component {
            text {
                configurationService.findValue("messages.customhead-suggest-clickable")
            }
            clickAction {
                ChatClickAction.SUGGEST_COMMAND to "/" + configurationService.findValue("commands.petblock.command") + " skin "
            }
            hover {
                text {
                    configurationService.findValue("messages.customhead-suggest-hover")
                }
            }
        }
        text {
            configurationService.findValue("messages.customhead-suggest-suffix")
        }
    }

    private var namingMessage = chatMessage {
        text {
            configurationService.findValue<String>("messages.prefix") + configurationService.findValue("messages.rename-suggest-prefix")
        }
        component {
            text {
                configurationService.findValue("messages.rename-suggest-clickable")
            }
            clickAction {
                ChatClickAction.SUGGEST_COMMAND to "/" + configurationService.findValue("commands.petblock.command") + " rename "
            }
            hover {
                text {
                    configurationService.findValue("messages.rename-suggest-hover")
                }
            }
        }
        text {
            configurationService.findValue("messages.rename-suggest-suffix")
        }
    }

    /**
     * Closes the gui for the given [player]. Does nothing when the GUI is already closed.
     */
    override fun <P> close(player: P) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        player.closeInventory()
    }

    /**
     * Clears all resources the given player has allocated from this service.
     */
    override fun <P> cleanResources(player: P) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        if (pageCache.containsKey(player)) {
            pageCache.remove(player)
        }
    }

    /**
     * Returns if the given [inventory] matches the inventory of this service.
     */
    override fun <I> isGUIInventory(inventory: I): Boolean {
        if (inventory !is CarriedInventory<*>) {
            throw IllegalArgumentException("Inventory has to be a SpongeInventory!")
        }

        if (!inventory.carrier.isPresent) {
            return false
        }

        val player = inventory.carrier.get()

        return this.pageCache.containsKey(player)
    }

    /**
     * Opens the gui for the given [player]. Does nothing when the GUI is already open.
     */
    override fun <P> open(player: P, pageName: String?) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        if (player.openInventory.isPresent) {
            player.closeInventory()
        }

        var page = pageName

        if (page == null) {
            page = "gui.main"
        }

        val guiTitle = configurationService.findValue<String>("messages.gui-title")
        val inventory =
            Inventory.builder().of(InventoryArchetypes.DOUBLE_CHEST).withCarrier(player).property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(guiTitle.toText())).build(plugin)
        player.openInventory(inventory)

        val petMeta = persistenceService.getPetMetaFromPlayer(player)
        pageCache[player] = GuiPlayerCacheEntity(page, inventory)
        renderPage(player, petMeta, page)
    }

    /**
     * Executes actions when the given [player] clicks on an [item] at the given [relativeSlot].
     */
    override fun <P, I> clickInventoryItem(player: P, relativeSlot: Int, item: I) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        if (item !is ItemStack) {
            throw IllegalArgumentException("Item has to be a SpongeItemStack!")
        }

        val optGuiItem = configurationService.findClickedGUIItem(pageCache[player]!!.path, item) ?: return

        if (optGuiItem.permission.isNotEmpty() && !player.hasPermission(optGuiItem.permission)) {
            if (!lockGui(player)) {
                player.sendMessage(configurationService.findValue<String>("messages.prefix") + configurationService.findValue<String>("messages.no-permission"))
            }

            return
        }

        val petMeta = persistenceService.getPetMetaFromPlayer(player)

        if (optGuiItem.blockedCondition != null && isConditionMatching(petMeta, optGuiItem.blockedCondition!!)) {
            return
        }

        if (pageCache.containsKey(player)) {
            var pageCache = pageCache[player]!!

            if (optGuiItem.targetSkin != null) {
                val skin = optGuiItem.targetSkin!!

                if (skin.sponsored) {
                    sendSponsoringMessage(pageCache, player)
                }

                petMeta.skin.typeName = skin.typeName
                petMeta.skin.dataValue = skin.dataValue
                petMeta.skin.owner = skin.owner
                petMeta.skin.unbreakable = skin.unbreakable

                while (pageCache.parent != null) {
                    pageCache = pageCache.parent!!
                }

                this.pageCache[player] = pageCache
            }

            if (optGuiItem.icon.skin.sponsored) {
                sendSponsoringMessage(pageCache, player)
            }

            for (aiBase in optGuiItem.removeAIs.toTypedArray()) {
                petMeta.aiGoals.removeIf { a -> a.type == aiBase.type }
            }

            for (aiBase in optGuiItem.addAIs.toTypedArray()) {
                petMeta.aiGoals.add(aiBase)
            }

            sync(concurrencyService, 1L) {
                renderPage(player, petMeta, this.pageCache[player]!!.path)
            }
        }

        if (optGuiItem.script != null) {
            sync(concurrencyService, 1L) {
                val scriptResult = scriptService.executeScript(optGuiItem.script!!)

                if (scriptResult.action == ScriptAction.SCROLL_PAGE) {
                    val result = scriptResult.valueContainer as Pair<Int, Int>
                    pageCache[player]!!.offsetX += result.first
                    pageCache[player]!!.offsetY += result.second

                    renderPage(player, petMeta, pageCache[player]!!.path)
                } else if (scriptResult.action == ScriptAction.PRINT_CUSTOM_SKIN_MESSAGE) {
                    messageService.sendPlayerMessage(player, skullNamingMessage)
                    this.close(player)
                } else if (scriptResult.action == ScriptAction.PRINT_SUGGEST_HEAD_MESSAGE) {
                    messageService.sendPlayerMessage(player, suggestHeadMessage)
                    this.close(player)
                } else if (scriptResult.action == ScriptAction.PRINT_CUSTOM_NAME_MESSAGE) {
                    messageService.sendPlayerMessage(player, namingMessage)
                    this.close(player)
                } else if (scriptResult.action == ScriptAction.DISABLE_PET) {
                    petActionService.disablePet(player)
                    this.close(player)
                } else if (scriptResult.action == ScriptAction.CALL_PET) {
                    petActionService.callPet(player)
                    this.close(player)
                } else if (scriptResult.action == ScriptAction.LAUNCH_CANNON) {
                    petActionService.launchPet(player)
                    this.close(player)
                } else if (scriptResult.action == ScriptAction.ENABLE_SOUND) {
                    petMeta.soundEnabled = true
                    renderPage(player, petMeta, pageCache[player]!!.path)
                } else if (scriptResult.action == ScriptAction.DISABLE_SOUND) {
                    petMeta.soundEnabled = false
                    renderPage(player, petMeta, pageCache[player]!!.path)
                } else if (scriptResult.action == ScriptAction.ENABLE_PARTICLES) {
                    petMeta.particleEnabled = true
                    renderPage(player, petMeta, pageCache[player]!!.path)
                } else if (scriptResult.action == ScriptAction.DISABLE_PARTICLES) {
                    petMeta.particleEnabled = false
                    renderPage(player, petMeta, pageCache[player]!!.path)
                } else if (scriptResult.action == ScriptAction.CLOSE_GUI) {
                    val page = pageCache[player]!!

                    if (page.parent == null) {
                        this.close(player)
                    } else {
                        pageCache[player] = page.parent!!
                        renderPage(player, petMeta, pageCache[player]!!.path)
                    }
                } else if (scriptResult.action == ScriptAction.OPEN_PAGE) {
                    val parent = pageCache[player]!!
                    pageCache[player] = GuiPlayerCacheEntity(scriptResult.valueContainer as String, parent.getInventory(), parent.advertisingMessageTime)
                    pageCache[player]!!.parent = parent
                    renderPage(player, petMeta, scriptResult.valueContainer as String)
                }
            }
        }
    }

    /**
     * Renders a single gui page.
     */
    private fun renderPage(player: Player, petMeta: PetMeta, path: String) {
        val inventory = this.pageCache[player]!!.getInventory<CarriedInventory<Player>>()
        inventory.clear()

        val items = configurationService.findGUIItemCollection(path)

        if (items == null) {
            loggingService.warn("Failed to load gui path '$path'.")
            return
        }

        for (item in items) {
            if (item.hidden) {
                continue
            }

            if (item.hiddenCondition != null && isConditionMatching(petMeta, item.hiddenCondition!!)) {
                continue
            }

            var hasPermission = true

            if (item.permission.isNotEmpty()) {
                hasPermission = player.hasPermission(item.permission)

                if (!hasPermission && item.hiddenCondition != null && item.hiddenCondition!!.contains("no-permission")) {
                    continue
                }
            }

            val position = if (item.fixed) {
                item.position
            } else {
                scrollCollection(player, item.position)
            }

            if (position < 0 || position > 53) {
                continue
            }

            if (item.icon.script != null) {
                val scriptResult = scriptService.executeScript(item.icon.script!!)

                if (scriptResult.action == ScriptAction.COPY_PET_SKIN) {
                    val guiIcon = GuiIconEntity()
                    guiIcon.displayName = petMeta.displayName

                    with(guiIcon.skin) {
                        typeName = petMeta.skin.typeName
                        dataValue = petMeta.skin.dataValue
                        owner = petMeta.skin.owner
                        unbreakable = petMeta.skin.unbreakable
                    }

                    renderIcon(inventory, position, guiIcon, hasPermission)
                } else if (scriptResult.action == ScriptAction.HIDE_RIGHT_SCROLL && item.script != null) {
                    val itemPreScriptResult = scriptService.executeScript(item.script!!)
                    val offsetData = itemPreScriptResult.valueContainer as Pair<Int, Int>

                    val cachedData = Pair(pageCache[player]!!.offsetX, pageCache[player]!!.offsetY)
                    pageCache[player]!!.offsetX += offsetData.first

                    var found = false

                    if (offsetData.first > 0) {
                        for (s in items) {
                            if (s.hidden) {
                                continue
                            }

                            val pos = scrollCollection(player, s.position)

                            if (pos in 0..53) {
                                found = true
                            }
                        }
                    }

                    pageCache[player]!!.offsetX = cachedData.first
                    pageCache[player]!!.offsetY = cachedData.second

                    if (found) {
                        renderIcon(inventory, position, item.icon, hasPermission)
                    }
                } else if (scriptResult.action == ScriptAction.HIDE_LEFT_SCROLL && item.script != null) {
                    val itemPreScriptResult = scriptService.executeScript(item.script!!)
                    val offsetData = itemPreScriptResult.valueContainer as Pair<Int, Int>

                    if (offsetData.first < 0) {
                        if (pageCache[player]!!.offsetX > 0) {
                            renderIcon(inventory, position, item.icon, hasPermission)
                        }

                        continue
                    }
                }
            } else {
                renderIcon(inventory, position, item.icon, hasPermission)
            }
        }

        fillEmptySlots(inventory)

        player.inventory.updateInventory()
    }

    /**
     * Renders a gui Icon.
     */
    private fun renderIcon(inventory: CarriedInventory<Player>, position: Int, guiIcon: GuiIcon, hasPermission: Boolean) {
        if (position < 0) {
            return
        }

        val itemStack = createItemStackIcon(guiIcon, hasPermission)

        try {
            inventory.setItem(position, itemStack)
        } catch (e: Exception) {
            // Inventory might not be available.
        }
    }

    /**
     * Creates a new rendered item stack.
     */
    private fun createItemStackIcon(guiIcon: GuiIcon, hasPermission: Boolean): ItemStack {
        val permissionMessage = if (hasPermission) {
            configurationService.findValue("messages.has-permission")
        } else {
            configurationService.findValue<String>("messages.has-no-permission")
        }

        var itemStack = itemService.createItemStack(guiIcon.skin.typeName, guiIcon.skin.dataValue).build<ItemStack>()

        itemStack.displayName = guiIcon.displayName
        itemStack.skin = guiIcon.skin.owner

        if (guiIcon.skin.unbreakable) {
            itemStack = itemStack.createUnbreakableCopy()
        }

        val tmpLore = ArrayList<String>()

        guiIcon.lore.forEach { l ->
            tmpLore.add(l.replace("<permission>", permissionMessage).translateChatColors())
        }

        itemStack.lore = tmpLore

        return itemStack
    }

    /**
     * Checks if the condition holds or not.
     */
    private fun isConditionMatching(petMeta: PetMeta, names: Array<String>): Boolean {
        for (name in names) {
            for (aiGoal in petMeta.aiGoals) {
                if (aiGoal.type.equals(name, true)) {
                    return true
                }
            }

            if (petMeta.enabled && name.equals("pet-enabled", true)) {
                return true
            }

            if (!petMeta.enabled && name.equals("pet-disabled", true)) {
                return true
            }

            if (petMeta.soundEnabled && name.equals("sound-enabled", true)) {
                return true
            }

            if (!petMeta.soundEnabled && name.equals("sound-disabled", true)) {
                return true
            }

            if (petMeta.particleEnabled && name.equals("particle-enabled", true)) {
                return true
            }

            if (!petMeta.particleEnabled && name.equals("particle-disabled", true)) {
                return true
            }
        }

        return false
    }

    /**
     * Sends the sponsoring message to the given player.
     */
    private fun sendSponsoringMessage(guiPlayerCache: GuiPlayerCache, player: Player) {
        val current = Date().time
        val difference = current - guiPlayerCache.advertisingMessageTime
        val timeOut = 2 * 60 * 1000

        if (difference > timeOut) {
            messageService.sendPlayerMessage(player, collectedMinecraftHeadsMessage)
            guiPlayerCache.advertisingMessageTime = current
        }
    }

    /**
     * Fills up the given [inventory] with the default item.
     */
    private fun fillEmptySlots(inventory: CarriedInventory<Player>) {
        val guiItem = this.configurationService.findGUIItemCollection("static-gui")!![0]

        for (i in 0..53) {
            inventory.query<Inventory>(GridInventory::class.java)
                .query<Inventory>(ItemTypes.AIR)
                .offer(createItemStackIcon(guiItem.icon, true))
        }
    }

    /**
     * Locks the GUI against spamming.
     * Returns if locked.
     */
    private fun lockGui(player: Player): Boolean {
        if (clickProtection.contains(player)) {
            return true
        }

        this.clickProtection.add(player)

        sync(concurrencyService, 10L) {
            this.clickProtection.remove(player)
        }

        return false
    }

    /**
     * Scrolls the page to the axes.
     */
    private fun scrollCollection(player: Player, sourcePosition: Int): Int {
        val vPosition = sourcePosition % 54
        val multiplier = sourcePosition / 54

        val offsetX = pageCache[player]!!.offsetX
        val row = vPosition / 9
        var column = (vPosition % 9) + multiplier * 9

        column -= offsetX

        if (column < 0 || column > 8) {
            return -1
        }

        return row * 9 + column
    }
}