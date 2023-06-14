package com.runicrealms.runicguilds.order.ui;

import com.runicrealms.plugin.common.util.ChatUtils;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.GUIUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.order.WorkOrder;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WorkOrderUI implements InventoryHolder {
    private final Inventory inventory;
    private final Player player;

    public WorkOrderUI(Player player) {
        this.inventory = Bukkit.createInventory(this, 54, ColorUtil.format("&eWeekly Work Order"));
        this.player = player;
        openMenu();
    }

    /**
     * Helper method to create the visual menu item for the given work order
     *
     * @param workOrder the work order to display item for
     * @return an ItemStack that can be used for a UI menu
     */
    private ItemStack workOrderItem(WorkOrder workOrder) {
        String displayName = workOrder.getName();
        ItemStack menuItem = new ItemStack(Material.PAPER);  // This could be a custom item specific to the work order
        ItemMeta meta = menuItem.getItemMeta();
        if (meta == null) return menuItem;

        meta.setDisplayName(ChatColor.YELLOW + displayName);
        // List out the description of the work order, including the required items and amounts
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.addAll(ChatUtils.formattedText("&6&lCLICK &7the item below to turn in all resources in your inventory. " +
                "Upon reaching a &d&lCHECKPOINT&7, you will earn a chunk of experience! " +
                "Complete each checkpoint to earn a hefty chunk of &a&lBONUS EXP&7!"));
        lore.add("");
        lore.add(ChatColor.GRAY + "Required Items:");
        try {
            workOrder.getItemRequirements().forEach((s, integer) -> {
                String name = RunicItemsAPI.generateItemFromTemplate(s).getDisplayableItem().getDisplayName();
                lore.add(ChatColor.GRAY + "-" + name + ": [0/" + integer + "]");
            });
        } catch (Exception ex) {

        }
        lore.add("");
        meta.setLore(lore);
        menuItem.setItemMeta(meta);
        return menuItem;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public Player getPlayer() {
        return this.player;
    }

    /**
     * Opens the inventory associated w/ this GUI
     */
    private void openMenu() {
        WorkOrder workOrder = RunicGuilds.getWorkOrderManager().getCurrentWorkOrder();
        this.inventory.clear();
        GUIUtil.fillInventoryBorders(this.inventory);
        this.inventory.setItem(0, GUIUtil.CLOSE_BUTTON);
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.addAll(ChatUtils.formattedText("&7The Guild Foreman will upgrade your guild if you provide him with resources. " +
                "Each week, deliver resources to the foreman to earn &aguild experience&7! " +
                "By earning experience, you can advance your &6&lGUILD STAGE&7, unlocking new perks " +
                "and increasing your guild size!"));
        lore.add("");
        this.inventory.setItem(4, GUIUtil.dispItem(
                Material.PAPER,
                ChatColor.YELLOW + String.valueOf(ChatColor.BOLD) + "GUILD WORK ORDERS",
                lore
        ));
        this.inventory.setItem(22, workOrderItem(workOrder));
    }
}

