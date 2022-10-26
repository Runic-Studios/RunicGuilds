package com.runicrealms.runicguilds.shop;

import org.bukkit.entity.Player;

public interface GuildShopBuyCondition {

    public GuildShopBuyResponse getResponse(Player player);

}
