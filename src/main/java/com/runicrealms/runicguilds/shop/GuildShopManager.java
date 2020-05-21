package com.runicrealms.runicguilds.shop;

import com.runicrealms.runicguilds.api.GuildShopIcon;
import com.runicrealms.runicnpcs.api.NpcClickEvent;
import com.runicrealms.runicguilds.api.GuildShop;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

    private static Map<Integer, GuildShop> shops = new HashMap<>();
    private static Map<UUID, Long> clickCooldowns = new HashMap<UUID, Long>();
    private static Map<UUID, GuildShop> inShop = new HashMap<UUID, GuildShop>();
    private static ItemStack blankSlot;

    public static void registerShop(GuildShop shop) {
        for (Integer npc : shop.getNpcIds()) {
            shops.put(npc, shop);
        }
        if (blankSlot == null) {
            blankSlot = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta meta = blankSlot.getItemMeta();
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
                inventory.setItem(trade.getKey(), trade.getValue().getItem());
            }
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
                if (inShop.get(player.getUniqueId()).getTrades().containsKey(event.getSlot())) {
                    if (event.getRawSlot() < event.getInventory().getSize()) {
                        GuildShopIcon icon = inShop.get(player.getUniqueId()).getTrades().get(event.getSlot());
                        if (icon.canBuy(player)) {
                            if (player.getInventory().contains(icon.getCurrency(), icon.getPrice())) {
                                if (icon.getPrice() > icon.getCurrency().getMaxStackSize()) {
                                    ItemStack stack = icon.getCurrency().clone();
                                    stack.setAmount(icon.getCurrency().getMaxStackSize());
                                    for (int i = 0; i < (icon.getPrice() - (icon.getPrice() % icon.getCurrency().getMaxStackSize())) / icon.getCurrency().getMaxStackSize(); i++) {
                                        player.getInventory().remove(stack);
                                    }
                                }
                                if (icon.getPrice() % icon.getCurrency().getMaxStackSize() != 0) {
                                    ItemStack leftOver = icon.getCurrency().clone();
                                    leftOver.setAmount(icon.getPrice() % icon.getCurrency().getMaxStackSize());
                                    player.getInventory().remove(leftOver);
                                }
                                player.updateInventory();
                                player.closeInventory();
                                icon.getOnBuyRunnable().run(player);
                            } else {
                                player.closeInventory();
                                player.sendMessage(ChatColor.RED + "You do not have enough items to buy this!");
                            }
                        } else {
                            player.closeInventory();
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', icon.getCondition().getDeniedMessage()));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (inShop.containsKey(event.getPlayer().getUniqueId())) {
            inShop.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (clickCooldowns.containsKey(event.getPlayer().getUniqueId())) {
            clickCooldowns.remove(event.getPlayer().getUniqueId());
        }
        if (inShop.containsKey(event.getPlayer().getUniqueId())) {
            inShop.remove(event.getPlayer().getUniqueId());
        }
    }
}
