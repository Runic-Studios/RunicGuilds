package com.runicrealms.runicguilds.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.api.RunicGuildsAPI;
import com.runicrealms.runicguilds.event.*;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildMember;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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
            Player plMem = Bukkit.getPlayer(member.getUUID());
            if (plMem == null) continue;
            syncDisplays(plMem);
        }
    }

    @EventHandler
    public void onLeave(GuildMemberLeaveEvent event) {
        Player whoLeft = Bukkit.getPlayer(event.getMember());
        syncDisplays(whoLeft);
        for (GuildMember member : event.getGuild().getMembersWithOwner()) {
            Player plMem = Bukkit.getPlayer(member.getUUID());
            if (plMem == null) continue;
            syncDisplays(plMem);
        }
    }

    @EventHandler
    public void onTransfer(GuildOwnershipTransferedEvent event) {
        Player oldOwner = Bukkit.getPlayer(event.getOldOwner());
        syncDisplays(oldOwner);
        for (GuildMember member : event.getGuild().getMembersWithOwner()) {
            Player plMem = Bukkit.getPlayer(member.getUUID());
            if (plMem == null) continue;
            syncDisplays(plMem);
        }
    }

    /**
     * @param player
     */
    private void syncDisplays(Player player) {
        if (player == null) return;
        Guild guild = RunicGuildsAPI.getGuild(player.getUniqueId());
        // todo: redis code
//        if (guild != null)
//            RunicCoreAPI.getPlayerCache(player).setGuild(guild.getGuildName());
//        else
//            RunicCoreAPI.getPlayerCache(player).setGuild("None");
        // update tab
        RunicCore.getTabListManager().setupTab(player);
        // todo: scoreboard
    }
}
