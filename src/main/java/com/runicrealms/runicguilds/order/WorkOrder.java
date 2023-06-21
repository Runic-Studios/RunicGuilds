package com.runicrealms.runicguilds.order;

import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class WorkOrder {
    public static final int MAX_CHECKPOINT_NUMBER = 10;
    private final ItemStack icon;
    private final String displayName;
    private final String orderId;
    private final Map<String, Integer> itemRequirements;
    private final int totalExp;

    /**
     * @param displayName      for the order in the ui
     * @param orderId          of the work order
     * @param itemRequirements a map of runic item id to amount
     * @param totalExp         granted by this work order
     */
    public WorkOrder(String orderId, String displayName, String icon, Map<String, Integer> itemRequirements, int totalExp) {
        this.icon = RunicItemsAPI.generateItemFromTemplate(icon).generateGUIItem();
        this.displayName = displayName;
        this.orderId = orderId;
        this.itemRequirements = itemRequirements;
        this.totalExp = totalExp;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getOrderId() {
        return orderId;
    }

    public Map<String, Integer> getItemRequirements() {
        return itemRequirements;
    }

    public int getTotalExp() {
        return totalExp;
    }

    /**
     * Based on the current progress across all payloads, determines the highest unlocked checkpoint
     *
     * @param currentItems a map of all payloads and the current progress
     * @return the current checkpoint
     */
    public int determineCurrentCheckpoint(Map<String, Integer> currentItems) {
        int minCheckpoint = MAX_CHECKPOINT_NUMBER; // Start with max value and look for minimum

        for (Map.Entry<String, Integer> entry : this.itemRequirements.entrySet()) { // Loop through master map
            String itemId = entry.getKey();
            int requiredAmount = entry.getValue();

            Integer currentItemAmount = currentItems.get(itemId);

            // If current item amount is null, it means we have 0 of that item, so checkpoint is 0
            int checkpoint = (currentItemAmount != null)
                    ? (currentItemAmount * MAX_CHECKPOINT_NUMBER) / requiredAmount
                    : 0;

            minCheckpoint = Math.min(minCheckpoint, checkpoint);
        }

        return minCheckpoint;
    }


}
