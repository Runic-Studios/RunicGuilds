package com.runicrealms.runicguilds.shop;

import com.runicrealms.runicnpcs.api.NpcClickEvent;
import com.runicrealms.runicguilds.api.GuildShop;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuildShopManager implements Listener {

    private static Map<Integer, GuildShop> shops = new HashMap<Integer, GuildShop>();
    private static Map<UUID, Long> clickCooldowns = new HashMap<UUID, Long>();
    private static Map<UUID, GuildShop> inShop = new HashMap<UUID, GuildShop>();

    public static void registerShop(GuildShop shop) {
        for (Integer npc : shop.getNpcIds()) {
            shops.put(npc, shop);
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
            // TODO - open inventory
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (inShop.containsKey(event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
            if (inShop.get(event.getWhoClicked().getUniqueId()).getItems().containsKey(event.getSlot())) {
                if (event.getRawSlot() < event.getInventory().getSize()) {
                    // TODO - run event for clicked item
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
