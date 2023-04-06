package com.runicrealms.runicguilds.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.api.event.*;
import com.runicrealms.runicguilds.command.GuildCommandMapManager;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.model.GuildInfo;
import com.runicrealms.runicguilds.model.GuildUUID;
import com.runicrealms.runicguilds.model.MemberData;
import com.runicrealms.runicguilds.util.GuildUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Handles logic for what to do whenever a particular custom guild event is called
 */
public class GuildEventListener implements Listener {

    @EventHandler
    public void onGuildCreation(GuildCreationEvent event) {
        Player owner = Bukkit.getPlayer(event.getUuid());
        syncDisplays(owner);
    }

    /**
     * Handles logic for removing guild data from memory on guild disband
     */
    @EventHandler
    public void onGuildDisband(GuildDisbandEvent event) {
        // Set guild data to "None" in redis / mongo, close guild bank
        Player player = event.getWhoDisbanded();
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            CompletableFuture<GuildData> future = RunicGuilds.getDataAPI().loadGuildData(event.getGuildUUID(), jedis);
            future.whenComplete((GuildData guildData, Throwable ex) -> {
                if (ex != null) {
                    Bukkit.getLogger().log(Level.SEVERE, "RunicGuilds failed to disband guild " + event.getGuildUUID());
                    ex.printStackTrace();
                } else {
                    Map<UUID, MemberData> memberDataMap = guildData.getMemberDataMap();
                    for (MemberData memberData : memberDataMap.values()) {
//                RunicGuilds.getGuildsAPI().setJedisGuild(member.getUUID(), "None", jedis);
                        // todo: handle jedis guild
                        Player playerMember = Bukkit.getPlayer(memberData.getUuid());
                        if (playerMember == null) continue;
//                        if (GuildBankUtil.isViewingBank(member.getUUID())) {
//                            GuildBankUtil.close(playerMember);
//                            // todo: pub/sub
//                        }
                    }
                    // todo: remove guild for owner
//                    RunicGuilds.getGuildsAPI().setJedisGuild(guild.getOwner().getUUID(), "None", jedis);


//                    if (GuildBankUtil.isViewingBank(guild.getOwner().getUUID())) {
//                        Player playerOwner = Bukkit.getPlayer(guild.getOwner().getUUID());
//                        if (playerOwner != null)
//                            GuildBankUtil.close(playerOwner);
//                    }

                    // Remove from jedis, mongo, and memory
                    // todo: guildData.removeFromJedis();
                    // todo: removeFromMongo
                    player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Successfully disbanded guild."));
                    GuildCommandMapManager.getDisbanding().remove(player.getUniqueId());
                    syncDisplays(event.getWhoDisbanded());
                    for (UUID uuid : memberDataMap.keySet()) {
                        Player playerMember = Bukkit.getPlayer(uuid);
                        if (playerMember == null) continue;
                        syncDisplays(playerMember);
                    }
                }
            });
        }
    }

    @EventHandler
    public void onGuildInvitationAccept(GuildInvitationAcceptedEvent event) {
        Player whoWasInvited = Bukkit.getPlayer(event.getInvited());
        syncDisplays(whoWasInvited);
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            syncMemberDisplays(event.getGuildUUID(), jedis);
//            event.getGuildData().writeToJedis(jedis);
            // todo: player guild jedis key?
        }
    }

    @EventHandler
    public void onGuildKick(GuildMemberKickedEvent event) {
        OfflinePlayer whoWasKicked = Bukkit.getOfflinePlayer(event.getKicked());
        RunicGuilds.getGuildsAPI().removeGuildMember(event.getGuildUUID(), whoWasKicked.getUniqueId());
        if (whoWasKicked.isOnline()) {
            Player player = whoWasKicked.getPlayer();
            assert player != null;
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + ChatColor.RED + "You have been kicked from your guild!"));
            syncDisplays(player);
        }
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            syncMemberDisplays(event.getGuildUUID(), jedis);
//            RunicGuilds.getGuildsAPI().setJedisGuild(whoWasKicked.getUniqueId(), "None", jedis);
            // todo: player guild jedis key?
        }
    }

    @EventHandler
    public void onGuildLeave(GuildMemberLeaveEvent event) {
        Player whoLeft = Bukkit.getPlayer(event.getMember());
        syncDisplays(whoLeft);
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            syncMemberDisplays(event.getGuildUUID(), jedis);
        }
    }

    /**
     * Updates player score on change, recalculates guild's entire score
     */
    @EventHandler
    public void onGuildScoreChange(GuildScoreChangeEvent event) {
        MemberData member = event.getMemberData();
        int score = member.getScore();
        member.setScore(score + event.getScore());
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            event.getMemberData().writeToJedis(event.getGuildUUID(), event.getMemberData().getUuid(), jedis);
        }
    }

    @EventHandler
    public void onGuildTransfer(GuildOwnershipTransferEvent event) {
        Player oldOwner = Bukkit.getPlayer(event.getOldOwner());
        syncDisplays(oldOwner);
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            syncMemberDisplays(event.getGuildUUID(), jedis);
        }
    }

    @EventHandler(priority = EventPriority.HIGH) // late
    public void onJoin(CharacterSelectEvent event) {
        syncDisplays(event.getPlayer());
    }

    /**
     * Clears player from in-memory command maps
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        GuildCommandMapManager.getTransferOwnership().remove(event.getPlayer().getUniqueId());
        GuildCommandMapManager.getDisbanding().remove(event.getPlayer().getUniqueId());
        RunicGuilds.getPlayersCreatingGuild().remove(event.getPlayer().getUniqueId());
    }

    /**
     * Sync displays ensures that redis and the player's session data properly reflect changes in the player's
     * guild during play time
     * <p>
     * Also syncs scoreboards and tab
     *
     * @param player to sync
     */
    private void syncDisplays(Player player) {
        if (player == null) return;
        GuildInfo guild = RunicGuilds.getDataAPI().getGuildInfo(player.getUniqueId());
//        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
//            if (guild != null) {
//                RunicGuilds.getGuildsAPI().setJedisGuild(player.getUniqueId(), guild.getGuildName(), jedis);
//            } else {
//                RunicGuilds.getGuildsAPI().setJedisGuild(player.getUniqueId(), "None", jedis);
//            }
//        }
        // todo: update the player's jedis tag
        RunicCore.getScoreboardAPI().updatePlayerScoreboard(player);
        GuildUtil.updateGuildTabColumn(player);
    }

    /**
     * ?
     *
     * @param guildUUID
     * @param jedis
     */
    private void syncMemberDisplays(GuildUUID guildUUID, Jedis jedis) {
        CompletableFuture<HashMap<UUID, MemberData>> future = RunicGuilds.getDataAPI().loadGuildMembers(guildUUID, jedis);
        future.whenComplete((HashMap<UUID, MemberData> memberDataMap, Throwable ex) -> {
            if (ex != null) {
                Bukkit.getLogger().log(Level.SEVERE, "RunicGuilds failed sync member displays");
                ex.printStackTrace();
            } else {
                List<MemberData> memberDataList = new ArrayList<>(memberDataMap.values());
                for (MemberData member : memberDataList) {
                    Player playerMember = Bukkit.getPlayer(member.getUuid());
                    if (playerMember == null) continue;
                    syncDisplays(playerMember);
                }
            }
        });
    }
}
