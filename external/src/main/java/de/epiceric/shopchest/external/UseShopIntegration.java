package de.epiceric.shopchest.external;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface UseShopIntegration {

    /**
     * Whether the player can use the player shop represented by this block
     *
     * @param block  The {@link Block} where the shop is
     * @param player The {@link Player} that is trying to use the shop
     * @return {@code true} if the player can use the shop. {@code false} otherwise
     */
    default boolean canUseAdminShop(Block block, Player player) {
        return true;
    }

    /**
     * Whether the player can use the admin shop represented by this block
     *
     * @param block  The {@link Block} where the shop is placed
     * @param player The {@link Player} that is trying to use the shop
     * @return {@code true} if the player can use the shop. {@code false} otherwise
     */
    default boolean canUsePlayerShop(Block block, Player player) {
        return true;
    }

    /**
     * <p>
     * Whether the player can use the shop represented by this block
     * </p>
     * <br>
     * <p>
     * It's the method called by the plugin
     * </p>
     * <p>
     * By default, it calls {@link UseShopIntegration#canUsePlayerShop(Block, Player)}} and
     * {@link UseShopIntegration#canUseAdminShop(Block, Player)}
     * </p>
     *
     * @param block  The {@link Block} where the shop is placed
     * @param player The {@link Player} that is trying to use the shop
     * @param admin  Whether this shop is an admin shop
     * @return {@code true} if the player can use the shop. {@code false} otherwise
     */
    default boolean canUseShop(Block block, Player player, boolean admin) {
        return admin ? canUseAdminShop(block, player) : canUsePlayerShop(block, player);
    }

}
