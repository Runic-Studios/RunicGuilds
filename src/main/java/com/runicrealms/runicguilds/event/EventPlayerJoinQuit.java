package com.runicrealms.runicguilds.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.runicrealms.runicguilds.config.GuildUtil;

public class EventPlayerJoinQuit implements Listener {
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		String prefix = GuildUtil.getGuild(event.getPlayer().getUniqueId()) == null ? null : GuildUtil.getGuild(event.getPlayer().getUniqueId()).getGuildPrefix();
		GuildUtil.getPlayerCache().put(event.getPlayer().getUniqueId(), prefix);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		GuildUtil.getPlayerCache().remove(event.getPlayer().getUniqueId());
	}
	
	public static void initializePlayerCache() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			String prefix = GuildUtil.getGuild(player.getUniqueId()) == null ? null : GuildUtil.getGuild(player.getUniqueId()).getGuildPrefix();
			GuildUtil.getPlayerCache().put(player.getUniqueId(), prefix);
		}
	}
	
}
