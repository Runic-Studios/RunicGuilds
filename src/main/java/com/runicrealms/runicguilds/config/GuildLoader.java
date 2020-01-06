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

	private static Map<File, FileConfiguration> configs = new HashMap<File, FileConfiguration>();
	private static Map<String, Guild> cachedGuilds = new HashMap<String, Guild>();

	public static void loadConfigs() {
		File folder = ConfigLoader.getSubFolder(Plugin.getInstance().getDataFolder(), "guilds");
		for (File file : folder.listFiles()) {
			if (!file.isDirectory()) {
				configs.put(new File(folder, file.getName()), ConfigLoader.getYamlConfigFile(file.getName(), folder);
			}
		}
	}

	private void saveToFile() {
		try {
			this.config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Guild loadGuild(String guildName) {
		boolean contains = false;
		for (File file : configs.keySet()) {
			if (file.getName().substring(0, file.getName().length() - 5).equalsIgnoreCase(guildName)) {
				contains = true;
				break;
			}
		}
		if (contains == false) {
			
		}
		if (!cachedGuilds.contains)
			ConfigurationSection guildMasterSec = config.getConfigurationSection("guild_master");
			UUID guildMasterUUID = UUID.fromString((String) guildMasterSec.getKeys(false).toArray()[0]);
			GuildMember owner = new GuildMember(guildMasterUUID, GuildRank.OWNER, guildMasterSec.getInt(guildMasterUUID + ".score"));
			List<GuildMember> members = new ArrayList<GuildMember>();
			ConfigurationSection membersSec = config.getConfigurationSection("members_list");
			for (String key : membersSec.getKeys(false)) {
				members.add(new GuildMember(UUID.fromString(key), GuildRank.getByName(membersSec.getString(key + ".rank")), membersSec.getInt(key + ".score")));
			}
			return new Guild(members, owner);
	}

	public static void saveToConfig(Guild guild) {
		File folder = ConfigLoader.getSubFolder(Plugin.getInstance().getDataFolder(), "guilds");
		ConfigurationSection config = ConfigLoader.getYamlConfigFile(guild, folder)
		this.config.set("guild_master." + guild.getOwner().getUUID().toString() + ".score", guild.getOwner().getScore());
		for (GuildMember member : guild.getMembers()) {
			this.config.set("members_list." + member.getUUID().toString() + ".rank", member.getRank().getName());
			this.config.set("members_list." + member.getUUID().toString() + ".score", member.getScore());
		}
		this.saveToFile();
	}

	public FileConfiguration getConfig() {
		return this.config;
	}

	public File getFile() {
		return this.file;
	}

	public static GuildLoader getFile(String fileName) {
		File folder = ConfigLoader.getSubFolder(Plugin.getInstance().getDataFolder(), "users");
		for (File file : folder.listFiles()) {
			if (file.getName().equalsIgnoreCase(fileName)) {
				return new GuildLoader(ConfigLoader.getYamlConfigFile(file.getName(), folder), file);
			}
		}
		File file = new File(folder, fileName);
		try {
			file.createNewFile();
			return new GuildLoader(ConfigLoader.getYamlConfigFile(file.getName(), folder), file);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}	
	}

}
