package com.runicrealms.runicguilds.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.api.event.*;
import com.runicrealms.runicguilds.command.GuildCommandMapManager;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.GuildMember;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.util.GuildBankUtil;
import com.runicrealms.runicguilds.util.GuildUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import redis.clients.jedis.Jedis;

/**
 * Handles logic for what to do whenever a particular custom guild event is called
 */
public class GuildEventListener implements Listener {

    @EventHandler
    public void onGuildCreation(GuildCreationEvent event) {
        Player owner = Bukkit.getPlayer(event.getGuild().getOwner().getUUID());
        syncDisplays(owner);
    }

    /**
     * Handles logic for removing guild data from memory on guild disband
     */
    @EventHandler
    public void onGuildDisband(GuildDisbandEvent event) {
        // set guild data to "None" in redis / mongo, close guild bank
        Player player = event.getWhoDisbanded();
        Guild guild = event.getGuild();
        GuildData guildData = RunicGuilds.getGuildsAPI().getGuildData(guild.getGuildPrefix());
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            for (GuildMember member : guild.getMembers()) {
                RunicGuilds.getGuildsAPI().setJedisGuild(member.getUUID(), "None", jedis);
                Player playerMember = Bukkit.getPlayer(member.getUUID());
                if (playerMember == null) continue;
                if (GuildBankUtil.isViewingBank(member.getUUID())) {
                    GuildBankUtil.close(playerMember);
                }
            }
            // remove guild for owner
            RunicGuilds.getGuildsAPI().setJedisGuild(guild.getOwner().getUUID(), "None", jedis);
        }

        if (GuildBankUtil.isViewingBank(guild.getOwner().getUUID())) {
            Player playerOwner = Bukkit.getPlayer(guild.getOwner().getUUID());
            if (playerOwner != null)
                GuildBankUtil.close(playerOwner);
        }

        // remove from jedis, mongo, and memory
        guildData.delete();
        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Successfully disbanded guild."));
        GuildCommandMapManager.getDisbanding().remove(player.getUniqueId());
        syncDisplays(event.getWhoDisbanded());
        for (GuildMember member : event.getGuild().getMembersWithOwner()) {
            Player playerMember = Bukkit.getPlayer(member.getUUID());
            if (playerMember == null) continue;
            syncDisplays(playerMember);
        }
    }

    @EventHandler
    public void onGuildInvitationAccept(GuildInvitationAcceptedEvent event) {
        Player whoWasInvited = Bukkit.getPlayer(event.getInvited());
        syncDisplays(whoWasInvited);
        for (GuildMember member : event.getGuildData().getGuild().getMembersWithOwner()) {
            Player playerMember = Bukkit.getPlayer(member.getUUID());
            if (playerMember == null) continue;
            syncDisplays(playerMember);
        }
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            event.getGuildData().writeToJedis(jedis);
        }
    }

    @EventHandler
    public void onGuildKick(GuildMemberKickedEvent event) {
        Player whoWasKicked = Bukkit.getPlayer(event.getKicked());
        if (whoWasKicked == null) return;
        event.getGuild().removeMember(whoWasKicked.getUniqueId());
        whoWasKicked.sendMessage(ColorUtil.format(GuildUtil.PREFIX + ChatColor.RED + "You have been kicked from your guild!"));
        syncDisplays(whoWasKicked);
        for (GuildMember member : event.getGuild().getMembersWithOwner()) {
            Player playerMember = Bukkit.getPlayer(member.getUUID());
            if (playerMember == null) continue;
            syncDisplays(playerMember);
        }
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            RunicGuilds.getGuildsAPI().setJedisGuild(whoWasKicked.getUniqueId(), "None", jedis);
        }
    }

    @EventHandler
    public void onGuildLeave(GuildMemberLeaveEvent event) {
        Player whoLeft = Bukkit.getPlayer(event.getMember());
        syncDisplays(whoLeft);
        for (GuildMember member : event.getGuild().getMembersWithOwner()) {
            Player playerMember = Bukkit.getPlayer(member.getUUID());
            if (playerMember == null) continue;
            syncDisplays(playerMember);
        }
    }

    /**
     * Updates player score on change, recalculates guild's entire score
     */
    @EventHandler
    public void onGuildScoreChange(GuildScoreChangeEvent event) {
        Guild guild = event.getGuildData().getGuild();
        GuildMember member = event.getGuildMember();
        int score = member.getScore();
        member.setScore(score + event.getScore());
        guild.recalculateScore();
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            event.getGuildData().writeToJedis(jedis);
        }
    }

    @EventHandler
    public void onGuildTransfer(GuildOwnershipTransferedEvent event) {
        Player oldOwner = Bukkit.getPlayer(event.getOldOwner());
        syncDisplays(oldOwner);
        for (GuildMember member : event.getGuild().getMembersWithOwner()) {
            Player playerMember = Bukkit.getPlayer(member.getUUID());
            if (playerMember == null) continue;
            syncDisplays(playerMember);
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
        Guild guild = RunicGuilds.getGuildsAPI().getGuild(player.getUniqueId());
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            if (guild != null) {
                RunicGuilds.getGuildsAPI().setJedisGuild(player.getUniqueId(), guild.getGuildName(), jedis);
            } else {
                RunicGuilds.getGuildsAPI().setJedisGuild(player.getUniqueId(), "None", jedis);
            }
        }
        RunicCore.getScoreboardAPI().updatePlayerScoreboard(player);
        GuildUtil.updateGuildTabColumn(player);
    }
}
