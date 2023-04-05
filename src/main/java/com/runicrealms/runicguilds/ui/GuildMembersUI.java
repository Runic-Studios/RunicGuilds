package com.runicrealms.runicguilds.ui;

import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.GUIUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.guild.RankCompare;
import com.runicrealms.runicguilds.model.GuildInfo;
import com.runicrealms.runicguilds.model.GuildUUID;
import com.runicrealms.runicguilds.model.MemberData;
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
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class GuildMembersUI implements InventoryHolder {
    private final Inventory inventory;
    private final Player player;

    /**
     * Builds an info UI that can be used to see info about a guild
     *
     * @param player who entered the command
     */
    public GuildMembersUI(Player player) {
        this.player = player;
        String name = RunicGuilds.getDataAPI().getGuildInfo(player.getUniqueId()).getName();
        this.inventory = Bukkit.createInventory(this, 54, ChatColor.GOLD + name);
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

    /**
     * @param player      the item represents
     * @param name        of the player
     * @param description of this itemStack
     * @return an ItemStack to display in the ui menu
     */
    private ItemStack guildMemberItem(Player player, String name, String description) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        SkullMeta skullMeta = (SkullMeta) meta;
        skullMeta.setOwningPlayer(player);

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
     * Opens the inventory associated w/ this GUI, ordering perks
     */
    private void openMenu() {
        this.inventory.clear();
        GUIUtil.fillInventoryBorders(this.inventory);
        this.inventory.setItem(0, GUIUtil.BACK_BUTTON);
        this.inventory.setItem(8, GUIUtil.CLOSE_BUTTON);
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player.getUniqueId());
        GuildUUID guildUUID = guildInfo.getGuildUUID();
        CompletableFuture<HashMap<UUID, MemberData>> future = RunicGuilds.getDataAPI().loadGuildMembers(guildUUID);


        future.whenComplete((HashMap<UUID, MemberData> memberDataMap, Throwable ex) -> {
            if (ex != null) {
                Bukkit.getLogger().log(Level.SEVERE, "RunicGuilds failed to load on ui");
                ex.printStackTrace();
            } else {
                List<MemberData> memberDataList = new ArrayList<>(memberDataMap.values());
                memberDataList.sort(new RankCompare());
                for (MemberData guildMember : memberDataList) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(guildMember.getUuid());
                    this.inventory.setItem(this.inventory.firstEmpty(), guildMemberItem
                            (
                                    offlinePlayer.getPlayer(),
                                    ChatColor.GOLD + offlinePlayer.getName(), // Last known name
                                    ChatColor.YELLOW + "Rank: " + guildMember.getRank() +
                                            "\n" + ChatColor.YELLOW + "Score: [" + guildMember.getScore() + "]"
                            ));
                }
            }
        });
    }
}