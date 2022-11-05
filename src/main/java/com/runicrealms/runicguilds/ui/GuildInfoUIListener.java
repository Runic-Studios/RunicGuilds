package com.runicrealms.runicguilds.ui;

import com.runicrealms.plugin.utilities.GUIUtil;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.util.GuildUtil;
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
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!(e.getView().getTopInventory().getHolder() instanceof GuildInfoUI)) return;
        // prevent clicking items in player inventory
        if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
            e.setCancelled(true);
            return;
        }
        GuildInfoUI guildInfoUI = (GuildInfoUI) e.getClickedInventory().getHolder();
        if (guildInfoUI == null) return;

        // insurance
        if (!e.getWhoClicked().equals(guildInfoUI.getPlayer())) {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            return;
        }
        Player player = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        if (guildInfoUI.getInventory().getItem(e.getRawSlot()) == null) return;
        ItemStack item = e.getCurrentItem();
        Material material = item.getType();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        e.setCancelled(true);
        if (GuildUtil.getGuildData(player.getUniqueId()) == null) return;
        Guild guild = GuildUtil.getGuildData(player.getUniqueId()).getData();
        if (material == GUIUtil.CLOSE_BUTTON.getType())
            player.closeInventory();
        else if (material == Material.PLAYER_HEAD)
            player.openInventory(new GuildMembersUI(player, guild).getInventory());
    }

}
