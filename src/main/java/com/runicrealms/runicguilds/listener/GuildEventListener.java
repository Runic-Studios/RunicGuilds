package com.runicrealms.runicguilds.listener;

import com.runicrealms.libs.taskchain.TaskChain;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterLoadedEvent;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.api.event.*;
import com.runicrealms.runicguilds.command.GuildCommandMapManager;
import com.runicrealms.runicguilds.guild.GuildRank;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.model.GuildInfo;
import com.runicrealms.runicguilds.model.GuildUUID;
import com.runicrealms.runicguilds.model.MemberData;
import com.runicrealms.runicguilds.util.GuildBankUtil;
import com.runicrealms.runicguilds.util.GuildUtil;
import com.runicrealms.runicguilds.util.TaskChainUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles logic for what to do whenever a particular custom guild event is called
 */
public class GuildEventListener implements Listener {

    private void disbandGuild(GuildData guildData, Player player) {
        Map<UUID, MemberData> memberDataMap = guildData.getMemberDataMap();
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

        // Remove from jedis, mongo, and memory
        // todo: guildData.removeFromJedis();
        // todo: removeFromMongo
        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Successfully disbanded guild."));
        GuildCommandMapManager.getDisbanding().remove(player.getUniqueId());
        syncDisplays(player);
        for (UUID uuid : memberDataMap.keySet()) {
            Player playerMember = Bukkit.getPlayer(uuid);
            if (playerMember == null) continue;
            syncDisplays(playerMember);
        }
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
                .asyncFirst(() -> RunicGuilds.getDataAPI().loadGuildData(event.getGuildUUID().getUUID()))
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicGuilds failed to load guild data!")
                .syncLast(guildData -> disbandGuild(guildData, player))
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
            try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
                GuildData guildDataNoBank = RunicGuilds.getDataAPI().loadGuildDataNoBank(guildInfo.getGuildUUID(), jedis);
                guildDataNoBank.setExp(amount);
                guildDataNoBank.writeToJedis(jedis);
            }
        });
    }

    @EventHandler
    public void onGuildInvitationAccept(GuildInvitationAcceptedEvent event) {
        Player whoWasInvited = Bukkit.getPlayer(event.getInvited());
        syncDisplays(whoWasInvited);
        syncMemberDisplays(event.getGuildUUID());
        // todo: player guild jedis key?
    }

    @EventHandler
    public void onGuildKick(GuildMemberKickedEvent event) {
        OfflinePlayer whoWasKicked = Bukkit.getOfflinePlayer(event.getKicked());
        Player target = Bukkit.getPlayer(event.getKicked());
        if (target != null && GuildBankUtil.isViewingBank(target.getUniqueId())) {
            GuildBankUtil.close(target);
        }
        RunicGuilds.getGuildsAPI().removeGuildMember(event.getGuildUUID(), whoWasKicked.getUniqueId());
        if (whoWasKicked.isOnline()) {
            Player player = whoWasKicked.getPlayer();
            assert player != null;
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + ChatColor.RED + "You have been kicked from your guild!"));
            syncDisplays(player);
        }
        syncMemberDisplays(event.getGuildUUID());
        RunicGuilds.getDataAPI().setGuildForPlayer(whoWasKicked.getUniqueId(), "None");
        Player player = Bukkit.getPlayer(event.getKicker());
        if (player != null) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Removed player from the guild!"));
        }
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
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            event.getMemberData().writeToJedis(event.getGuildUUID(), event.getMemberData().getUuid(), jedis);
        }
    }

    @EventHandler
    public void onGuildTransfer(GuildOwnershipTransferEvent event) {
        Player oldOwner = event.getOldOwner();
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(oldOwner); // GuildCommandMapManager.getInvites().get(event.getOldOwner().getUniqueId()
        // Load members async, populate inventory async, then open inv sync
        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> {
                    try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
                        return RunicGuilds.getDataAPI().loadGuildDataNoBank(guildInfo.getGuildUUID(), jedis);
                    }
                })
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicGuilds failed to load member data!")
                .syncLast(guildDataNoBank -> {
                    MemberData oldOwnerData = guildDataNoBank.getMemberDataMap().get(guildDataNoBank.getOwnerUuid());
                    MemberData newOwnerData = guildDataNoBank.getMemberDataMap().get(event.getNewOwner().getUniqueId());
                    if (newOwnerData == null) {
                        event.getOldOwner().sendMessage(GuildUtil.PREFIX + "Error: new owner data could not be found!");
                        return;
                    }
                    oldOwnerData.setRank(GuildRank.OFFICER);
                    newOwnerData.setRank(GuildRank.OWNER);
                    syncDisplays(oldOwner);
                    syncMemberDisplays(event.getGuildUUID());
                    oldOwner.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Successfully transferred guild ownership. You have been demoted to officer."));
                    event.getNewOwner().sendMessage(GuildUtil.PREFIX + "You are now the owner of " + guildDataNoBank.getName() + "!");
                })
                .execute();
    }

    @EventHandler(priority = EventPriority.HIGHEST) // late
    public void onJoin(CharacterLoadedEvent event) {
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
        if (player == null) return; // Player went offline
//        Bukkit.broadcastMessage("syncing guild displays");
        GuildInfo guild = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (guild != null) {
            RunicGuilds.getDataAPI().setGuildForPlayer(player.getUniqueId(), guild.getName());
        } else {
            RunicGuilds.getDataAPI().setGuildForPlayer(player.getUniqueId(), "None");
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
        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> {
                    try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
                        return RunicGuilds.getDataAPI().loadGuildMembers(guildUUID, jedis);
                    }
                })
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicGuilds failed to load member data!")
                .syncLast(guildMembers -> {
                    List<MemberData> memberDataList = new ArrayList<>(guildMembers.values());
                    for (MemberData member : memberDataList) {
                        Player playerMember = Bukkit.getPlayer(member.getUuid());
                        if (playerMember == null) continue;
                        syncDisplays(playerMember);
                    }
                })
                .execute();
    }
}
