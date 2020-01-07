package com.runicrealms.runicguilds.guilds;

public enum GuildRank {
	
	MEMBER("Member", 4), RECRUITER("Recruiter", 3), OFFICER("Officer", 2), OWNER("Owner", 1);
	
	private String name;
	private Integer rank;
	
	private GuildRank(String name, Integer rank) {
		this.name = name;
		this.rank = rank;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Integer getRankNumber() {
		return this.rank;
	}
	
	public void setRankNumber(Integer rank) {
		this.rank = rank;
	}
	
	public static GuildRank getByName(String name) {
		switch (name) {
		case "Member":
			return GuildRank.MEMBER;
		case "Officer":
			return GuildRank.OFFICER;
		case "Owner":
			return GuildRank.OWNER;
		}
		return GuildRank.MEMBER;
	}
	
}
