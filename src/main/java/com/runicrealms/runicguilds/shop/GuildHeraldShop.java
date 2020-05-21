package com.runicrealms.runicguilds.shop;

import com.runicrealms.plugin.utilities.CurrencyUtil;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.api.GuildShop;
import com.runicrealms.runicguilds.api.GuildShopBuyCondition;
import com.runicrealms.runicguilds.api.GuildShopBuyRunnable;
import com.runicrealms.runicguilds.api.GuildShopIcon;
import com.runicrealms.runicguilds.api.RunicGuildsAPI;
import com.runicrealms.runicguilds.data.GuildUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GuildHeraldShop implements GuildShop {

    private ItemStack icon;
    private List<Integer> npcs;
    private Map<Integer, GuildShopIcon> trades;

    public GuildHeraldShop() {
        this.icon = new ItemStack(Material.GOLD_INGOT);
        ItemMeta iconMeta = this.icon.getItemMeta();
        iconMeta.setDisplayName(ChatColor.GOLD + "Guild Herald Shop");
        iconMeta.setLore(Arrays.asList(new String[] {ChatColor.GRAY + "Purchase a Guild"}));
        this.icon.setItemMeta(iconMeta);
        this.npcs = Plugin.getInstance().getConfig().getIntegerList("guild-heralds");
        ItemStack trade = new ItemStack(Material.IRON_SWORD);
        ItemMeta tradeMeta = trade.getItemMeta();
        tradeMeta.setDisplayName(ChatColor.YELLOW + "Purchase a Guild");
        tradeMeta.setLore(Arrays.asList(new String[] {ChatColor.GOLD + " " + Plugin.getInstance().getConfig().getInt("guild-cost") + " " + ChatColor.GRAY + "coins"}));
        trade.setItemMeta(tradeMeta);
        GuildShopIcon tradeIcon = new GuildShopIcon(Plugin.getInstance().getConfig().getInt("guild-cost"), CurrencyUtil.goldCoin(), trade, new GuildShopBuyCondition() {
            @Override
            public boolean canBuy(Player player) {
                if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
                    if (!Plugin.getPlayersCreatingGuild().contains(event.getClicker().getUniqueId())) {
                        event.getClicker().sendMessage(ChatColor.YELLOW + "Creating a guild will cost you " + Plugin.GUILD_COST + " gold. To confirm or cancel the purchasing of this guild, type " + ChatColor.GOLD + "/guild confirm" + ChatColor.YELLOW + " or " + ChatColor.GOLD + "/guild cancel" + ChatColor.YELLOW + " in chat.");
                        Plugin.getPlayersCreatingGuild().add(event.getClicker().getUniqueId());
                    } else {
                        event.getClicker().sendMessage(ChatColor.YELLOW + "Type " + ChatColor.GOLD + "/guild confirm" + ChatColor.YELLOW + " or " + ChatColor.GOLD + "/guild cancel" + ChatColor.YELLOW + " in chat to confirm/cancel the creation of your guild.");
                    }
                } else {
                    event.getClicker().sendMessage(ChatColor.YELLOW + "You cannot create a guild because you are already in one!");
                }
            }
            @Override
            public String getDeniedMessage() {
                return ChatColor.YELLOW + "You cannot create a guild because you are already in one, or you are currently creating a guild! ";
            }
        }, new GuildShopBuyRunnable() {
            @Override
            public void run(Player player) {
                RunicGuildsAPI.createGuild(player.getUniqueId(), )
            }
        });
        tradeIcon.setRemovePayment(false);
        this.trades.put(0, tradeIcon);
    }

    @Override
    public Map<Integer, GuildShopIcon> getTrades() {
        return null;
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
        return this.npcs;
    }

    @Override
    public String getName() {
        return "Guild Herald";
    }

}