package com.runicrealms.runicguilds;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.runicrealms.runicguilds.config.ConfigLoader;
import com.runicrealms.runicguilds.config.GuildUtil;
import com.runicrealms.runicguilds.event.EventPlayerJoinQuit;

public class Plugin extends JavaPlugin {
	
	private static Plugin instance;
	
	@Override
	public void onEnable() {
		instance = this;
		ConfigLoader.initDirs();
		GuildUtil.loadGuilds();
		Bukkit.getLogger().log(Level.INFO, "[RunicGuilds] All guilds have been loaded!");
		EventPlayerJoinQuit.initializePlayerCache();
		this.getServer().getPluginManager().registerEvents(new EventPlayerJoinQuit(), this);
	}

	public static Plugin getInstance() {
		return instance;
	}

}
