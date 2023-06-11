package com.runicrealms.runicguilds.ui;

import com.runicrealms.plugin.common.util.GUIUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.model.GuildInfo;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

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
        ItemStack item = event.getCurrentItem();
        Material material = item.getType();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        event.setCancelled(true);
        if (!RunicGuilds.getGuildsAPI().isInGuild(player.getUniqueId())) return;
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (material == GUIUtil.CLOSE_BUTTON.getType())
            player.closeInventory();
        else if (material == GUIUtil.BACK_BUTTON.getType())
            event.getWhoClicked().openInventory(new GuildInfoUI(player, guildInfo).getInventory());
    }

}
