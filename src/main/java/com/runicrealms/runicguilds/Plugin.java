package com.runicrealms.runicguilds;

import java.util.*;
import java.util.logging.Level;

import com.runicrealms.runicguilds.event.EventClickNpc;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.listeners.DataListener;
import com.runicrealms.runicguilds.util.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.runicrealms.runicguilds.command.GuildCommand;
import com.runicrealms.runicguilds.command.GuildModCommand;
import com.runicrealms.runicguilds.config.ConfigLoader;
import com.runicrealms.runicguilds.config.GuildUtil;
import com.runicrealms.runicguilds.event.EventPlayerJoinQuit;
import com.runicrealms.runicguilds.gui.GuildBankUtil;

public class Plugin extends JavaPlugin {
	
	private static Plugin instance;
	private static Set<UUID> playersCreatingGuild = new HashSet<UUID>();

	public static List<Integer> GUILD_HERALDS;
	public static int GUILD_COST;
	
	@Override
	public void onEnable() {
		instance = this;
		FileConfiguration config = this.getConfig();
		config.options().copyDefaults(true);
		this.saveDefaultConfig();
		ConfigLoader.initDirs();
		GuildUtil.loadGuilds();
		Bukkit.getLogger().log(Level.INFO, "[RunicGuilds] All guilds have been loaded!");
		GUILD_HERALDS = this.getConfig().getIntegerList("guild-heralds");
		GUILD_COST = this.getConfig().getInt("guild-cost");
		EventPlayerJoinQuit.initializePlayerCache();
		this.getServer().getPluginManager().registerEvents(new EventPlayerJoinQuit(), this);
		this.getServer().getPluginManager().registerEvents(new GuildBankUtil(), this);
		this.getServer().getPluginManager().registerEvents(new EventClickNpc(), this);
		this.getServer().getPluginManager().registerEvents(new DataListener(), this);
		this.getCommand("guild").setExecutor(new GuildCommand());
		this.getCommand("guildmod").setExecutor(new GuildModCommand());

		// register placeholder tags
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			new PlaceholderAPI().register();
		}
	}

	@Override
	public void onDisable() {
		// todo: save guilds data
	}

	public static Set<UUID> getPlayersCreatingGuild() {
		return playersCreatingGuild;
	}

	public static Plugin getInstance() {
		return instance;
	}

}
