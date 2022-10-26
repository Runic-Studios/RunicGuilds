package com.runicrealms.runicguilds.shop;

public class GuildShopBuyResponse {

    private final boolean canBuy;
    private final String response;

    public GuildShopBuyResponse(boolean canBuy, String response) {
        this.canBuy = canBuy;
        this.response = response;
    }

    public boolean canBuy() {
        return this.canBuy;
    }

    public String getResponse() {
        return this.response;
    }

}
