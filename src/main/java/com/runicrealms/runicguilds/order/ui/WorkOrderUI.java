package com.runicrealms.runicguilds.order.ui;

import com.runicrealms.plugin.common.util.ChatUtils;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.GUIUtil;
import com.runicrealms.plugin.utilities.NumRounder;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.model.GuildInfo;
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
    private final GuildInfo guildInfo;
    private final Inventory inventory;
    private final Player player;

    public WorkOrderUI(GuildInfo guildInfo, Player player) {
        this.guildInfo = guildInfo;
        this.inventory = Bukkit.createInventory(this, 54, ColorUtil.format("&eWeekly Work Order"));
        this.player = player;
        openMenu();
    }

    private static String buildProgressBar(double checkPoint) {
        String bar = "❚❚❚❚❚❚❚❚❚" + ChatColor.GOLD + "❚"; // 10 bars
        try {
            double progress = checkPoint / WorkOrder.MAX_CHECKPOINT_NUMBER;
            int progressRounded = (int) NumRounder.round(progress * 100);
            int percent = Math.min(progressRounded / 10, 10); // limit percent to a maximum of 10
            return ChatColor.GREEN + bar.substring(0, percent) +
                    ChatColor.WHITE + bar.substring(percent) +
                    ChatColor.GRAY + " [" + ChatColor.WHITE + (int) checkPoint +
                    ChatColor.GRAY + "/10]";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ChatColor.WHITE + bar;
    }

    public GuildInfo getGuildInfo() {
        return guildInfo;
    }

    /**
     * Helper method to create the visual menu item for the given work order
     *
     * @param workOrder the work order to display item for
     * @return an ItemStack that can be used for a UI menu
     */
    private ItemStack workOrderItem(WorkOrder workOrder) {
        String displayName = workOrder.getDisplayName();
        ItemStack menuItem = workOrder.getIcon();  // This could be a custom item specific to the work order
        ItemMeta meta = menuItem.getItemMeta();
        if (meta == null) return menuItem;
        meta.setDisplayName(ChatColor.YELLOW + displayName);
        // List out the description of the work order, including the required items and amounts
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.addAll(ChatUtils.formattedText("&6&lCLICK &7to turn in all resources in your inventory. " +
                "Each checkpoint (10% payload across all items) awards guild exp. " +
                "Complete ALL checkpoints to earn a hefty chunk of &a&l25% &a&lbonus &a&lexp&7!"));
        lore.add("");
        lore.add(ChatColor.DARK_GREEN + String.valueOf(ChatColor.BOLD) + "CHECKPOINT:");
        int checkpoint = workOrder.determineCurrentCheckpoint(guildInfo.getWorkOrderMap());
        lore.add(buildProgressBar(checkpoint));
        lore.add("");
        lore.add(ChatColor.GRAY + "Required Items:");
        try {
            workOrder.getItemRequirements().forEach((s, integer) -> {
                String name = RunicItemsAPI.generateItemFromTemplate(s).getDisplayableItem().getDisplayName();
                double current = guildInfo.getWorkOrderMap().get(s);
                double progress = current / integer;
                int progressRounded = (int) NumRounder.round(progress * 100);
                lore.add(ChatColor.GRAY + "- " + ChatColor.BLUE + name + ": " + ChatColor.GRAY + "[" +
                        ChatColor.WHITE + guildInfo.getWorkOrderMap().get(s) + ChatColor.GRAY + "/" + integer + "] " +
                        ChatColor.GREEN + ChatColor.BOLD + progressRounded + "% ");
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        lore.add("");
        double expPerCheckpoint = (double) workOrder.getTotalExp() / WorkOrder.MAX_CHECKPOINT_NUMBER;
        lore.add(ChatColor.translateAlternateColorCodes('&',
                "&9&oEarned &f&o" + (int) (checkpoint * expPerCheckpoint) + " &9&oof " + workOrder.getTotalExp() + " exp"));
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

