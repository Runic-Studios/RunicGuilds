package com.runicrealms.runicguilds.data;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.client.FindIterable;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.guilds.GuildRank;
import com.runicrealms.runicrestart.api.RunicRestartApi;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildMember;
import com.runicrealms.runicguilds.api.GuildCreationResult;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GuildUtil {

	private static final Map<String, GuildData> guilds = new HashMap<String, GuildData>();
	private static final Map<UUID, String> players = new HashMap<UUID, String>();

	public static List<Guild> getAllGuilds() {
		List<Guild> allGuilds = new ArrayList<Guild>();
		for (String key : guilds.keySet()) {
			allGuilds.add(guilds.get(key).getData());
		}
		return allGuilds;
	}

	public static void loadGuilds() {
		Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> {
			FindIterable<Document> iterable = RunicCore.getDatabaseManager().getGuildData().find();
			for (Document guildData : iterable) {
				guilds.put(guildData.getString("prefix"), new GuildData(guildData.getString("prefix")));
			}
			RunicRestartApi.markPluginLoaded("guilds");
		});
	}

	public static GuildData getGuildData(UUID player) {
		for (Map.Entry<String, GuildData> entry : guilds.entrySet()) {
			if (entry.getValue().getData().getOwner().getUUID().toString().equalsIgnoreCase(player.toString())) {
				return entry.getValue();
			}
			for (GuildMember member : entry.getValue().getData().getMembers()) {
				if (member.getUUID().toString().equalsIgnoreCase(player.toString())) {
					return entry.getValue();
				}
			}
		}
		return null;
	}

	public static GuildData getGuildData(String prefix) {
		for (Map.Entry<String, GuildData> entry : guilds.entrySet()) {
			if (entry.getValue().getData().getGuildPrefix().equalsIgnoreCase(prefix)) {
				return entry.getValue();
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
			if (guilds.get(guild).getData().getGuildName().equalsIgnoreCase(name)) {
				return GuildCreationResult.NAME_NOT_UNIQUE;
			}
			if (guilds.get(guild).getData().getOwner().getUUID().toString().equalsIgnoreCase(owner.toString())) {
				return GuildCreationResult.CREATOR_IN_GUILD;
			}
			for (GuildMember member : guilds.get(guild).getData().getMembers()) {
				if (member.getUUID().toString().equalsIgnoreCase(owner.toString())) {
					return GuildCreationResult.CREATOR_IN_GUILD;
				}
			}
			if (guild.equalsIgnoreCase(prefix)) {
				return GuildCreationResult.PREFIX_NOT_UNIQUE;
			}
		}
		List<ItemStack> bank = new ArrayList<ItemStack>();
		for (int i = 0; i < 45; i++) {
			bank.add(null);
		}
		GuildData data = new GuildData(new Guild(new HashSet<GuildMember>(), new GuildMember(owner, GuildRank.OWNER, 0, GuildUtil.getOfflinePlayerName(owner)), name, prefix, bank, 45));
		guilds.put(prefix, data);
		players.put(owner, prefix);
		return GuildCreationResult.SUCCESSFUL;
	}

	public static Map<UUID, String> getPlayerCache() {
		return players;
	}

	public static Map<String, GuildData> getGuildDatas() {
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

//	public static void saveGuild(Guild guild) {
//		guilds.get(guild.getGuildPrefix()).save(guild);
//	}
//
//	public static void saveGuildToFile(Guild guild) {
//		guilds.get(guild.getGuildPrefix()).saveGuildToFile(guild);
//	}

	public static void removeGuildFromCache(Guild guild) {
		guilds.remove(guild.getGuildPrefix());
	}

//	public static GuildRenameResult renameGuild(Guild guild, String name) {
//		if (name.length() > 16) {
//			return GuildRenameResult.NAME_NOT_UNIQUE;
//		}
//		for (String otherGuild : guilds.keySet()) {
//			if (guilds.get(otherGuild).getGuild().getGuildName().equalsIgnoreCase(name)) {
//				return GuildRenameResult.NAME_NOT_UNIQUE;
//			}
//		}
//		try {
//			guild.setGuildName(name);
//			saveGuild(guild);
//		} catch (Exception exception) {
//			exception.printStackTrace();
//			return GuildRenameResult.INTERNAL_ERROR;
//		}
//		return GuildRenameResult.SUCCESSFUL;
//	}
//
//	public static GuildReprefixResult reprefixGuild(Guild guild, String prefix, String oldPrefix) {
//		Pattern pattern = Pattern.compile("[a-zA-Z]");
//		Matcher matcher = pattern.matcher(prefix);
//		if (matcher.find() == false) {
//			return GuildReprefixResult.BAD_PREFIX;
//		}
//		for (String otherGuild : guilds.keySet()) {
//			if (otherGuild.equalsIgnoreCase(prefix)) {
//				if (!guilds.get(otherGuild).getGuild().getGuildName().equalsIgnoreCase(guild.getGuildName())) {
//					return GuildReprefixResult.PREFIX_NOT_UNIQUE;
//				}
//			}
//		}
//		try {
//			Map<UUID, String> newPlayers = new HashMap<UUID, String>();
//			for (Map.Entry<UUID, String> player : players.entrySet()) {
//				if (!player.getValue().equalsIgnoreCase(oldPrefix)) {
//					newPlayers.put(player.getKey(), player.getValue());
//				} else {
//					newPlayers.put(player.getKey(), prefix);
//				}
//			}
//			players = newPlayers;
//			DataFileConfiguration data = guilds.get(oldPrefix);
//			File newFile = new File(data.getFile().getParent(), prefix + ".yml");
//			File oldFile = new File(data.getFile().getParent(), oldPrefix + ".yml");
//			data.getFile().renameTo(newFile);
//			DataFileConfiguration newData = data.clone(prefix);
//			Guild newGuild = newData.getGuild();
//			newGuild.setGuildPrefix(prefix);
//			newData.save(newGuild);
//			guilds.remove(oldPrefix);
//			guilds.put(prefix, newData);
//		} catch (Exception exception) {
//			exception.printStackTrace();
//			return GuildReprefixResult.INTERNAL_ERROR;
//		}
//		return GuildReprefixResult.SUCCESSFUL;
//	}

}
