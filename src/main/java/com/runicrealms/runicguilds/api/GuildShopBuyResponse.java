package com.runicrealms.runicguilds.api;

public class GuildShopBuyResponse {

    private boolean canBuy;
    private String response;

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
