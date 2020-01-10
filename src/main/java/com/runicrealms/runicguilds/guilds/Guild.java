package com.runicrealms.runicguilds.guilds;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class Guild {

	private Set<GuildMember> members;
	private GuildMember owner;
	private String guildName;
	private String guildPrefix;
	private Integer score;

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

	public void removeMember(UUID uuid) {
		for (GuildMember member : members) {
			if (member.getUUID().toString().equalsIgnoreCase(uuid.toString())) {
				members.remove(member);
				break;
			}
		}
	}

	public Integer getScore() {
		return this.score;
	}

	private void recalculateScore() {
		this.score = 0;
		for (GuildMember member : members) {
			score += member.getScore();
		}
	}

	public void setPlayerScore(UUID player, Integer score) {
		GuildMember member = getMember(player);
		member.setScore(score);
		this.recalculateScore();
	}

	public void increasePlayerScore(UUID player, Integer score) {
		GuildMember member = getMember(player);
		member.setScore(member.getScore() + score);
		this.recalculateScore();
	}

}
