package com.runicrealms.runicguilds.api;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.runicrealms.runicguilds.data.GuildData;
import com.runicrealms.runicguilds.data.PlayerGuildDataUtil;
import org.bukkit.Bukkit;

import com.runicrealms.runicguilds.data.GuildUtil;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildMember;

public class RunicGuildsAPI {

	public static GuildCreationResult createGuild(UUID owner, String name, String prefix, boolean modCreated) {
		GuildCreationResult result = GuildUtil.createGuild(owner, name, prefix);
		if (result == GuildCreationResult.SUCCESSFUL) {
			PlayerGuildDataUtil.setGuildForPlayer(name, owner.toString());
			Bukkit.getServer().getPluginManager().callEvent(new GuildCreationEvent(GuildUtil.getGuildData(prefix).getData(), modCreated));
		}
		return result;
	}

	public static Guild getGuild(UUID uuid) {
		GuildData data = GuildUtil.getGuildData(uuid);
		if (data != null) {
			return data.getData();
		}
		return null;
	}

	public static Guild getGuild(String prefix) {
		GuildData data = GuildUtil.getGuildData(prefix);
		if (data != null) {
			return data.getData();
		}
		return null;
	}

	public static List<Guild> getAllGuilds() {
		return GuildUtil.getAllGuilds();
	}

	public static boolean isInGuild(UUID player) {
		return GuildUtil.getGuildData(player).getData() != null;
	}

	public static boolean addPlayerScore(UUID player, Integer score) {
		if (isInGuild(player)) {
			GuildData guildData = GuildUtil.getGuildData(player);
			guildData.getData().increasePlayerScore(player, score);
			guildData.queueToSave();
			return true;
		}
		return false;
	}

	public static Set<UUID> getGuildRecipients(UUID player) {
		if (!isInGuild(player)) {
			return null;
		}
		Set<UUID> recipients = new HashSet<UUID>();
		Guild guild = getGuild(player);
		if (!guild.getOwner().getUUID().toString().equalsIgnoreCase(player.toString())) {
			recipients.add(guild.getOwner().getUUID());
		}
		for (GuildMember member : guild.getMembers()) {
			if (!member.getUUID().toString().equalsIgnoreCase(player.toString())) {
				recipients.add(member.getUUID());
			}
		}
		return recipients;
	}

}
