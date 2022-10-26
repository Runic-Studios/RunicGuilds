package com.runicrealms.runicguilds.shop;

import com.runicrealms.plugin.item.shops.RunicItemRunnable;
import com.runicrealms.plugin.item.shops.RunicShopGeneric;
import com.runicrealms.plugin.item.shops.RunicShopItem;
import com.runicrealms.plugin.item.util.ItemRemover;
import com.runicrealms.plugin.utilities.ChatUtils;
import com.runicrealms.plugin.utilities.CurrencyUtil;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.api.RunicGuildsAPI;
import com.runicrealms.runicguilds.util.GuildUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class GuildShopManager {

    public GuildShopManager() {
        getGuildHeraldShop();
        getGuildVendorShop();
    }

    public RunicShopGeneric getGuildHeraldShop() {
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();
        ItemStack purchaseGuildItemStack = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta tradeMeta = purchaseGuildItemStack.getItemMeta();
        if (tradeMeta != null) {
            tradeMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Guild Charter");
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
        shopItems.add(new RunicShopItem(1500, "Coin", purchaseGuildItemStack, runGuildHeraldBuy()));
        shopItems.forEach(runicShopItem -> runicShopItem.setRemovePayment(false));
        return new RunicShopGeneric(45, ChatColor.GOLD + "Guild Herald", Plugin.GUILD_HERALDS, shopItems, new int[]{13});
    }

    private RunicItemRunnable runGuildHeraldBuy() {
        return player -> {
            if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
                if (!Plugin.getPlayersCreatingGuild().contains(player.getUniqueId())) {
                    player.sendMessage
                            (ChatColor.YELLOW + "Creating a guild will cost you " + Plugin.GUILD_COST +
                                    " gold. To confirm or cancel the purchasing of this guild, type " + ChatColor.GOLD +
                                    "/guild confirm" + ChatColor.YELLOW + " or " + ChatColor.GOLD + "/guild cancel"
                                    + ChatColor.YELLOW + " in chat.");
                    Plugin.getPlayersCreatingGuild().add(player.getUniqueId());
                } else {
                    player.sendMessage(
                            ChatColor.YELLOW + "Type " + ChatColor.GOLD + "/guild confirm" +
                                    ChatColor.YELLOW + " or " + ChatColor.GOLD + "/guild cancel" +
                                    ChatColor.YELLOW + " in chat to confirm or cancel the creation of your guild.");
                }
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                player.sendMessage(ChatColor.YELLOW + "You cannot create a guild because you are already in one!");
            }
        };
    }

    private final ItemStack swiftBlackSteed = RunicItemsAPI.generateItemFromTemplate("swift-black-steed").generateItem();
    private final ItemStack swiftWhiteStallion = RunicItemsAPI.generateItemFromTemplate("swift-white-stallion").generateItem();

    public RunicShopGeneric getGuildVendorShop() {
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();
        shopItems.add(new RunicShopItem(2000, "Coin", swiftBlackSteed, runGuildVendorBuy(swiftBlackSteed, 2000, 500)));
        shopItems.add(new RunicShopItem(2000, "Coin", swiftWhiteStallion, runGuildVendorBuy(swiftWhiteStallion, 2000, 500)));
        shopItems.forEach(runicShopItem -> runicShopItem.setRemovePayment(false));
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Guild Vendor", Plugin.GUILD_VENDORS, shopItems);
    }

    /**
     * Handles shop logic for Guild Vendor, which requires a minimum amount of guild points
     * Note, we already check if player has enough gold, so no need to do it here
     *
     * @param itemToPurchase     the item that will be bought
     * @param price              the price of the item (in coins)
     * @param minimumGuildPoints the guild points the player must have contributed to purchase the item
     * @return a RunicItemRunnable
     */
    private RunicItemRunnable runGuildVendorBuy(ItemStack itemToPurchase, int price, int minimumGuildPoints) {
        return player -> {
            if (RunicGuildsAPI.getGuild(player.getUniqueId()) != null) {
                int score = RunicGuildsAPI.getGuild(player.getUniqueId()).getMember(player.getUniqueId()).getScore();
                if (score >= minimumGuildPoints) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                    player.sendMessage(ChatColor.GREEN + "You've purchased a Guild Vendor item!");
                    ItemRemover.takeItem(player, CurrencyUtil.goldCoin(), price);
                    RunicItemsAPI.addItem(player.getInventory(), itemToPurchase);
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                    player.sendMessage
                            (
                                    ChatColor.RED + "You must have contributed at least " +
                                            ChatColor.YELLOW + minimumGuildPoints + ChatColor.RED +
                                            " points to your guild to purchase this!"
                            );
                }
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                player.sendMessage
                        (
                                ChatColor.RED + "You must have contributed at least " +
                                        ChatColor.YELLOW + minimumGuildPoints + ChatColor.RED +
                                        " points to your guild to purchase this!"
                        );
            }
        };
    }

}
