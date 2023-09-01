package com.runicrealms.plugin.runicguilds.ui;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class GuildMembersUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof GuildMembersUI)) return;
        // prevent clicking items in player inventory
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }
        GuildMembersUI guildMembersUI = (GuildMembersUI) event.getClickedInventory().getHolder();
        if (guildMembersUI == null) return;

        // insurance
        if (!event.getWhoClicked().equals(guildMembersUI.getPlayer())) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;
        if (guildMembersUI.getInventory().getItem(event.getRawSlot()) == null) return;
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        event.setCancelled(true);

        if (event.getSlot() == 0) {
            guildMembersUI.setPage(guildMembersUI.getPage() - 1);
        } else if (event.getSlot() == 8) {
            guildMembersUI.setPage(guildMembersUI.getPage() + 1);
        }
    }

}
