package com.runicrealms.runicguilds.api;

import org.bukkit.entity.Player;

public interface GuildShopBuyCondition {

    public boolean canBuy(Player player);

    public String getDeniedMessage();

}
