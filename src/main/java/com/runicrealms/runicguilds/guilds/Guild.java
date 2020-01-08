package com.runicrealms.runicguilds.guilds;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.runicrealms.runicguilds.result.GuildPromotionResult;

public class Guild {

	private Set<GuildMember> members;
	private GuildMember owner;
	private String guildName;
	private String guildPrefix;

	public Guild(Set<GuildMember> members, GuildMember owner, String guildName, String guildPrefix) {
		this.members = members;
		this.owner = owner;
		this.guildName = guildName;
		this.guildPrefix = guildPrefix;
	}

	public Set<GuildMember> getMembers() {
		return this.members;
	}

	public GuildMember getOwner() {
		return this.owner;
	}

	public String getGuildName() {
		return this.guildName;
	}

	public String getGuildPrefix() {
		return this.guildPrefix;
	}

	public GuildPromotionResult promoteMember(GuildMember member) {
		if (member.getRank() == GuildRank.OWNER) {
			return GuildPromotionResult.MEMBER_IS_OWNER;
		}
		if (member.getRank() == GuildRank.OFFICER) {
			return GuildPromotionResult.MEMBER_IS_OFFICER;
		}

		if (member.getRank().getRankNumber() < 2) {
			member.getRank().setRankNumber(member.getRank().getRankNumber() + 1);
		}
		return GuildPromotionResult.SUCCESSFUL;
	}

	public void transferOwnership(GuildMember member) {
		this.owner = member;
		this.members.remove(member);
	}

	public boolean hasMinRank(UUID player, Integer rank) {
		if (this.owner.getUUID().toString().equalsIgnoreCase(player.toString())) {
			return true;
		}
		for (GuildMember member : this.members) {
			if (member.getUUID().toString().equalsIgnoreCase(player.toString())) {
				if (member.getRank().getRankNumber() <= rank) {
					return true;
				}
				break;
			}
		}
		return false;
	}

	public GuildMember getMember(UUID player) {
		for (GuildMember member : this.members) {
			if (member.getUUID().toString().equalsIgnoreCase(player.toString())) {
				return member;
			}
		}
		return null;
	}

	public boolean isInGuild(String playerName) {
		@SuppressWarnings("deprecation")
		OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
		if (player.hasPlayedBefore()) {
			return this.getMember(player.getUniqueId()) != null;
		}
		return false;
	}

}
