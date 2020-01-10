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

	public static GuildRank getByNumber(Integer number) {
		switch (number) {
			case 1:
				return GuildRank.OWNER;
			case 2:
				return GuildRank.OFFICER;
			case 3:
				return GuildRank.RECRUITER;
			case 4:
				return GuildRank.MEMBER;
		}
		return GuildRank.MEMBER;
	}
	
}
