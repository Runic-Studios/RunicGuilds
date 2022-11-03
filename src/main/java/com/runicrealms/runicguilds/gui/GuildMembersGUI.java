package com.runicrealms.runicguilds.gui;

import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.GUIUtil;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.GuildMember;
import com.runicrealms.runicguilds.guild.RankCompare;
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

public class GuildMembersGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Player player;
    private final Guild guild;

    /**
     * Builds an info UI that can be used to see info about a guild
     *
     * @param player who entered the command
     * @param guild  of the player
     */
    public GuildMembersGUI(Player player, Guild guild) {
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
        this.inventory.setItem(0, GUIUtil.backButton());
        this.inventory.setItem(8, GUIUtil.closeButton());
        List<GuildMember> sortedByRank = new ArrayList<>(this.guild.getMembersWithOwner());
        sortedByRank.sort(new RankCompare());
        for (GuildMember guildMember : sortedByRank) {
            OfflinePlayer member = Bukkit.getOfflinePlayer(guildMember.getUUID());
            this.inventory.setItem(this.inventory.firstEmpty(), guildMemberItem
                    (
                            member.getPlayer(),
                            Material.PLAYER_HEAD,
                            ChatColor.GOLD + guildMember.getLastKnownName(),
                            ChatColor.YELLOW + "Rank: " + guildMember.getRank() +
                                    "\n" + ChatColor.YELLOW + "Score: [" + guildMember.getScore() + "]"
                    ));
        }
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