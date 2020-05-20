package com.runicrealms.runicguilds.api;

import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;

public interface GuildShop {

    /**
     * Represents the buyable items in the shop GUI
     * Map key represents the item slot
     */
    public Map<Integer, GuildShopIcon> getTrades();

    /**
     * Amount of slots in the shop GUI
     * NOTICE: does not include the first row of items, occupied by the shop icon
     */
    public int getShopSize();

    /**
     * The shop icon that appears in the first row of the GUI
     */
    public ItemStack getIcon();

    /**
     * List of NPCs that can open this GUI
     * Uses RunicNpcs, no citizens
     */
    public Collection<Integer> getNpcIds();

    /**
     * Shop GUI name
     */
    public String getName();

}
