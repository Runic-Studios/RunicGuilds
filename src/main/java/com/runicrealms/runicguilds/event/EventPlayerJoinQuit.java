package com.runicrealms.runicguilds.event;

import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.data.GuildData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.runicrealms.runicguilds.command.GuildCommand;
import com.runicrealms.runicguilds.data.GuildUtil;

public class EventPlayerJoinQuit implements Listener {
	
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
		if (GuildCommand.getTransferOwnership().containsKey(event.getPlayer().getUniqueId())) {
			GuildCommand.getTransferOwnership().remove(event.getPlayer().getUniqueId());
		}
		if (GuildCommand.getDisbanding().contains(event.getPlayer().getUniqueId())) {
			GuildCommand.getDisbanding().remove(event.getPlayer().getUniqueId());
		}
		if (Plugin.getPlayersCreatingGuild().contains(event.getPlayer().getUniqueId())) {
			Plugin.getPlayersCreatingGuild().remove(event.getPlayer().getUniqueId());
		}
	}
	
	public static void initializePlayerCache() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			GuildUtil.getPlayerCache().put(player.getUniqueId(), GuildUtil.getGuildData(player.getUniqueId()).getData().getGuildPrefix());
		}
	}
	
}
