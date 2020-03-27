package com.runicrealms.runicguilds.guilds;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

public class Guild implements Cloneable {

	private Set<GuildMember> members;
	private GuildMember owner;
	private String guildName;
	private String guildPrefix;
	private Integer score;
	private List<ItemStack> bank;
	private Integer bankSize;

	public Guild(Set<GuildMember> members, GuildMember owner, String guildName, String guildPrefix, List<ItemStack> bank, Integer bankSize) {
		this.members = members;
		this.owner = owner;
		this.guildName = guildName;
		this.guildPrefix = guildPrefix;
		this.bank = bank;
		this.bankSize = bankSize;
		this.recalculateScore();
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

	public Integer getBankSize() {
		return this.bankSize;
	}

	public List<ItemStack> getBank() {
		return this.bank;
	}

	public List<GuildMember> getMembersWithOwner() {
		List<GuildMember> membersWithOwner = new ArrayList<GuildMember>(this.members);
		membersWithOwner.add(this.owner);
		return membersWithOwner;
	}

	public void setBankSize(Integer size) {
		this.bankSize = size;
	}

	public void setBank(List<ItemStack> bank) {
		this.bank = bank;
	}

	public void transferOwnership(GuildMember member) {
		this.owner = member;
		this.members.remove(member);
	}

	public boolean hasMinRank(UUID player, GuildRank rank) {
		if (this.owner.getUUID().toString().equalsIgnoreCase(player.toString())) {
			return true;
		}
		for (GuildMember member : this.members) {
			if (member.getUUID().toString().equalsIgnoreCase(player.toString())) {
				if (member.getRank().getRankNumber() <= rank.getRankNumber()) {
					return true;
				}
				break;
			}
		}
		return false;
	}

	public GuildMember getMember(UUID player) {
		if (this.owner.getUUID().toString().equalsIgnoreCase(player.toString())) {
			return this.owner;
		}
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

	public void setGuildName(String name) {
		this.guildName = name;
	}

	public void setGuildPrefix(String prefix) {
		this.guildPrefix = prefix;
	}

	@Override
	public Guild clone() {
		List<ItemStack> newItems = new ArrayList<ItemStack>();
		for (ItemStack item : this.bank) {
			if (item != null) {
				newItems.add(item.clone());
			} else {
				newItems.add(null);
			}
		}
		Set<GuildMember> newMembers = new HashSet<GuildMember>();
		for (GuildMember member : this.members) {
			newMembers.add(member.clone());
		}
		return new Guild(newMembers, this.owner.clone(), new String(this.guildName), new String(this.guildPrefix), newItems, new Integer(this.bankSize));
	}

}
