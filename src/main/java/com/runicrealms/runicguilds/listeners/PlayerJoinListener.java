package com.runicrealms.runicguilds.listeners;

import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.command.GuildCommandMapManager;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.util.GuildUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        GuildData guildData = GuildUtil.getGuildData(event.getPlayer().getUniqueId());
        if (guildData != null) {
            GuildUtil.getPlayerCache().put(event.getPlayer().getUniqueId(), guildData.getData().getGuildPrefix());
        } else {
            GuildUtil.getPlayerCache().put(event.getPlayer().getUniqueId(), null);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        GuildUtil.getPlayerCache().remove(event.getPlayer().getUniqueId());
        GuildCommandMapManager.getTransferOwnership().remove(event.getPlayer().getUniqueId());
        GuildCommandMapManager.getDisbanding().remove(event.getPlayer().getUniqueId());
        RunicGuilds.getPlayersCreatingGuild().remove(event.getPlayer().getUniqueId());
    }

    public static void initializePlayerCache() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            GuildUtil.getPlayerCache().put(player.getUniqueId(), GuildUtil.getGuildData(player.getUniqueId()).getData().getGuildPrefix());
        }
    }

}
