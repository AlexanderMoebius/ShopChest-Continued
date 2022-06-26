package de.epiceric.shopchest.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.config.Placeholder;
import de.epiceric.shopchest.event.ShopExtendEvent;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.Message;
import de.epiceric.shopchest.language.Replacement;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.utils.Callback;
import de.epiceric.shopchest.utils.ItemUtils;
import de.epiceric.shopchest.utils.Permissions;
import de.epiceric.shopchest.utils.ShopUtils;
import de.epiceric.shopchest.utils.Utils;
import net.milkbowl.vault.economy.EconomyResponse;

public class ChestProtectListener implements Listener {

    private ShopChest plugin;
    private ShopUtils shopUtils;

    public ChestProtectListener(ShopChest plugin) {
        this.plugin = plugin;
        this.shopUtils = plugin.getShopUtils();
    }

    private void remove(final Shop shop, final Block b, final Player p) {
        if (shop.getInventoryHolder() instanceof DoubleChest) {
            DoubleChest dc = (DoubleChest) shop.getInventoryHolder();
            final Chest l = (Chest) dc.getLeftSide();
            final Chest r = (Chest) dc.getRightSide();

            Location loc = (b.getLocation().equals(l.getLocation()) ? r.getLocation() : l.getLocation());
            final Shop newShop = new Shop(shop.getID(), plugin, shop.getVendor(), shop.getProduct(), loc, shop.getBuyPrice(), shop.getSellPrice(), shop.getShopType());

            shopUtils.removeShop(shop, true, new Callback<Void>(plugin) {
                @Override
                public void onResult(Void result) {
                    newShop.create(true);
                    shopUtils.addShop(newShop, true);
                }
            });
        } else {
            CompletableFuture.runAsync(() -> {
                double creationPrice = shop.getShopType() == ShopType.ADMIN ? Config.shopCreationPriceAdmin : Config.shopCreationPriceNormal;
                if (creationPrice > 0 && Config.refundShopCreation && p.getUniqueId().equals(shop.getVendor().getUniqueId())) {
                    EconomyResponse r = plugin.getEconomy().depositPlayer(p, shop.getLocation().getWorld().getName(), creationPrice);
                    if (!r.transactionSuccess()) {
                        plugin.getDebugLogger().debug("Economy transaction failed: " + r.errorMessage);
                        p.sendMessage(LanguageUtils.getMessage(Message.ERROR_OCCURRED,
                                new Replacement(Placeholder.ERROR, r.errorMessage)));
                        p.sendMessage(LanguageUtils.getMessage(Message.SHOP_REMOVED_REFUND,
                                new Replacement(Placeholder.CREATION_PRICE, 0)));
                    } else {
                        p.sendMessage(LanguageUtils.getMessage(Message.SHOP_REMOVED_REFUND,
                                new Replacement(Placeholder.CREATION_PRICE, creationPrice)));
                    }
                } else {
                    p.sendMessage(LanguageUtils.getMessage(Message.SHOP_REMOVED));
                }
            });

            shopUtils.removeShop(shop, true);
            plugin.getDebugLogger().debug(String.format("%s broke %s's shop (#%d)", p.getName(), shop.getVendor().getName(), shop.getID()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        final Block b = e.getBlock();

        if (shopUtils.isShop(b.getLocation())) {
            final Shop shop = shopUtils.getShop(e.getBlock().getLocation());
            Player p = e.getPlayer();

            if (p.isSneaking() && Utils.hasAxeInHand(p)) {
                plugin.getDebugLogger().debug(String.format("%s tries to break %s's shop (#%d)", p.getName(), shop.getVendor().getName(), shop.getID()));

                if (shop.getShopType() == Shop.ShopType.ADMIN) {
                    if (p.hasPermission(Permissions.REMOVE_ADMIN)) {
                        remove(shop, b, p);
                        return;
                    }
                } else {
                    if (shop.getVendor().getUniqueId().equals(p.getUniqueId()) || p.hasPermission(Permissions.REMOVE_OTHER)) {
                        remove(shop, b, p);
                        return;
                    }
                }
            }

            if (shop.getItem() != null) {
                shop.getItem().resetForPlayer(p);
            }

            e.setCancelled(true);
            e.getPlayer().sendMessage(LanguageUtils.getMessage(Message.CANNOT_BREAK_SHOP));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        ArrayList<Block> bl = new ArrayList<>(e.blockList());
        for (Block b : bl) {
            if (ShopUtils.isShopMaterial(b.getType())) {
                if (shopUtils.isShop(b.getLocation())) e.blockList().remove(b);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        for (Block b: e.getBlocks()) {
            if (ShopUtils.isShopMaterial(b.getType())) {
                if (shopUtils.isShop(b.getLocation())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        for (Block b: e.getBlocks()) {
            if (ShopUtils.isShopMaterial(b.getType())) {
                if (shopUtils.isShop(b.getLocation())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        final Player p = e.getPlayer();
        final Block b = e.getBlockPlaced();

        if (!ShopUtils.isShopMaterial(b.getType())) {
            return;
        }
        
        Container c = (Container) b.getState();
        Block b2;

        // Can't use Utils::getChestLocations since inventory holder
        // has not been updated yet in this event (for 1.13+)

        if (Utils.getMajorVersion() < 13) {
            InventoryHolder ih = c.getInventory().getHolder();
            if (!(ih instanceof DoubleChest)) {
                return;
            }

            DoubleChest dc = (DoubleChest) ih;
            Chest l = (Chest) dc.getLeftSide();
            Chest r = (Chest) dc.getRightSide();

            if (b.getLocation().equals(l.getLocation())) {
                b2 = r.getBlock();
            } else {
                b2 = l.getBlock();
            }
        } else {
            if (!(c instanceof Chest)) {
                return;
            }

            org.bukkit.block.data.type.Chest data = (org.bukkit.block.data.type.Chest) c.getBlockData();

            if (data.equals(Type.SINGLE)) {
                return;
            }

            BlockFace neighborFacing;

            switch (data.getFacing()) {
                case NORTH:
                    neighborFacing = data.getType() == Type.LEFT ? BlockFace.EAST : BlockFace.WEST;
                    break;
                case EAST:
                    neighborFacing = data.getType() == Type.LEFT ? BlockFace.SOUTH : BlockFace.NORTH;
                    break;
                case SOUTH:
                    neighborFacing = data.getType() == Type.LEFT ? BlockFace.WEST : BlockFace.EAST;
                    break;
                case WEST:
                    neighborFacing = data.getType() == Type.LEFT ? BlockFace.NORTH : BlockFace.SOUTH;
                    break;
                default:
                    neighborFacing = null;
            }

            b2 = b.getRelative(neighborFacing);
        }

        final Shop shop = shopUtils.getShop(b2.getLocation());
        if (shop == null)
            return;

        plugin.getDebugLogger().debug(String.format("%s tries to extend %s's shop (#%d)", p.getName(), shop.getVendor().getName(), shop.getID()));


        final List<Block> currentShopBlocks = Utils.getChestLocations(shop).stream().map(Location::getBlock).toList();

        boolean canExtendHook = plugin.getHookManager().canExtendShop(b, currentShopBlocks, p);
        if(!canExtendHook && !p.hasPermission(Permissions.EXTEND_PROTECTED)) {
            plugin.getDebugLogger().debug("Extend cancelled (Hook)");
            e.setCancelled(true);
            p.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_EXTEND_PROTECTED));
            return;
        }

        ShopExtendEvent event = new ShopExtendEvent(p, shop, b.getLocation());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            e.setCancelled(true);
            // Don't send message, assume that plugins that cancel this event will do it
            return;
        }

        if (!p.getUniqueId().equals(shop.getVendor().getUniqueId()) && !p.hasPermission(Permissions.EXTEND_OTHER)) {
            e.setCancelled(true);
            p.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_EXTEND_OTHERS));
            return;
        }

        if (!ItemUtils.isAir(b.getRelative(BlockFace.UP).getType())) {
            e.setCancelled(true);
            p.sendMessage(LanguageUtils.getMessage(Message.CHEST_BLOCKED));
            return;
        }

        final Shop newShop = new Shop(shop.getID(), plugin, shop.getVendor(), shop.getProduct(), shop.getLocation(), shop.getBuyPrice(), shop.getSellPrice(), shop.getShopType());

        shopUtils.removeShop(shop, true, new Callback<Void>(plugin) {
            @Override
            public void onResult(Void result) {
                newShop.create(true);
                shopUtils.addShop(newShop, true);
                plugin.getDebugLogger().debug(String.format("%s extended %s's shop (#%d)", p.getName(), shop.getVendor().getName(), shop.getID()));
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemMove(InventoryMoveItemEvent e) {
        if ((e.getSource().getType().equals(InventoryType.CHEST) || e.getSource().getType().equals(InventoryType.SHULKER_BOX) || e.getSource().getType().equals(InventoryType.BARREL)) && (!e.getInitiator().getType().equals(InventoryType.PLAYER))) {

            if (e.getSource().getHolder() instanceof DoubleChest) {
                DoubleChest dc = (DoubleChest) e.getSource().getHolder();
                Chest r = (Chest) dc.getRightSide();
                Chest l = (Chest) dc.getLeftSide();

                if (shopUtils.isShop(r.getLocation()) || shopUtils.isShop(l.getLocation())) e.setCancelled(true);

            } else if (e.getSource().getHolder() instanceof Chest || e.getSource().getHolder() instanceof ShulkerBox || e.getSource().getHolder() instanceof Barrel) {
                Container c = (Container) e.getSource().getHolder();

                if (shopUtils.isShop(c.getLocation())) e.setCancelled(true);
            }
        }
    }

}
