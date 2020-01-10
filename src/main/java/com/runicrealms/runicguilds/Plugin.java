package com.runicrealms.runicguilds;

import java.util.logging.Level;

import com.runicrealms.runicguilds.command.GuildCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.runicrealms.runicguilds.config.ConfigLoader;
import com.runicrealms.runicguilds.config.GuildUtil;
import com.runicrealms.runicguilds.event.EventPlayerJoinQuit;

public class Plugin extends JavaPlugin {
	
	private static Plugin instance;
	
	@Override
	public void onEnable() {
		instance = this;
		FileConfiguration config = this.getConfig();
		config.options().copyDefaults(true);
		this.saveDefaultConfig();
		ConfigLoader.initDirs();
		GuildUtil.loadGuilds();
		Bukkit.getLogger().log(Level.INFO, "[RunicGuilds] All guilds have been loaded!");
		EventPlayerJoinQuit.initializePlayerCache();
		this.getServer().getPluginManager().registerEvents(new EventPlayerJoinQuit(), this);
		this.getCommand("guild").setExecutor(new GuildCommand());

	}

	public static Plugin getInstance() {
		return instance;
	}

}
