package com.runicrealms.runicguilds.api;

import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;

public interface GuildShop {

    public Map<Integer, GuildShopIcon> getItems();

    public int getShopSize();

    public ItemStack getIcon();

    public Collection<Integer> getNpcIds();

}
