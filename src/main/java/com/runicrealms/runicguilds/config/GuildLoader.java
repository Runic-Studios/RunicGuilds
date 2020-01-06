package com.runicrealms.runicguilds.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildMember;
import com.runicrealms.runicguilds.guilds.GuildRank;

public class GuildLoader {

	private static Map<Guild, DataFileConfiguration> configs = new HashMap<Guild, DataFileConfiguration>();
	private static Map<String, Guild> cachedGuilds = new HashMap<String, Guild>();

	public static List<Guild> getAllGuilds() {
		List<Guild> guilds = new ArrayList<Guild>();
		for (File file : configs.keySet()) {
			String prefix = file.getName().replaceAll(".yml", "");
			guilds.add(loadGuild(prefix));
		}
		return guilds;
	}

	public static void loadConfigs() {
		File folder = ConfigLoader.getSubFolder(Plugin.getInstance().getDataFolder(), "guilds");
		for (File file : folder.listFiles()) {
			if (!file.isDirectory()) {
				configs.put(new File(folder, file.getName()), ConfigLoader.getYamlConfigFile(file.getName(), folder));
			}
		}
	}

	public static void createGuild(UUID owner, String name, String prefix) {
		File folder = ConfigLoader.getSubFolder(Plugin.getInstance().getDataFolder(), "guilds");
		FileConfiguration fileConfig = ConfigLoader.getYamlConfigFile(prefix + ".yml", folder);
		configs.put(new File(folder, prefix + ".yml"), fileConfig);
		Plugin.getGuilds().add(loadGuild(prefix));
	}

	public static Guild loadGuild(String guildPrefix) {
		File folder = ConfigLoader.getSubFolder(Plugin.getInstance().getDataFolder(), "guilds");
		boolean contains = false;
		for (File file : configs.keySet()) {
			if (file.getName().equalsIgnoreCase(guildPrefix + ".yml")) {
				contains = true;
				break;
			}
		}
		if (contains == false) {
			FileConfiguration fileConfig = ConfigLoader.getYamlConfigFile(guildPrefix + ".yml", folder);
			configs.put(new File(folder, guildPrefix + ".yml"), fileConfig);
		}
		if (!cachedGuilds.containsKey(guildPrefix)) {
			FileConfiguration config = ConfigLoader.getYamlConfigFile(guildPrefix + ".yml", folder);
			ConfigurationSection guildMasterSec = config.getConfigurationSection("owner");
			UUID guildMasterUUID = UUID.fromString((String) guildMasterSec.getKeys(false).toArray()[0]);
			GuildMember owner = new GuildMember(guildMasterUUID, GuildRank.OWNER, guildMasterSec.getInt(guildMasterUUID + ".score"));
			List<GuildMember> members = new ArrayList<GuildMember>();
			ConfigurationSection membersSec = config.getConfigurationSection("members");
			for (String key : membersSec.getKeys(false)) {
				members.add(new GuildMember(UUID.fromString(key), GuildRank.getByName(membersSec.getString(key + ".rank")), membersSec.getInt(key + ".score")));
			}
			return new Guild(members, owner, config.getString("name"), config.getString("prefix"));
		}
		return cachedGuilds.get(guildPrefix);
	}

}
