package com.github.shynixn.petblockstools.logic.service

import com.github.shynixn.discordwebhook.contract.DiscordWebhookService
import com.github.shynixn.discordwebhook.entity.DiscordAuthor
import com.github.shynixn.discordwebhook.entity.DiscordEmbed
import com.github.shynixn.discordwebhook.entity.DiscordField
import com.github.shynixn.discordwebhook.entity.DiscordPayload
import com.github.shynixn.discordwebhook.extension.decimal
import com.github.shynixn.discordwebhook.extension.timestampIso8601
import com.github.shynixn.discordwebhook.impl.DiscordWebhookServiceImpl
import com.github.shynixn.petblockstools.contract.SnapshotService
import java.awt.Color
import java.util.*

/**
 * Created by Shynixn 2019.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2019 by Shynixn
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
class PublishPetBlocksSnapshotToDiscord(
    private val snapshotService: SnapshotService = SnapshotServiceImpl(),
    private val discordWebHookService: DiscordWebhookService = DiscordWebhookServiceImpl()) {
    private val bukkitSnapshotRepo = "https://oss.sonatype.org/content/repositories/snapshots/com/github/shynixn/petblocks/petblocks-bukkit-plugin/"
    private val spongeSnapshotRepo = "https://oss.sonatype.org/content/repositories/snapshots/com/github/shynixn/petblocks/petblocks-sponge-plugin/"
    private val petBlocksImage = "https://raw.githubusercontent.com/Shynixn/travis-ci-discord-webhook/master/pet.jpg"

    /**
     * Sends the snapshot status to discord.
     */
    fun publishSnapshotToDiscord(webHookUrl: String) {
        val bukkitSnapshotDownloadUrl = snapshotService.findSnapshotDownloadUrl(bukkitSnapshotRepo)
        val spongeSnapshotDownloadUrl = snapshotService.findSnapshotDownloadUrl(spongeSnapshotRepo)
        val snapshotId = snapshotService.findSnapshotId(bukkitSnapshotRepo)

        val payload = DiscordPayload("PetBlocks-Snapshots", petBlocksImage)

        val embeddedMessage = DiscordEmbed(
            "Downloads",
            Color(0, 145, 234).decimal,
            DiscordAuthor("PetBlocks Snapshot - Shynixn/PetBlocks - $snapshotId", "", petBlocksImage),
            "Author Shynixn published a new snapshot build",
            Date().timestampIso8601
        )

        embeddedMessage.fields.add(DiscordField("Spigot/Bukkit", "<:bukkit:493024859555627009> [`Direct Download`]($bukkitSnapshotDownloadUrl)"))
        embeddedMessage.fields.add(DiscordField("Sponge", "<:sponge:493038148629168138> [`Direct Download`]($spongeSnapshotDownloadUrl)"))

        payload.embeds.add(embeddedMessage)

        discordWebHookService.sendDiscordPayload(webHookUrl, payload)
    }
}