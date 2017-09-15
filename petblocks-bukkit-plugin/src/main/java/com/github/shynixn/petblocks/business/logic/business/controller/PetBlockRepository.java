package com.github.shynixn.petblocks.business.logic.business.controller;

import com.github.shynixn.petblocks.api.bukkit.event.PetBlockDeathEvent;
import com.github.shynixn.petblocks.api.business.controller.PetBlockController;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright 2017 Shynixn
 * <p>
 * Do not remove this header!
 * <p>
 * Version 1.0
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2017
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
public final class PetBlockRepository implements PetBlockController {
    private final Map<Player, PetBlock> petblocks = new HashMap<>();

    /**
     * Creates a new petblock for the given player and meta
     *
     * @param player  player
     * @param petMeta meta
     * @return petblock
     */
    @Override
    public PetBlock create(Object player, PetMeta petMeta) {
        final Player mPlayer = (Player) player;
        return NMSRegistry.createPetBlock(mPlayer.getLocation(), petMeta);
    }

    /**
     * Returns the petblock of the given player
     *
     * @param player player
     * @return petblock
     */
    @Override
    public PetBlock getByPlayer(Object player) {
        final Player mPlayer = (Player) player;
        if (this.petblocks.containsKey(mPlayer)) {
            return this.petblocks.get(mPlayer);
        }
        return null;
    }

    /**
     * Removes the petblock of the given player
     *
     * @param player player
     */
    @Override
    public void removeByPlayer(Object player) {
        this.remove(this.getByPlayer(player));
    }

    /**
     * Stores a new a item in the repository
     *
     * @param item item
     */
    @Override
    public void store(PetBlock item) {
        final Player mPlayer = (Player) item.getPlayer();
        if (!this.petblocks.containsKey(mPlayer)) {
            this.petblocks.put(mPlayer, item);
        }
    }

    /**
     * Removes an item from the repository
     *
     * @param item item
     */
    @Override
    public void remove(PetBlock item) {
        if (item == null)
            return;
        final Player player = (Player) item.getPlayer();
        if (this.petblocks.containsKey(player)) {
            final PetBlockDeathEvent event = new PetBlockDeathEvent(this.petblocks.get(player));
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCanceled()) {
                this.petblocks.get(player).remove();
                this.petblocks.remove(player);
            }
        }
    }

    /**
     * Returns the amount of items in the repository
     *
     * @return size
     */
    @Override
    public int size() {
        return this.petblocks.size();
    }

    /**
     * Returns all items from the repository as unmodifiableList
     *
     * @return items
     */
    @Override
    public List<PetBlock> getAll() {
        return new ArrayList<>(this.petblocks.values());
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     * <p>
     * <p>While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to
     * declare concrete implementations of the {@code close} method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     * <p>
     * <p> Cases where the close operation may fail require careful
     * attention by implementers. It is strongly advised to relinquish
     * the underlying resources and to internally <em>mark</em> the
     * resource as closed, prior to throwing the exception. The {@code
     * close} method is unlikely to be invoked more than once and so
     * this ensures that the resources are released in a timely manner.
     * Furthermore it reduces problems that could arise when the resource
     * wraps, or is wrapped, by another resource.
     * <p>
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     * <p>
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     * <p>
     * More generally, if it would cause problems for an
     * exception to be suppressed, the {@code AutoCloseable.close}
     * method should not throw it.
     * <p>
     * <p>Note that unlike the {@link Closeable#close close}
     * method of {@link Closeable}, this {@code close} method
     * is <em>not</em> required to be idempotent.  In other words,
     * calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is
     * required to have no effect if called more than once.
     * <p>
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        for (final Player player : this.petblocks.keySet()) {
            this.petblocks.get(player).remove();
        }
        this.petblocks.clear();
    }
}