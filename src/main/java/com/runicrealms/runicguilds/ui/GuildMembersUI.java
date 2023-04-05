package com.runicrealms.runicguilds.ui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.utilities.GUIUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.guild.RankCompare;
import com.runicrealms.runicguilds.model.GuildInfo;
import com.runicrealms.runicguilds.model.GuildUUID;
import com.runicrealms.runicguilds.model.MemberData;
import com.runicrealms.runicguilds.util.GuildUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;

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
     * Opens the inventory associated w/ this GUI, ordering perks
     */
    private void openMenu() {
        this.inventory.clear();
        GUIUtil.fillInventoryBorders(this.inventory);
        this.inventory.setItem(0, GUIUtil.BACK_BUTTON);
        this.inventory.setItem(8, GUIUtil.CLOSE_BUTTON);
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player.getUniqueId());
        GuildUUID guildUUID = guildInfo.getGuildUUID();
        // Load guild members as a CompletableFuture
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            CompletableFuture<HashMap<UUID, MemberData>> future = RunicGuilds.getDataAPI().loadGuildMembers(guildUUID, jedis);
            future.whenComplete((HashMap<UUID, MemberData> memberDataMap, Throwable ex) -> {
                if (ex != null) {
                    Bukkit.getLogger().log(Level.SEVERE, "RunicGuilds failed to load on ui");
                    ex.printStackTrace();
                } else {
                    List<MemberData> memberDataList = new ArrayList<>(memberDataMap.values());
                    memberDataList.sort(new RankCompare());
                    for (MemberData guildMember : memberDataList) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(guildMember.getUuid());
                        this.inventory.setItem(this.inventory.firstEmpty(), GuildUtil.guildMemberItem
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
}