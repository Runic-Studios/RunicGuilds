package com.runicrealms.runicguilds.order.ui;

import com.runicrealms.plugin.common.util.GUIUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class WorkOrderUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof WorkOrderUI workOrderUI)) return;
        // prevent clicking items in player inventory
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }
        // insurance
        if (!event.getWhoClicked().equals(workOrderUI.getPlayer())) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;
        if (workOrderUI.getInventory().getItem(event.getRawSlot()) == null) return;
        ItemStack item = event.getCurrentItem();
        Material material = item.getType();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        event.setCancelled(true);
        if (material == GUIUtil.CLOSE_BUTTON.getType())
            event.getWhoClicked().closeInventory();
//        if (GatheringSkill.getGatheringSkillMenuSlots().contains(event.getRawSlot()))
//            RunicProfessions.getAPI().openGatheringSkillGUI(player, GatheringSkill.getFromMenuSlot(event.getSlot()));
    }
}
