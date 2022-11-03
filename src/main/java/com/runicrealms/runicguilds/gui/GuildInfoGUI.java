package com.runicrealms.runicguilds.gui;

import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.GUIUtil;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GuildInfoGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Player player;
    private final Guild guild;

    /**
     * Builds an info UI that can be used to see info about a guild
     *
     * @param player who entered the command
     * @param guild  of the player
     */
    public GuildInfoGUI(Player player, Guild guild) {
        this.player = player;
        this.guild = guild;
        this.inventory = Bukkit.createInventory(this, 54, ChatColor.GOLD + this.guild.getGuildName());
        openMenu();
    }

    /**
     * Opens the inventory associated w/ this GUI, ordering perks
     */
    private void openMenu() {
        this.inventory.clear();
        GUIUtil.fillInventoryBorders(this.inventory);
        this.inventory.setItem(8, GUIUtil.closeButton());
        this.inventory.setItem(21, guildInfoItem());
        OfflinePlayer owner = Bukkit.getOfflinePlayer(this.guild.getOwner().getUUID());
        this.inventory.setItem(23, guildMemberItem
                (
                        owner.getPlayer(),
                        Material.PLAYER_HEAD,
                        ChatColor.GOLD + "View Members",
                        ChatColor.GRAY + "View your guild members!"
                ));
    }

    private ItemStack guildInfoItem() {
        ItemStack menuItem = new ItemStack(Material.IRON_HORSE_ARMOR);
        ItemMeta meta = menuItem.getItemMeta();
        if (meta == null) return menuItem;
        meta.setDisplayName(ChatColor.GOLD + "Guild Info");
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ColorUtil.format("&6&l" + guild.getGuildName()));
        lore.add(ColorUtil.format("&6Total Score: [" + guild.getScore() + "]"));
        lore.add(ColorUtil.format("&eGuild Stage: [&f" + guild.getGuildStage().getRank() + "&e/" + GuildStage.getMaxStage().getRank() + "]"));
        lore.add(ColorUtil.format("&eGuild Exp: " + guild.getGuildExp()));
        lore.add(ColorUtil.format("&eGuild Owner: " + guild.getOwner().getLastKnownName()));
        lore.add(ColorUtil.format("&eMax Members: " + guild.getGuildStage().getMaxMembers()));
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
     * @param player
     * @param material
     * @param name
     * @param description
     * @return
     */
    private ItemStack guildMemberItem(Player player, Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        if (material == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) meta;
            skullMeta.setOwningPlayer(player);
        }

        ArrayList<String> lore = new ArrayList<>();
        meta.setDisplayName(ColorUtil.format(name));
        String[] desc = description.split("\n");
        for (String line : desc) {
            lore.add(ColorUtil.format(line));
        }
        meta.setLore(lore);
        ((Damageable) meta).setDamage(5);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
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
            if (guildStage.getRank() <= this.guild.getGuildStage().getRank())
                result.add(ChatColor.GREEN + "- " + guildStage.getStageReward().getFormattedReward());
        }
        if (result.isEmpty())
            result.add(ChatColor.GRAY + "- None");
        return result;
    }


//    HashMap<GuildRank, StringBuilder> members = new HashMap<>();
//        for (
//    GuildMember member : guild.getMembers()) {
//        if (!members.containsKey(member.getRank())) {
//            members.put(member.getRank(), new StringBuilder());
//        }
//        members.get(member.getRank())
//                .append("&7[")
//                .append(member.getScore())
//                .append("] &e")
//                .append(member.getLastKnownName())
//                .append("&r, ");
//    }
//
//        for (GuildRank rank : GuildRank.values()) {
//        if (members.containsKey(rank)) {
//            player.sendMessage(ColorUtil.format("&6Guild " + rank.getPlural() + ": &r" + members.get(rank).substring(0, members.get(rank).toString().length() - 2)));
//        }
//    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Guild getGuild() {
        return guild;
    }
}