package com.runicrealms.runicguilds.ui;

import com.runicrealms.plugin.common.api.guilds.GuildStage;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.GUIUtil;
import com.runicrealms.plugin.common.util.OfflinePlayerUtil;
import com.runicrealms.runicguilds.model.GuildInfo;
import com.runicrealms.runicguilds.util.GuildUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GuildInfoUI implements InventoryHolder {
    private final GuildInfo guildInfo;
    private final Inventory inventory;
    private final Player player;

    /**
     * Builds an info UI that can be used to see info about a guild
     *
     * @param player    who entered the command
     * @param guildInfo of the guild
     */
    public GuildInfoUI(Player player, GuildInfo guildInfo) {
        this.player = player;
        this.guildInfo = guildInfo;
        this.inventory = Bukkit.createInventory(this, 54, ChatColor.GOLD + this.guildInfo.getName());
        openMenu();
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public Player getPlayer() {
        return this.player;
    }

    private ItemStack guildInfoItem(String ownerName) {
        ItemStack menuItem = new ItemStack(Material.IRON_HORSE_ARMOR);
        ItemMeta meta = menuItem.getItemMeta();
        if (meta == null) return menuItem;
        meta.setDisplayName(ChatColor.GOLD + "Guild Info");
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ColorUtil.format("&6&l" + guildInfo.getName()));
        lore.add(ColorUtil.format("&6Total Score: [" + guildInfo.getScore() + "]"));
        GuildStage stage = GuildStage.getFromExp(guildInfo.getExp());
        lore.add(ColorUtil.format("&eGuild Stage: [&f" + stage.getRank() + "&e/" + GuildStage.getMaxStage().getRank() + "]"));
        lore.add(ColorUtil.format("&eGuild Exp: " + guildInfo.getExp()));
        lore.add(ColorUtil.format("&eGuild Owner: " + ownerName));
        lore.add(ColorUtil.format("&eMax Members: " + stage.getMaxMembers()));
        lore.add("");
        lore.add(ColorUtil.format("&6Unlocked Guild Perks:"));
        lore.addAll(guildPerks());
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        menuItem.setItemMeta(meta);
        return menuItem;
    }

    /**
     * Get a list of perks that the guild has unlocked by leveling
     *
     * @return a list of string for UI
     */
    private List<String> guildPerks() {
        List<String> result = new ArrayList<>();
        for (GuildStage guildStage : GuildStage.values()) {
            if (guildStage.getStageReward().getMessage().equalsIgnoreCase("")) continue;
            if (guildStage.getExp() <= this.guildInfo.getExp())
                result.add(ChatColor.GREEN + "- " + guildStage.getStageReward().getFormattedReward());
        }
        if (result.isEmpty())
            result.add(ChatColor.GRAY + "- None");
        return result;
    }

    /**
     * Opens the inventory associated w/ this GUI, ordering perks
     */
    private void openMenu() {
        this.inventory.clear();
        GUIUtil.fillInventoryBorders(this.inventory);
        this.inventory.setItem(8, GUIUtil.CLOSE_BUTTON);
        OfflinePlayerUtil.getName(guildInfo.getOwnerUuid()).thenAcceptAsync(name -> {
            this.inventory.setItem(21, guildInfoItem(name));
            this.inventory.setItem(23, GuildUtil.guildMemberItem
                    (
                            this.guildInfo.getOwnerUuid(),
                            ChatColor.GOLD + "View Members",
                            ChatColor.GRAY + "View your guild members!"
                    ));
        });

    }

}