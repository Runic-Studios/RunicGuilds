package com.runicrealms.runicguilds.ui;

import com.runicrealms.libs.taskchain.TaskChain;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.utilities.GUIUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.guild.RankCompare;
import com.runicrealms.runicguilds.model.GuildInfo;
import com.runicrealms.runicguilds.model.GuildUUID;
import com.runicrealms.runicguilds.model.MemberData;
import com.runicrealms.runicguilds.util.GuildUtil;
import com.runicrealms.runicguilds.util.TaskChainUtil;
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
        String name = RunicGuilds.getDataAPI().getGuildInfo(player).getName();
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
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        GuildUUID guildUUID = guildInfo.getGuildUUID();
        // Load members async, populate inventory async, then open inv sync
        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> {
                    try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
                        HashMap<UUID, MemberData> guildMembers = RunicGuilds.getDataAPI().loadGuildMembers(guildUUID, jedis);
                        List<MemberData> memberDataList = new ArrayList<>(guildMembers.values());
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
                        return guildMembers;
                    }
                })
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicGuilds failed to load member data!")
                .syncLast(memberData -> {
                    // Open the UI!
                    player.openInventory(this.inventory);
                })
                .execute();
    }
}