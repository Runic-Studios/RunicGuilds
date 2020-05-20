package com.runicrealms.runicguilds.api;

import org.bukkit.inventory.ItemStack;

public class GuildShopIcon {

    private int price;
    private ItemStack item;
    private Runnable onBuy;

    public GuildShopIcon(int price, ItemStack item, Runnable onBuy) {
        this.price = price;
        this.item = item;
        this.onBuy = onBuy;
    }

    public int getPrice() {
        return this.price;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public Runnable getOnBuyRunnable() {
        return this.onBuy;
    }

}
