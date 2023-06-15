package com.runicrealms.runicguilds.listener;

import co.aikar.taskchain.TaskChain;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.model.CorePlayerData;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import com.runicrealms.runicguilds.GuildManager;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.api.event.GiveGuildEXPEvent;
import com.runicrealms.runicguilds.api.event.GuildCreationEvent;
import com.runicrealms.runicguilds.api.event.GuildDisbandEvent;
import com.runicrealms.runicguilds.api.event.GuildInvitationAcceptedEvent;
import com.runicrealms.runicguilds.api.event.GuildMemberDemotedEvent;
import com.runicrealms.runicguilds.api.event.GuildMemberKickedEvent;
import com.runicrealms.runicguilds.api.event.GuildMemberLeaveEvent;
import com.runicrealms.runicguilds.api.event.GuildMemberPromotedEvent;
import com.runicrealms.runicguilds.api.event.GuildOwnershipTransferEvent;
import com.runicrealms.runicguilds.api.event.GuildScoreChangeEvent;
import com.runicrealms.runicguilds.command.GuildCommandMapManager;
import com.runicrealms.runicguilds.guild.GuildRank;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.model.GuildDataField;
import com.runicrealms.runicguilds.model.GuildInfo;
import com.runicrealms.runicguilds.model.MemberData;
import com.runicrealms.runicguilds.util.GuildBankUtil;
import com.runicrealms.runicguilds.util.GuildUtil;
import com.runicrealms.runicguilds.util.TaskChainUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Map;
import java.util.Set;
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
        // 1. Delete from Mongo
        Query query = new Query();
        query.addCriteria(Criteria.where(GuildDataField.GUILD_UUID.getField()).is(guildData.getUUID()));
        MongoTemplate mongoTemplate = RunicDatabase.getAPI().getDataAPI().getMongoTemplate();
        mongoTemplate.remove(query, GuildData.class);
        // 2. Delete from memory
        RunicGuilds.getDataAPI().getGuildInfoMap().remove(guildData.getUUID());
        // 3. Final cleanup
        for (MemberData memberData : memberDataMap.values()) {
            Player playerMember = Bukkit.getPlayer(memberData.getUuid());
            if (playerMember == null) continue;
            if (GuildBankUtil.isViewingBank(memberData.getUuid())) {
                GuildBankUtil.close(playerMember);
            }
        }
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
                    GuildData guildData = RunicGuilds.getDataAPI().loadGuildData(event.getUUID());
                    disbandGuild(guildData, player);
                    return guildData;
                })
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicGuilds failed to load guild data!")
                .syncLast(guildData -> {
                    if (player != null) {
                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Successfully disbanded guild."));
                        GuildCommandMapManager.getDisbanding().remove(player.getUniqueId());
                    }
                })
                .execute();
    }

    @EventHandler
    public void onGuildExp(GiveGuildEXPEvent event) {
        if (event.isCancelled()) return;
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(event.getUUID());
        GuildStage currentStage = GuildStage.getFromExp(guildInfo.getExp());
        guildInfo.setExp(guildInfo.getExp() + event.getAmount());
        // Get the guild data async and update
        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> RunicGuilds.getDataAPI().loadGuildData(event.getUUID()))
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicGuilds failed to give exp!")
                .syncLast(guildData -> {
                    guildData.setExp(guildInfo.getExp());
                    GuildStage newStage = GuildStage.getFromExp(guildInfo.getExp());
                    if (currentStage == null || newStage == null) return;
                    if (currentStage != newStage) { // Stage upgrade!
                        for (UUID memberUuid : guildData.getMemberDataMap().keySet()) {
                            Player online = Bukkit.getPlayer(memberUuid);
                            if (online == null) continue; // Player offline
                            online.playSound(online.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 0.5f);
                            online.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Your guild has advanced a stage! " +
                                    guildInfo.getName() + " is now stage " + newStage.getName() + "!"));
                            if (newStage.getStageReward().getMessage().equalsIgnoreCase("")) continue;
                            online.sendMessage(ColorUtil.format(GuildUtil.PREFIX + ChatColor.GREEN + newStage.getStageReward().getMessage()));
                        }
                    }
                    RunicGuilds.getGuildWriteOperation().updateGuildData
                            (
                                    guildInfo.getUUID(),
                                    "exp",
                                    guildData.getExp(),
                                    () -> {
                                        for (UUID memberUuid : guildData.getMemberDataMap().keySet()) {
                                            Player online = Bukkit.getPlayer(memberUuid);
                                            if (online == null) continue; // Player offline
                                            online.playSound(online.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
                                            online.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Your guild has received " + event.getAmount() + " experience!"));
                                        }
                                    }
                            );
                })
                .execute();
    }

    @EventHandler
    public void onGuildInvitationAccept(GuildInvitationAcceptedEvent event) {
        Player whoWasInvited = Bukkit.getPlayer(event.getInvited());
        if (whoWasInvited != null) {
            syncDisplays(whoWasInvited);
        }
        syncMemberDisplays(event.getUUID());
    }

    @EventHandler
    public void onGuildKick(GuildMemberKickedEvent event) {
        Player whoWasKicked = Bukkit.getPlayer(event.getKicked());
        GuildData guildData = event.guildData();
        RunicGuilds.getGuildWriteOperation().updateGuildData
                (
                        guildData.getUUID(),
                        "memberDataMap",
                        guildData.getMemberDataMap(),
                        () -> {
                            // Remove from in-memory store
                            GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(guildData.getUUID());
                            if (guildInfo != null) {
                                guildInfo.getMembersUuids().remove(event.getKicked());
                            }
                            if (whoWasKicked != null) {
                                whoWasKicked.sendMessage(ColorUtil.format(GuildUtil.PREFIX + ChatColor.RED + "You have been kicked from your guild!"));
                                syncDisplays(whoWasKicked);
                            }
                            syncMemberDisplays(guildData.getUUID());
                            Player player = Bukkit.getPlayer(event.getKicker());
                            if (player != null) {
                                player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Removed player from the guild!"));
                            }
                        }
                );
    }

    @EventHandler
    public void onGuildLeave(GuildMemberLeaveEvent event) {
        GuildData guildData = event.getGuildData();
        guildData.getMemberDataMap().remove(event.getWhoLeft().getUniqueId());
        RunicGuilds.getGuildWriteOperation().updateGuildData
                (
                        guildData.getUUID(),
                        "memberDataMap",
                        guildData.getMemberDataMap(),
                        () -> {
                            // Remove from in-memory store
                            GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(guildData.getUUID());
                            if (guildInfo != null) {
                                guildInfo.getMembersUuids().remove(event.getWhoLeft().getUniqueId());
                            }
                            event.getWhoLeft().sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You have left your guild."));
                            syncDisplays(event.getWhoLeft());
                            syncMemberDisplays(event.getUUID());
                        }
                );
    }

    /**
     * Updates player score on change, recalculates guild's entire score
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onGuildScoreChange(GuildScoreChangeEvent event) {
        MemberData memberData = event.getMemberData();
        int score = memberData.getScore();
        memberData.setScore(score + event.getScore());
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(event.getUUID());
        // Load guild data async then recalculate score
        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> RunicGuilds.getDataAPI().loadGuildData(guildInfo.getUUID()))
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "GuildScoreChangeEvent failed to load!")
                .syncLast(guildData -> {
                    // Update MongoDB
                    guildData.getMemberDataMap().put(event.getMemberData().getUuid(), memberData);
                    RunicGuilds.getGuildWriteOperation().updateGuildData
                            (
                                    guildInfo.getUUID(),
                                    "memberDataMap",
                                    guildData.getMemberDataMap(),
                                    () -> {
                                        // Update in-memory cache
                                        guildInfo.setScore(guildData.calculateGuildScore());
                                        Player player = Bukkit.getPlayer(event.getMemberData().getUuid());
                                        if (player != null) {
                                            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                                    GuildUtil.PREFIX + ChatColor.GREEN + "You received " +
                                                            ChatColor.GOLD + ChatColor.BOLD + event.getScore() + ChatColor.GREEN + " guild score!"));
                                        }
                                    }
                            );
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
                    GuildData guildData = RunicGuilds.getDataAPI().loadGuildData(guildInfo.getUUID());
                    if (guildData == null) {
                        Bukkit.getLogger().severe("Guild ownership transfer failed!");
                        oldOwner.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "&cThere was an error processing this command!"));
                        return null;
                    }
                    MemberData oldOwnerData = guildData.getMemberDataMap().get(guildData.getOwnerUuid());
                    MemberData newOwnerData = guildData.getMemberDataMap().get(event.getNewOwner().getUniqueId());
                    if (newOwnerData == null) {
                        event.getOldOwner().sendMessage(GuildUtil.PREFIX + "Error: new owner data could not be found!");
                        return null;
                    }
                    oldOwnerData.setRank(GuildRank.OFFICER);
                    newOwnerData.setRank(GuildRank.OWNER);
                    guildData.getMemberDataMap().put(oldOwnerData.getUuid(), oldOwnerData);
                    guildData.getMemberDataMap().put(newOwnerData.getUuid(), newOwnerData);
                    return guildData;
                })
                .abortIfNull()
                .syncLast(guildData -> {
                    guildInfo.setOwnerUuid(event.getNewOwner().getUniqueId());
                    RunicGuilds.getGuildWriteOperation().updateGuildData
                            (
                                    guildInfo.getUUID(),
                                    "memberDataMap",
                                    guildData.getMemberDataMap(),
                                    () -> {
                                        syncDisplays(oldOwner);
                                        syncMemberDisplays(event.getUUID());
                                        oldOwner.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Successfully transferred guild ownership. You have been demoted to officer."));
                                        event.getNewOwner().sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You are now the owner of " + guildData.getName() + "!"));
                                    }
                            );
                })
                .execute();
    }

    @EventHandler(priority = EventPriority.HIGHEST) // late
    public void onJoin(CharacterLoadedEvent event) {
        // Ensure player is mapped to their guild in-memory
        UUID playerUuid = event.getPlayer().getUniqueId();
        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> {
                    String guildName = RunicGuilds.getDataAPI().loadGuildForPlayer(playerUuid);
                    // Update last known name on login
                    if (guildName != null) {
                        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(guildName);
                        if (guildInfo != null) {
                            GuildData guildData = RunicGuilds.getDataAPI().loadGuildData(guildInfo.getUUID());
                            MemberData memberData = guildData.getMemberDataMap().get(playerUuid);
                            memberData.setLastKnownName(event.getPlayer().getName());
                            guildData.getMemberDataMap().put(playerUuid, memberData);
                            // Update last known name in mongo on a different TaskChain
                            RunicGuilds.getGuildWriteOperation().updateGuildData
                                    (
                                            guildInfo.getUUID(),
                                            "memberDataMap",
                                            guildData.getMemberDataMap(),
                                            () -> {
                                            }
                                    );
                        }
                    }
                    return guildName;
                })
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicGuilds failed to load guild data!")
                .syncLast(guildName -> {
                            // Update their 'guild' quick lookup tag in core
                            CorePlayerData corePlayerData = (CorePlayerData) event.getCharacterSelectEvent().getSessionDataMongo();
                            corePlayerData.setGuild(guildName);
                            // Sync scoreboard, tab
                            syncDisplays(event.getPlayer());
                        }
                )
                .execute();
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
        if (player != null) {
            GuildManager.updateGuildTab(player);
            RunicCore.getScoreboardAPI().updatePlayerScoreboard(player);
        }
    }

    /**
     * Syncs the scoreboard/tab for all guild members
     *
     * @param guildUUID of the guild to sync
     */
    private void syncMemberDisplays(UUID guildUUID) {
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(guildUUID);
        Set<UUID> members = guildInfo.getMembersUuids();
        Bukkit.getScheduler().runTaskAsynchronously(RunicGuilds.getInstance(), () -> {
            for (UUID memberUuid : members) {
                Player playerMember = Bukkit.getPlayer(memberUuid);
                if (playerMember == null) continue; // Player must be online
                syncDisplays(playerMember);
            }
        });
    }
}
