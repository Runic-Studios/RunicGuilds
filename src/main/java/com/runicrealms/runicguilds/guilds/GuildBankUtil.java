package com.runicrealms.runicguilds.guilds;

import com.runicrealms.runicguilds.config.GuildUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GuildBankUtil implements Listener {

    private Map<UUID, Integer> viewers = new HashMap<UUID, Integer>();

    public void open(Player player) {

    }

    public void close(Player player) {

    }

    private void saveToBank(Inventory inventory, Integer page, UUID uuid) {
        List<ItemStack> bank = new ArrayList<ItemStack>(GuildUtil.getGuild(uuid).getBank());
        for (int i = page * 54; i < (page + 1) * 54; i++) {
            bank.set(i, inventory.getItem(i - page * 54));
        }
        GuildUtil.getGuild(uuid).setBank(bank);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (viewers.containsKey(event.getPlayer().getUniqueId())) {
            viewers.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (viewers.containsKey(event.getPlayer().getUniqueId())) {
            viewers.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onDropItemEvent(PlayerDropItemEvent event) {
        if (viewers.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

}
