package com.runicrealms.runicguilds.shop;

import com.runicrealms.plugin.item.util.ItemRemover;
import com.runicrealms.plugin.utilities.CurrencyUtil;
import com.runicrealms.runicguilds.api.GuildShopBuyResponse;
import com.runicrealms.runicguilds.api.GuildShopIcon;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicnpcs.api.NpcClickEvent;
import com.runicrealms.runicguilds.api.GuildShop;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuildShopManager implements Listener {

    private static final Map<Integer, GuildShop> shops = new HashMap<>();
    private static final Map<UUID, Long> clickCooldowns = new HashMap<>();
    private static final Map<UUID, GuildShop> inShop = new HashMap<>();
    private static ItemStack blankSlot;

    public static void registerShop(GuildShop shop) {
        for (Integer npc : shop.getNpcIds()) {
            shops.put(npc, shop);
        }
        if (blankSlot == null) {
            blankSlot = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta meta = blankSlot.getItemMeta();
            if (meta == null) return;
            meta.setDisplayName(" ");
            blankSlot.setItemMeta(meta);
        }
    }

    @EventHandler
    public void onNpcClick(NpcClickEvent event) {
        if (clickCooldowns.containsKey(event.getPlayer().getUniqueId())) {
            if (clickCooldowns.get(event.getPlayer().getUniqueId()) + 2000 > System.currentTimeMillis()) {
                return;
            }
        }
        clickCooldowns.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        if (shops.containsKey(event.getNpc().getId())) {
            GuildShop shop = shops.get(event.getNpc().getId());
            Inventory inventory = Bukkit.createInventory(null, 9 + shop.getShopSize(), shop.getName());
            for (int i = 0; i < 9; i++) {
                if (i != 4) {
                    inventory.setItem(i, blankSlot);
                }
            }
            inventory.setItem(4, shop.getIcon());
            for (Map.Entry<Integer, GuildShopIcon> trade : shop.getTrades().entrySet()) {
                inventory.setItem(trade.getKey() + 9, trade.getValue().getItem());
            }
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            event.getPlayer().openInventory(inventory);
            inShop.put(event.getPlayer().getUniqueId(), shop);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (inShop.containsKey(player.getUniqueId())) {
                event.setCancelled(true);
                if (inShop.get(player.getUniqueId()).getTrades().containsKey(event.getSlot() - 9)) {
                    if (event.getRawSlot() < event.getInventory().getSize()) {
                        GuildShopIcon icon = inShop.get(player.getUniqueId()).getTrades().get(event.getSlot() - 9);
                        GuildShopBuyResponse response = icon.getCondition().getResponse(player);
                        if (response.canBuy()) {
                            if (hasItems(player, icon.getCurrency(), icon.getPrice())) {
                                if (icon.removePayment()) {
                                    ItemRemover.takeItem(player, CurrencyUtil.goldCoin(), icon.getPrice());
                                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                                    player.updateInventory();
                                }
                                player.closeInventory();
                                icon.runBuy(player);
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', response.getResponse()));
                            } else {
                                player.closeInventory();
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                                player.sendMessage(ChatColor.RED + "You don't have enough items to buy this!");
                            }
                        } else {
                            player.closeInventory();
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', response.getResponse()));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        inShop.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clickCooldowns.remove(event.getPlayer().getUniqueId());
        inShop.remove(event.getPlayer().getUniqueId());
    }

    private static boolean hasItems(Player player, ItemStack item, Integer needed) {
        if (needed == 0) return true;
        int amount = 0;
        for (ItemStack inventoryItem : player.getInventory().getContents()) {
            if (inventoryItem != null) {
                if (RunicItemsAPI.isRunicItemSimilar(item, inventoryItem)) {
                    amount += inventoryItem.getAmount();
                    if (amount >= needed) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
