package com.github.shynixn.petblocks.business.logic.persistence2;

import com.github.shynixn.petblocks.lib.util.IDatabaseController;
import org.bukkit.entity.Player;

/**
 * Created by Shynixn
 */
public interface IPetDataController extends IDatabaseController<PetData> {
    /**
     * Returns the petdata from the given player
     * @param player player
     * @return petData
     */
    PetData getByPlayer(Player player);

    /**
     * Creates a petdata from the given player
     * @param player player
     * @return petdata
     */
    PetData create(Player player);
}
