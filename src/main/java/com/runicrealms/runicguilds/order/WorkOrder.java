package com.runicrealms.runicguilds.order;

import java.util.Map;

public class WorkOrder {
    private static final int MAX_CHECKPOINT_NUMBER = 10;
    private final String name;
    private final Map<String, Integer> itemRequirements;
    private final int totalExp;

    /**
     * @param name             of the work order
     * @param itemRequirements a map of runic item id to amount
     * @param totalExp         granted by this work order
     */
    public WorkOrder(String name, Map<String, Integer> itemRequirements, int totalExp) {
        this.name = name;
        this.itemRequirements = itemRequirements;
        this.totalExp = totalExp;
    }

    public String getName() {
        return name;
    }

    /**
     * Offers an ADDITIONAL 25% bonus for the last checkpoint
     *
     * @param checkpointNumber the current checkpoint towards this order
     * @return the total experience to reward
     */
    public int getCheckpointReward(int checkpointNumber) {
        if (checkpointNumber == MAX_CHECKPOINT_NUMBER) {
            return (totalExp / 10) + (totalExp / 4); // Extra 25% for the last checkpoint
        } else {
            return totalExp / 10;
        }
    }

    public int getTotalItemCount() {
        return itemRequirements.values().stream().mapToInt(Integer::intValue).sum();
    }

}
