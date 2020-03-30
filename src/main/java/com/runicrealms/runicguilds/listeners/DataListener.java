package com.runicrealms.runicguilds.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.api.*;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildMember;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

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
    public void onGuildDisband(GuildDisbandEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player disbander = Bukkit.getPlayer(e.getDisbander());
                syncDisplays(disbander);
                for (GuildMember member : e.getGuild().getMembersWithOwner()) {
                    Player plMem = Bukkit.getPlayer(member.getUUID());
                    if (plMem == null) continue;
                    syncDisplays(plMem);
                }
            }
        }.runTaskLater(Plugin.getInstance(), 20L);
    }

    @EventHandler
    public void onInvitationAccept(GuildInvitationAcceptedEvent e) {
        Player invited = Bukkit.getPlayer(e.getInvited());
        syncDisplays(invited);
        for (GuildMember member : e.getGuild().getMembersWithOwner()) {
            Player plMem = Bukkit.getPlayer(member.getUUID());
            if (plMem == null) continue;
            syncDisplays(plMem);
        }
    }

    @EventHandler
    public void onKick(GuildMemberKickedEvent e) {
        Player kicked = Bukkit.getPlayer(e.getKicked());
        syncDisplays(kicked);
        for (GuildMember member : e.getGuild().getMembersWithOwner()) {
            Player plMem = Bukkit.getPlayer(member.getUUID());
            if (plMem == null) continue;
            syncDisplays(plMem);
        }
    }

    @EventHandler
    public void onLeave(GuildMemberLeaveEvent e) {
        Player leaver = Bukkit.getPlayer(e.getMember());
        syncDisplays(leaver);
        for (GuildMember member : e.getGuild().getMembersWithOwner()) {
            Player plMem = Bukkit.getPlayer(member.getUUID());
            if (plMem == null) continue;
            syncDisplays(plMem);
        }
    }

    @EventHandler
    public void onTransfer(GuildOwnershipTransferedEvent e) {
        Player oldOwner = Bukkit.getPlayer(e.getOldOwner());
        syncDisplays(oldOwner);
        for (GuildMember member : e.getGuild().getMembersWithOwner()) {
            Player plMem = Bukkit.getPlayer(member.getUUID());
            if (plMem == null) continue;
            syncDisplays(plMem);
        }
    }

    private void syncDisplays(Player pl) {
        if (pl == null) return;
        Guild guild = RunicGuildsAPI.getGuild(pl.getUniqueId());
        // update cache
        if (guild != null) {
            RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).setGuild(guild.getGuildName());
        } else {
            RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).setGuild("None");
        }
        // update tab
        RunicCore.getTabListManager().setupTab(pl);
    }
}
