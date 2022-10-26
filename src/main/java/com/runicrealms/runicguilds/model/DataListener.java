package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.redis.RedisUtil;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.api.RunicGuildsAPI;
import com.runicrealms.runicguilds.event.*;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildMember;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;

public class DataListener implements Listener {

    @EventHandler
    public void onGuildCreation(GuildCreationEvent e) {
        Player owner = Bukkit.getPlayer(e.getGuild().getOwner().getUUID());
        syncDisplays(owner);
    }

    /**
     * Delayed by 1s, as this event is called BEFORE the guild is removed.
     */
    @EventHandler
    public void onGuildDisband(GuildDisbandEvent event) {
        Bukkit.getScheduler().runTaskLater(Plugin.getInstance(), () -> {
            Player whoDisbanded = Bukkit.getPlayer(event.getDisbander());
            syncDisplays(whoDisbanded);
            for (GuildMember member : event.getGuild().getMembersWithOwner()) {
                Player playerMember = Bukkit.getPlayer(member.getUUID());
                if (playerMember == null) continue;
                syncDisplays(playerMember);
            }
        }, 20L);
    }

    @EventHandler
    public void onInvitationAccept(GuildInvitationAcceptedEvent event) {
        Player whoWasInvited = Bukkit.getPlayer(event.getInvited());
        syncDisplays(whoWasInvited);
        for (GuildMember member : event.getGuild().getMembersWithOwner()) {
            Player playerMember = Bukkit.getPlayer(member.getUUID());
            if (playerMember == null) continue;
            syncDisplays(playerMember);
        }
    }

    @EventHandler
    public void onKick(GuildMemberKickedEvent event) {
        Player whoWasKicked = Bukkit.getPlayer(event.getKicked());
        syncDisplays(whoWasKicked);
        for (GuildMember member : event.getGuild().getMembersWithOwner()) {
            Player playerMember = Bukkit.getPlayer(member.getUUID());
            if (playerMember == null) continue;
            syncDisplays(playerMember);
        }
    }

    @EventHandler
    public void onLeave(GuildMemberLeaveEvent event) {
        Player whoLeft = Bukkit.getPlayer(event.getMember());
        syncDisplays(whoLeft);
        for (GuildMember member : event.getGuild().getMembersWithOwner()) {
            Player playerMember = Bukkit.getPlayer(member.getUUID());
            if (playerMember == null) continue;
            syncDisplays(playerMember);
        }
    }

    @EventHandler
    public void onTransfer(GuildOwnershipTransferedEvent event) {
        Player oldOwner = Bukkit.getPlayer(event.getOldOwner());
        syncDisplays(oldOwner);
        for (GuildMember member : event.getGuild().getMembersWithOwner()) {
            Player playerMember = Bukkit.getPlayer(member.getUUID());
            if (playerMember == null) continue;
            syncDisplays(playerMember);
        }
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
        Guild guild = RunicGuildsAPI.getGuild(player.getUniqueId());
        try (Jedis jedis = RunicCoreAPI.getNewJedisResource()) {
            String key = player.getUniqueId() + ":guild";
            if (guild != null) {
                jedis.set(player.getUniqueId() + ":guild", guild.getGuildName());
                jedis.expire(key, RedisUtil.EXPIRE_TIME);
            } else {
                jedis.set(player.getUniqueId() + ":guild", "None");
                jedis.expire(key, RedisUtil.EXPIRE_TIME);
            }
        }
        RunicCore.getTabListManager().setupTab(player);
        RunicCoreAPI.updatePlayerScoreboard(player);
    }
}
