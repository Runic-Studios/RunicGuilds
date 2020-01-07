package com.runicrealms.runicguilds.guilds;

import java.util.Set;

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
	
}
