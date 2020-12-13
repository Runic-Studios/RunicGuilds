package com.runicrealms.runicguilds.guilds;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

public class Guild implements Cloneable {

	private final Set<GuildMember> members;
	private final GuildBanner guildBanner;
	private GuildMember owner;
	private String guildName;
	private String guildPrefix;
	private GuildStage guildStage;
	private Integer score;
	private List<ItemStack> bank;
	private Integer bankSize;
	private Map<GuildRank, Boolean> bankAccess;
	private int guildEXP;

	public Guild(Set<GuildMember> members, GuildMember owner, String guildName, String guildPrefix, List<ItemStack> bank, Integer bankSize, Map<GuildRank, Boolean> bankAccess) {
		this.members = members;
		this.owner = owner;
		this.guildName = guildName;
		this.guildPrefix = guildPrefix;
		this.guildStage = GuildStage.STAGE1;
		this.bank = bank;
		this.bankSize = bankSize;
		this.bankAccess = bankAccess;
		this.guildEXP = 0;
		this.recalculateScore();
		this.guildBanner = new GuildBanner(this);
	}

	public Guild(Set<GuildMember> members, GuildBanner guildBanner, GuildMember owner, String guildName, String guildPrefix, GuildStage guildStage, Integer score, List<ItemStack> bank, Integer bankSize, Map<GuildRank, Boolean> bankAccess, int guildEXP) {
		this.members = members;
		this.guildBanner = guildBanner;
		this.owner = owner;
		this.guildName = guildName;
		this.guildPrefix = guildPrefix;
		this.guildStage = guildStage;
		this.score = score;
		this.bank = bank;
		this.bankSize = bankSize;
		this.bankAccess = bankAccess;
		this.guildEXP = guildEXP;
		this.recalculateScore();
	}

	public Guild(Set<GuildMember> members, ItemStack guildBanner, GuildMember owner, String guildName, String guildPrefix, GuildStage guildStage, Integer score, List<ItemStack> bank, Integer bankSize, Map<GuildRank, Boolean> bankAccess, int guildEXP) {
		this.members = members;
		this.owner = owner;
		this.guildName = guildName;
		this.guildPrefix = guildPrefix;
		this.guildStage = guildStage;
		this.score = score;
		this.bank = bank;
		this.bankSize = bankSize;
		this.bankAccess = bankAccess;
		this.guildEXP = guildEXP;
		this.recalculateScore();
		this.guildBanner = new GuildBanner(this, guildBanner);
	}

	public Set<GuildMember> getMembers() {
		return this.members;
	}

	public GuildBanner getGuildBanner() {
		return this.guildBanner;
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

	public GuildStage getGuildStage() {
		return this.guildStage;
	}

	public Integer getBankSize() {
		return this.bankSize;
	}

	public List<ItemStack> getBank() {
		return this.bank;
	}

	public boolean canAccessBank(GuildRank rank) {
		return rank == GuildRank.OWNER ? true : this.bankAccess.get(rank);
	}

	public Map<GuildRank, Boolean> getBankAccess() {
		return this.bankAccess;
	}

	public int getGuildEXP() {
		return this.guildEXP;
	}

	public List<GuildMember> getMembersWithOwner() {
		List<GuildMember> membersWithOwner = new ArrayList<>(this.members);
		membersWithOwner.add(this.owner);
		return membersWithOwner;
	}

	public void setGuildStage(GuildStage guildStage) {
		this.guildStage = guildStage;
	}

	public void setBankSize(Integer size) {
		this.bankSize = size;
	}

	public void setBank(List<ItemStack> bank) {
		this.bank = bank;
	}

	public void setBankAccess(GuildRank rank, Boolean canAccess) {
		this.bankAccess.put(rank, canAccess);
	}

	public void setGuildEXP(int guildEXP) {
		this.guildEXP = guildEXP;
	}

	public void transferOwnership(GuildMember member) {
		this.members.add(new GuildMember(this.owner.getUUID(), GuildRank.OFFICER, this.owner.getScore(), this.owner.getLastKnownName()));
		this.owner = null;
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

	public int guildEXPToLevel() {
		return 0; //placeholder
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

	public void recalculateScore() {
		this.score = 0;
		for (GuildMember member : members) {
			score += member.getScore();
		}
		score += owner.getScore();
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
		return new Guild(newMembers, this.owner.clone(), new String(this.guildName), new String(this.guildPrefix), newItems, new Integer(this.bankSize), this.bankAccess);
	}

}
