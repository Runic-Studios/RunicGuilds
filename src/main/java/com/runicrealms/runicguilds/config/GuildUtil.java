package com.runicrealms.runicguilds.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildMember;
import com.runicrealms.runicguilds.result.GuildCreationResult;

public class GuildUtil {

	private static Map<String, DataFileConfiguration> guilds = new HashMap<String, DataFileConfiguration>();
	private static Map<UUID, String> players = new HashMap<UUID, String>();
	
	public static List<Guild> getAllGuilds() {
		List<Guild> allGuilds = new ArrayList<Guild>();
		for (String key : guilds.keySet()) {
			allGuilds.add(guilds.get(key).getGuild());
		}
		return allGuilds;
	}

	public static void loadGuilds() {
		for (File file : ConfigLoader.getGuildsFolder().listFiles()) {
			if (!file.isDirectory()) {
				guilds.put(file.getName().replace(".yml", ""), new DataFileConfiguration(file.getName().replace(".yml", "")));
			}
		}
	}
	
	public static Guild getGuild(UUID player) {
		for (String key : guilds.keySet()) {
			for (GuildMember member : guilds.get(key).getGuild().getMembers()) {
				if (member.getUUID().toString().equalsIgnoreCase(player.toString())) {
					return guilds.get(key).getGuild();
				}
			}
		}
		return null;
	}
	
	public static Guild getGuild(String prefix) {
		for (String key : guilds.keySet()) {
			if (guilds.get(key).getGuild().getGuildPrefix().equalsIgnoreCase(prefix)) {
				return guilds.get(key).getGuild();
			}
		}
		return null;
	}
	
	public static GuildCreationResult createGuild(UUID owner, String name, String prefix) {
		if (guilds.containsKey(prefix)) {
			return GuildCreationResult.PREFIX_NOT_UNIQUE;
		}
		for (String guild : guilds.keySet()) {
			if (guilds.get(guild).getGuild().getGuildName().equalsIgnoreCase(name)) {
				return GuildCreationResult.NAME_NOT_UNIQUE;
			}
			if (guilds.get(guild).getGuild().getOwner().getUUID().toString().equalsIgnoreCase(owner.toString())) {
				return GuildCreationResult.CREATOR_IN_GUILD;
			}
			for (GuildMember member : guilds.get(guild).getGuild().getMembers()) {
				if (member.getUUID().toString().equalsIgnoreCase(owner.toString())) {
					return GuildCreationResult.CREATOR_IN_GUILD;
				}
			}
		}
		FileConfiguration config = ConfigLoader.getYamlConfigFile(prefix + ".yml", ConfigLoader.getGuildsFolder());
		config.set("owner." + owner.toString() + ".score", 0);
		config.set("prefix", prefix);
		config.set("name", name);
		guilds.put(prefix, new DataFileConfiguration(prefix + ".yml"));
		return GuildCreationResult.SUCCESSFUL;
	}
	
	public static Map<UUID, String> getPlayerCache() {
		return players;
	}
	
	public static Map<String, DataFileConfiguration> getGuildFiles() {
		return guilds;
	}
	
	public static UUID getOfflinePlayerUUID(String playerName) {
		@SuppressWarnings("deprecation")
		OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
		if (player.hasPlayedBefore()) {
			return player.getUniqueId();
		}
		return null;
	}

	public static void saveGuild(Guild guild) {
		guilds.get(guild.getGuildPrefix()).save(guild);
	}

	public static void removeGuild(Guild guild) {
		guilds.remove(guild.getGuildPrefix());

	}

}
