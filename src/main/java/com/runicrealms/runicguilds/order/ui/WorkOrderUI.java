package com.runicrealms.runicguilds.order.ui;

import com.runicrealms.plugin.common.util.ChatUtils;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.GUIUtil;
import com.runicrealms.plugin.utilities.NumRounder;
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

    private static String buildProgressBar(double total) { // todo: take in a guild
        String bar = "❚❚❚❚❚❚❚❚❚❚"; // 10 bars
        try {
            double current = 2500;
            double progress = current / total;
            int progressRounded = (int) NumRounder.round(progress * 100);
            int percent = Math.min(progressRounded / 10, 10); // limit percent to a maximum of 10
            return ChatColor.GREEN + bar.substring(0, percent) + ChatColor.WHITE + bar.substring(percent) +
                    " [0/10]" +
                    " " + ChatColor.GREEN + ChatColor.BOLD + progressRounded + "% ";
        } catch (Exception ex) {
//            Bukkit.getLogger().warning("There was a problem creating the gathering progress bar for " + gatheringSkill.getIdentifier());
            ex.printStackTrace();
        }
        return ChatColor.WHITE + bar;
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
        // todo: add a custom 'display name' field to yml
        // List out the description of the work order, including the required items and amounts
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.addAll(ChatUtils.formattedText("&6&lCLICK &7the item below to turn in all resources in your inventory. " +
                "Each checkpoint awards guild exp. " +
                "Complete ALL checkpoints to earn a hefty chunk of &a&l25% bonus &a&lexp&7!"));
        lore.add("");
        lore.add(ChatColor.DARK_GREEN + String.valueOf(ChatColor.BOLD) + "EXP CHECKPOINTS:");
        lore.add(buildProgressBar(workOrder.getItemRequirements().values().stream().mapToDouble(Integer::doubleValue).sum()));
        lore.add("");
        lore.add(ChatColor.GRAY + "Required Items:");
        try {
            workOrder.getItemRequirements().forEach((s, integer) -> {
                String name = RunicItemsAPI.generateItemFromTemplate(s).getDisplayableItem().getDisplayName();
                lore.add(ChatColor.GRAY + "- " + ChatColor.BLUE + name + ": " + ChatColor.GRAY + "[0/" + integer + "]");
            });
            // todo: change the config format to include a template-id icon (generateGUIItem)
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        lore.add("");
        lore.add(ChatColor.translateAlternateColorCodes('&',
                "&9&oEarned &f&o0 &9&oof " + workOrder.getTotalExp() + " exp"));
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

