package com.runicrealms.runicguilds.ui;

import com.runicrealms.plugin.utilities.GUIUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class GuildInfoUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof GuildInfoUI)) return;
        // Prevent clicking items in player inventory
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }
        GuildInfoUI guildInfoUI = (GuildInfoUI) event.getClickedInventory().getHolder();
        if (guildInfoUI == null) return;

        // Insurance
        if (!event.getWhoClicked().equals(guildInfoUI.getPlayer())) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;
        if (guildInfoUI.getInventory().getItem(event.getRawSlot()) == null) return;
        ItemStack item = event.getCurrentItem();
        Material material = item.getType();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        event.setCancelled(true);
        if (RunicGuilds.getDataAPI().getGuildInfo(player.getUniqueId()) == null) return;
        if (material == GUIUtil.CLOSE_BUTTON.getType())
            player.closeInventory();
        else if (material == Material.PLAYER_HEAD)
            player.openInventory(new GuildMembersUI(player).getInventory());
    }

}
