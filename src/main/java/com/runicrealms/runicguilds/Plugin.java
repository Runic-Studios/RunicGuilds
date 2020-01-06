package com.runicrealms.runicguilds;

import com.runicrealms.runicguilds.config.ConfigLoader;
import com.runicrealms.runicguilds.config.GuildLoader;
import com.runicrealms.runicguilds.guilds.Guild;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Plugin extends JavaPlugin {
	
	private static Plugin instance;
	private static List<Guild> guilds;
	
	@Override
	public void onEnable() {
		instance = this;
		ConfigLoader.initDirs();
		GuildLoader.loadConfigs();
		guilds = GuildLoader.getAllGuilds();
	}

	public static Plugin getInstance() {
		return instance;
	}

	public static List<Guild> getGuilds() {
		return guilds;
	}
	
}
