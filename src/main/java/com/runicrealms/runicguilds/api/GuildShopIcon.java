package com.runicrealms.runicguilds.api;

import org.bukkit.inventory.ItemStack;

public class GuildShopIcon {

    private int price;
    private ItemStack currency;
    private ItemStack item;
    private GuildShopBuyRunnable onBuy;

    public GuildShopIcon(int price, ItemStack currency, ItemStack item, GuildShopBuyRunnable onBuy) {
        this.price = price;
        this.currency = currency;
        this.item = item;
        this.onBuy = onBuy;
    }

    public int getPrice() {
        return this.price;
    }

    public ItemStack getCurrency() {
        return this.currency;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public GuildShopBuyRunnable getOnBuyRunnable() {
        return this.onBuy;
    }

}
