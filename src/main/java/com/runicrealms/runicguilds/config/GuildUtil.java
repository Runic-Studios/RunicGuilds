package com.runicrealms.runicguilds.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.runicrealms.runicguilds.api.GuildRenameResult;
import com.runicrealms.runicguilds.api.GuildReprefixResult;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildMember;
import com.runicrealms.runicguilds.api.GuildCreationResult;
import org.bukkit.entity.Player;

import javax.xml.crypto.Data;

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
				guilds.put(file.getName().replace(".yml", ""), new DataFileConfiguration(file.getName()));
			}
		}
	}

	public static Guild getGuild(UUID player) {
		for (String key : guilds.keySet()) {
			Guild guild = guilds.get(key).getGuild();
			if (guild.getOwner().getUUID().toString().equalsIgnoreCase(player.toString())) {
				return guild;
			}
			for (GuildMember member : guild.getMembers()) {
				if (member.getUUID().toString().equalsIgnoreCase(player.toString())) {
					return guild;
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


	public static List<GuildMember> getOnlineMembersWithOwner(Guild guild) {
		List<GuildMember> online = new ArrayList<>();
		for (Player pl : Bukkit.getOnlinePlayers()) {
			if (guild.getMember(pl.getUniqueId()) != null) {
				online.add(guild.getMember(pl.getUniqueId()));
			}
		}
		return online;
	}
	
	public static GuildCreationResult createGuild(UUID owner, String name, String prefix) {
		if (prefix.length() != 3) {
			return GuildCreationResult.BAD_PREFIX;
		}
		Pattern pattern = Pattern.compile("[a-zA-Z]");
		Matcher matcher = pattern.matcher(prefix);
		if (matcher.find() == false) {
			return GuildCreationResult.BAD_PREFIX;
		}
		if (name.length() > 16) {
			return GuildCreationResult.NAME_TOO_LONG;
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
			if (guild.equalsIgnoreCase(prefix)) {
				return GuildCreationResult.PREFIX_NOT_UNIQUE;
			}
		}
		FileConfiguration config = ConfigLoader.getYamlConfigFile(prefix + ".yml", ConfigLoader.getGuildsFolder());
		config.set("owner." + owner.toString() + ".score", 0);
		config.set("prefix", prefix);
		config.set("name", name);
		config.set("bank-size", 45);
		try {
			config.save(new File(ConfigLoader.getGuildsFolder(), prefix + ".yml"));
		} catch (IOException exception) {
			exception.printStackTrace();
			return GuildCreationResult.INTERNAL_ERROR;
		}
		DataFileConfiguration fileConfig = new DataFileConfiguration(prefix + ".yml");
		guilds.put(prefix, fileConfig);
		players.put(owner, prefix);
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

	public static String getOfflinePlayerName(UUID uuid) {
		@SuppressWarnings("deprecation")
		OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
		if (player.hasPlayedBefore()) {
			return player.getName();
		}
		return null;
	}

	public static void saveGuild(Guild guild) {
		guilds.get(guild.getGuildPrefix()).save(guild);
	}

	public static void saveGuildToFile(Guild guild) {
		guilds.get(guild.getGuildPrefix()).saveGuildToFile(guild);
	}

	public static void removeGuild(Guild guild) {
		guilds.remove(guild.getGuildPrefix());
	}

	public static GuildRenameResult renameGuild(Guild guild, String name) {
		if (name.length() > 16) {
			return GuildRenameResult.NAME_NOT_UNIQUE;
		}
		for (String otherGuild : guilds.keySet()) {
			if (guilds.get(otherGuild).getGuild().getGuildName().equalsIgnoreCase(name)) {
				return GuildRenameResult.NAME_NOT_UNIQUE;
			}
		}
		try {
			guild.setGuildName(name);
			saveGuild(guild);
		} catch (Exception exception) {
			exception.printStackTrace();
			return GuildRenameResult.INTERNAL_ERROR;
		}
		return GuildRenameResult.SUCCESSFUL;
	}

	public static GuildReprefixResult reprefixGuild(Guild guild, String prefix, String oldPrefix) {
		Pattern pattern = Pattern.compile("[a-zA-Z]");
		Matcher matcher = pattern.matcher(prefix);
		if (matcher.find() == false) {
			return GuildReprefixResult.BAD_PREFIX;
		}
		for (String otherGuild : guilds.keySet()) {
			if (otherGuild.equalsIgnoreCase(prefix)) {
				if (!guilds.get(otherGuild).getGuild().getGuildName().equalsIgnoreCase(guild.getGuildName())) {
					return GuildReprefixResult.PREFIX_NOT_UNIQUE;
				}
			}
		}
		try {
			Map<UUID, String> newPlayers = new HashMap<UUID, String>();
			for (Map.Entry<UUID, String> player : players.entrySet()) {
				if (!player.getValue().equalsIgnoreCase(oldPrefix)) {
					newPlayers.put(player.getKey(), player.getValue());
				} else {
					newPlayers.put(player.getKey(), prefix);
				}
			}
			players = newPlayers;
			DataFileConfiguration data = guilds.get(oldPrefix);
			File newFile = new File(data.getFile().getParent(), prefix + ".yml");
			File oldFile = new File(data.getFile().getParent(), oldPrefix + ".yml");
			data.getFile().renameTo(newFile);
			DataFileConfiguration newData = data.clone(prefix);
			Guild newGuild = newData.getGuild();
			newGuild.setGuildPrefix(prefix);
			newData.save(newGuild);
			guilds.remove(oldPrefix);
			guilds.put(prefix, newData);
		} catch (Exception exception) {
			exception.printStackTrace();
			return GuildReprefixResult.INTERNAL_ERROR;
		}
		return GuildReprefixResult.SUCCESSFUL;
	}

}
