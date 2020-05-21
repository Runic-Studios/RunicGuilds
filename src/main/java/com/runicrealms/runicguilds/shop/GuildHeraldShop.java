package com.runicrealms.runicguilds.shop;

import com.runicrealms.plugin.utilities.CurrencyUtil;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.api.GuildShop;
import com.runicrealms.runicguilds.api.GuildShopBuyCondition;
import com.runicrealms.runicguilds.api.GuildShopBuyResponse;
import com.runicrealms.runicguilds.api.GuildShopBuyRunnable;
import com.runicrealms.runicguilds.api.GuildShopIcon;
import com.runicrealms.runicguilds.data.GuildUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GuildHeraldShop implements GuildShop {

    private ItemStack icon;
    private Map<Integer, GuildShopIcon> trades = new HashMap<Integer, GuildShopIcon>();

    public GuildHeraldShop() {

        this.icon = new ItemStack(Material.GOLD_INGOT);
        ItemMeta iconMeta = this.icon.getItemMeta();
        iconMeta.setDisplayName(ChatColor.GOLD + "Guild Herald Shop");
        iconMeta.setLore(Arrays.asList(new String[] {ChatColor.GRAY + "Purchase a Guild"}));
        this.icon.setItemMeta(iconMeta);

        ItemStack trade = new ItemStack(Material.IRON_SWORD);
        ItemMeta tradeMeta = trade.getItemMeta();
        tradeMeta.setDisplayName(ChatColor.YELLOW + "Purchase a Guild");
        tradeMeta.setLore(Arrays.asList(new String[] {ChatColor.GOLD + " " + Plugin.GUILD_COST + " " + ChatColor.GRAY + "coins"}));
        tradeMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        trade.setItemMeta(tradeMeta);

        GuildShopIcon tradeIcon = new GuildShopIcon(Plugin.GUILD_COST, CurrencyUtil.goldCoin(), trade, new GuildShopBuyCondition() {
            @Override
            public GuildShopBuyResponse getResponse(Player player) {
                if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
                    if (!Plugin.getPlayersCreatingGuild().contains(player.getUniqueId())) {
                        return new GuildShopBuyResponse(true, ChatColor.YELLOW + "Creating a guild will cost you " + Plugin.GUILD_COST + " gold. To confirm or cancel the purchasing of this guild, type " + ChatColor.GOLD + "/guild confirm" + ChatColor.YELLOW + " or " + ChatColor.GOLD + "/guild cancel" + ChatColor.YELLOW + " in chat.");
                    } else {
                        return new GuildShopBuyResponse(false, ChatColor.YELLOW + "Type " + ChatColor.GOLD + "/guild confirm" + ChatColor.YELLOW + " or " + ChatColor.GOLD + "/guild cancel" + ChatColor.YELLOW + " in chat to confirm/cancel the creation of your guild.");
                    }
                } else {
                    return new GuildShopBuyResponse(false, ChatColor.YELLOW + "You cannot create a guild because you are already in one!");
                }
            }
        }, new GuildShopBuyRunnable() {
            @Override
            public void run(Player player) {
                Plugin.getPlayersCreatingGuild().add(player.getUniqueId());
            }
        });
        tradeIcon.setRemovePayment(false);
        this.trades.put(0, tradeIcon);

    }

    @Override
    public Map<Integer, GuildShopIcon> getTrades() {
        return this.trades;
    }

    @Override
    public int getShopSize() {
        return 9;
    }

    @Override
    public ItemStack getIcon() {
        return this.icon;
    }

    @Override
    public Collection<Integer> getNpcIds() {
        return Plugin.GUILD_HERALDS;
    }

    @Override
    public String getName() {
        return "Guild Herald";
    }

}