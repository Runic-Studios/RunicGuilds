package com.runicrealms.runicguilds.listener;

import com.runicrealms.libs.taskchain.TaskChain;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterLoadedEvent;
import com.runicrealms.plugin.model.CorePlayerData;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.api.event.*;
import com.runicrealms.runicguilds.command.GuildCommandMapManager;
import com.runicrealms.runicguilds.guild.GuildRank;
import com.runicrealms.runicguilds.model.*;
import com.runicrealms.runicguilds.util.GuildUtil;
import com.runicrealms.runicguilds.util.TaskChainUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles logic for what to do whenever a particular custom guild event is called
 */
public class GuildEventListener implements Listener {

    /**
     * Disbands the guild, removing its data from Redis, Mongo, and local memory
     */
    private void disbandGuild(GuildData guildData, Player player) {
        // Store a reference to the guild members
        Map<UUID, MemberData> memberDataMap = guildData.getMemberDataMap();
        // 1. Delete from Redis, remove Guild from 'markedForSave'
        String rootKey = GuildData.getJedisKey(guildData.getGuildUUID());
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            // Removes all sub-keys for the guild
            RunicCore.getRedisAPI().removeAllFromRedis(jedis, rootKey);
            jedis.del(rootKey);
            // Guild is no longer marked for save
            String database = RunicCore.getDataAPI().getMongoDatabase().getName();
            jedis.srem(database + ":markedForSave:guilds", String.valueOf(guildData.getGuildUUID().getUUID()));
        }
        // 2. Delete from Mongo
        Query query = new Query();
        query.addCriteria(Criteria.where(GuildDataField.GUILD_UUID.getField() + ".uuid").is(guildData.getGuildUUID().getUUID()));
        MongoTemplate mongoTemplate = RunicCore.getDataAPI().getMongoTemplate();
        mongoTemplate.remove(query, GuildData.class);
        // 3. Delete from memory
        RunicGuilds.getDataAPI().getGuildInfoMap().remove(guildData.getGuildUUID().getUUID());
        // 4. Final cleanup
        for (MemberData memberData : memberDataMap.values()) {
            RunicGuilds.getDataAPI().setGuildForPlayer(memberData.getUuid(), "None");
            Player playerMember = Bukkit.getPlayer(memberData.getUuid());
            if (playerMember == null) continue;
//                        if (GuildBankUtil.isViewingBank(member.getUUID())) {
//                            GuildBankUtil.close(playerMember);
//                            // todo: pub/sub
//                        }
        }
//                    if (GuildBankUtil.isViewingBank(guild.getOwner().getUUID())) {
//                        Player playerOwner = Bukkit.getPlayer(guild.getOwner().getUUID());
//                        if (playerOwner != null)
//                            GuildBankUtil.close(playerOwner);
//                    }
        // Sync visual displays
        syncDisplays(player);
        for (UUID uuid : memberDataMap.keySet()) {
            Player playerMember = Bukkit.getPlayer(uuid);
            if (playerMember == null) continue;
            syncDisplays(playerMember);
        }
    }

    @EventHandler
    public void onDemote(GuildMemberDemotedEvent event) {
        Player demoted = Bukkit.getPlayer(event.getDemoted());
        if (demoted == null) return; // Player offline
        demoted.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You have been demoted to rank " + event.getNewRank() + "!"));
    }

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
        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> {
                    GuildData guildData = RunicGuilds.getDataAPI().loadGuildData(event.getGuildUUID().getUUID());
                    disbandGuild(guildData, player);
                    return guildData;
                })
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicGuilds failed to load guild data!")
                .syncLast(guildData -> {
                    player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Successfully disbanded guild."));
                    GuildCommandMapManager.getDisbanding().remove(player.getUniqueId());
                })
                .execute();
    }

    @EventHandler
    public void onGuildExp(GiveGuildEXPEvent event) {
        if (event.isCancelled()) return;
        int amount = event.getAmount();
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(event.getGuildUUID());
        guildInfo.setExp(guildInfo.getExp() + event.getAmount());
        // Get the guild data async and update
        Bukkit.getScheduler().runTaskAsynchronously(RunicGuilds.getInstance(), () -> {
            GuildData guildDataNoBank = RunicGuilds.getDataAPI().loadGuildDataNoBank(guildInfo.getGuildUUID().getUUID());
            guildDataNoBank.setExp(amount);
            try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
                guildDataNoBank.writeToJedis(jedis);
            }
        });
    }

    @EventHandler
    public void onGuildInvitationAccept(GuildInvitationAcceptedEvent event) {
        Player whoWasInvited = Bukkit.getPlayer(event.getInvited());
        syncDisplays(whoWasInvited);
        syncMemberDisplays(event.getGuildUUID());
    }

    @EventHandler
    public void onGuildKick(GuildMemberKickedEvent event) {
//        OfflinePlayer whoWasKicked = Bukkit.getOfflinePlayer(event.getKicked());
//        Player target = Bukkit.getPlayer(event.getKicked());
//        if (target != null && GuildBankUtil.isViewingBank(target.getUniqueId())) {
//            GuildBankUtil.close(target);
//        }
//        RunicGuilds.getGuildsAPI().removeGuildMember(event.getGuildUUID(), whoWasKicked.getUniqueId());
//        if (whoWasKicked.isOnline()) {
//            Player player = whoWasKicked.getPlayer();
//            assert player != null;
//            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + ChatColor.RED + "You have been kicked from your guild!"));
//            syncDisplays(player);
//        }
//        syncMemberDisplays(event.getGuildUUID());
//        RunicGuilds.getDataAPI().setGuildForPlayer(whoWasKicked.getUniqueId(), "None");
//        Player player = Bukkit.getPlayer(event.getKicker());
//        if (player != null) {
//            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Removed player from the guild!"));
//        }
    }

    @EventHandler
    public void onGuildLeave(GuildMemberLeaveEvent event) {
        Player whoLeft = Bukkit.getPlayer(event.getMember());
        syncDisplays(whoLeft);
        syncMemberDisplays(event.getGuildUUID());
    }

    /**
     * Updates player score on change, recalculates guild's entire score
     */
    @EventHandler
    public void onGuildScoreChange(GuildScoreChangeEvent event) {
        MemberData member = event.getMemberData();
        int score = member.getScore();
        member.setScore(score + event.getScore());
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(event.getGuildUUID());
        // Load guild data async then recalculate score
        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> {
                    try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
                        event.getMemberData().writeToJedis(event.getGuildUUID(), event.getMemberData().getUuid(), jedis);
                    }
                    return RunicGuilds.getDataAPI().loadGuildDataNoBank(guildInfo.getGuildUUID().getUUID());
                })
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "GuildScoreChangeEvent failed to load!")
                .syncLast(guildDataNoBank -> {
                    guildInfo.setScore(guildDataNoBank.calculateGuildScore());
                })
                .execute();
    }

    @EventHandler
    public void onGuildTransfer(GuildOwnershipTransferEvent event) {
        Player oldOwner = event.getOldOwner();
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(oldOwner);
        // Load members async, populate inventory async, then open inv sync
        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> {
                    GuildData guildDataNoBank = RunicGuilds.getDataAPI().loadGuildDataNoBank(guildInfo.getGuildUUID().getUUID());
                    if (guildDataNoBank == null) {
                        Bukkit.getLogger().severe("Guild ownership transfer failed!");
                        oldOwner.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "&cThere was an error processing this command!"));
                        return null;
                    }
                    MemberData oldOwnerData = guildDataNoBank.getMemberDataMap().get(guildDataNoBank.getOwnerUuid());
                    MemberData newOwnerData = guildDataNoBank.getMemberDataMap().get(event.getNewOwner().getUniqueId());
                    if (newOwnerData == null) {
                        event.getOldOwner().sendMessage(GuildUtil.PREFIX + "Error: new owner data could not be found!");
                        return null;
                    }
                    oldOwnerData.setRank(GuildRank.OFFICER);
                    newOwnerData.setRank(GuildRank.OWNER);
                    try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
                        guildDataNoBank.writeToJedis(jedis);
                    }
                    return guildDataNoBank;
                })
                .abortIfNull()
                .syncLast(guildDataNoBank -> {
                    guildInfo.setOwnerUuid(event.getNewOwner().getUniqueId());
                    syncDisplays(oldOwner);
                    syncMemberDisplays(event.getGuildUUID());
                    oldOwner.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Successfully transferred guild ownership. You have been demoted to officer."));
                    event.getNewOwner().sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You are now the owner of " + guildDataNoBank.getName() + "!"));
                })
                .execute();
    }

    @EventHandler(priority = EventPriority.HIGHEST) // late
    public void onJoin(CharacterLoadedEvent event) {
        // Ensure player is mapped to their guild in-memory
        UUID uuid = event.getPlayer().getUniqueId();
        String guildName = RunicGuilds.getDataAPI().getGuildForPlayer(uuid);
        if (guildName != null && !guildName.equalsIgnoreCase("none")) {
            GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(guildName);
            RunicGuilds.getDataAPI().getPlayerToGuildMap().put(uuid, guildInfo.getGuildUUID().getUUID());
        }
        // Update their 'guild' quick lookup tag in core
        CorePlayerData corePlayerData = event.getCharacterSelectEvent().getCorePlayerData();
        corePlayerData.setGuild(guildName);
        // Sync scoreboard, tab
        syncDisplays(event.getPlayer());
    }

    @EventHandler
    public void onPromote(GuildMemberPromotedEvent event) {
        Player promoted = Bukkit.getPlayer(event.getPromoted());
        if (promoted == null) return; // Player offline
        promoted.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You have been promoted to rank " + event.getNewRank() + "!"));
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
        if (player == null) return; // Player went offline
//        Bukkit.broadcastMessage("syncing guild displays");
        String guildName = RunicGuilds.getDataAPI().getGuildForPlayer(player.getUniqueId());
//        Bukkit.broadcastMessage("guild name is " + guildName);
        if (guildName == null) {
            RunicGuilds.getDataAPI().setGuildForPlayer(player.getUniqueId(), "None");
        } else {
            GuildInfo guild = RunicGuilds.getDataAPI().getGuildInfo(guildName);
            if (guild != null) {
                RunicGuilds.getDataAPI().setGuildForPlayer(player.getUniqueId(), guild.getName());
            } else {
                RunicGuilds.getDataAPI().setGuildForPlayer(player.getUniqueId(), "None");
            }
        }
        RunicCore.getScoreboardAPI().updatePlayerScoreboard(player);
        GuildUtil.updateGuildTabColumn(player);
    }

    /**
     * Syncs the scoreboard/tab for all guild members
     *
     * @param guildUUID of the guild to sync
     */
    private void syncMemberDisplays(GuildUUID guildUUID) {
        Bukkit.getScheduler().runTaskAsynchronously(RunicGuilds.getInstance(), () -> {
            try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
                Map<UUID, MemberData> guildMembers = RunicGuilds.getDataAPI().loadGuildMembers(guildUUID, jedis);
                List<MemberData> memberDataList = new ArrayList<>(guildMembers.values());
                for (MemberData member : memberDataList) {
                    Player playerMember = Bukkit.getPlayer(member.getUuid());
                    if (playerMember == null) continue; // Player must be online
                    syncDisplays(playerMember);
                }
            }
        });
    }
}
