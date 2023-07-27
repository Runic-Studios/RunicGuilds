package com.runicrealms.plugin.runicguilds.shop;

import com.runicrealms.plugin.common.util.ChatUtils;
import com.runicrealms.plugin.item.shops.RunicItemRunnable;
import com.runicrealms.plugin.item.shops.RunicShopGeneric;
import com.runicrealms.plugin.item.shops.RunicShopItem;
import com.runicrealms.plugin.runicguilds.RunicGuilds;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GuildShopManager {

    public GuildShopManager() {
        loadGuildHeraldShop();
    }

    /**
     * Loads the shop which allows players to purchase a guild
     */
    public void loadGuildHeraldShop() {
        ArrayList<RunicShopItem> shopItems = new ArrayList<>();
        ItemStack purchaseGuildItemStack = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta tradeMeta = purchaseGuildItemStack.getItemMeta();
        if (tradeMeta != null) {
            tradeMeta.setDisplayName(ChatColor.GOLD + String.valueOf(ChatColor.BOLD) + "Guild Charter");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.addAll(ChatUtils.formattedText
                    (
                            "&aPurchase a guild &7and become a guild master! " +
                                    "Earn guild score and make your mark on the realm!"
                    ));
            tradeMeta.setLore(lore);
            tradeMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            purchaseGuildItemStack.setItemMeta(tradeMeta);
        }
        shopItems.add(new RunicShopItem(1500, purchaseGuildItemStack, runGuildHeraldBuy()));
        shopItems.forEach(runicShopItem -> runicShopItem.setRemovePayment(false));
        new RunicShopGeneric(45, ChatColor.GOLD + "Guild Herald", RunicGuilds.GUILD_HERALDS, shopItems, new int[]{13});
    }

    private RunicItemRunnable runGuildHeraldBuy() {
        return player -> {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            if (!RunicGuilds.getGuildsAPI().isInGuild(player)) {
                if (!RunicGuilds.getPlayersCreatingGuild().contains(player.getUniqueId())) {
                    player.sendMessage
                            (ChatColor.YELLOW + "Creating a guild will cost you " + RunicGuilds.GUILD_COST +
                                    " gold. To confirm or cancel the purchasing of this guild, type " + ChatColor.GOLD +
                                    "/guild confirm" + ChatColor.YELLOW + " or " + ChatColor.GOLD + "/guild cancel"
                                    + ChatColor.YELLOW + " in chat.");
                    RunicGuilds.getPlayersCreatingGuild().add(player.getUniqueId());
                } else {
                    player.sendMessage(
                            ChatColor.YELLOW + "Type " + ChatColor.GOLD + "/guild confirm" +
                                    ChatColor.YELLOW + " or " + ChatColor.GOLD + "/guild cancel" +
                                    ChatColor.YELLOW + " in chat to confirm or cancel the creation of your guild.");
                }
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                player.sendMessage(ChatColor.YELLOW + "You're already in a guild!");
            }
        };
    }

}
