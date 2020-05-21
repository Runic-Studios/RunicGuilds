package com.runicrealms.runicguilds.api;

import org.bukkit.entity.Player;

public interface GuildShopBuyCondition {

    public GuildShopBuyResponse getResponse(Player player);

}
