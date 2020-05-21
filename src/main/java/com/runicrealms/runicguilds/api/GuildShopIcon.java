package com.runicrealms.runicguilds.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GuildShopIcon {

    private int price;
    private ItemStack currency;
    private ItemStack item;
    private GuildShopBuyCondition condition;
    private GuildShopBuyRunnable onBuy;
    private boolean removePayment = true;

    public GuildShopIcon(int price, ItemStack currency, ItemStack item, GuildShopBuyCondition condition, GuildShopBuyRunnable onBuy) {
        this.price = price;
        this.currency = currency;
        this.item = item;
        this.condition = condition;
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

    public GuildShopBuyCondition getCondition() {
        return this.condition;
    }

    public void runBuy(Player player) {
        if (this.onBuy != null) {
            this.onBuy.run(player);
        }
    }

    public boolean removePayment() {
        return this.removePayment;
    }

    public void setRemovePayment(boolean removePayment) {
        this.removePayment = removePayment;
    }

}
