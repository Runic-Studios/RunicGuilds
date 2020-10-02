package com.runicrealms.runicguilds.guilds;

public enum GuildRank {

	OWNER("owner", "Owner", "Owners", 1, true),
	OFFICER("officer", "Officer", "Officers", 2, true),
	RECRUITER("recruiter", "Recruiter", "Recruiters", 3, true),
	TRUSTED("trusted", "Trusted", "Trusted", 4, true),
	MEMBER("member", "Member", "Members", 5, false);

	private String identifier;
	private String name;
	private String plural;
	private Integer rank;
	private Boolean defaultBankAccess;
	
	GuildRank(String identifier, String name, String plural, Integer rank, Boolean defaultBankAccess) {
		this.identifier = identifier;
		this.name = name;
		this.plural = plural;
		this.rank = rank;
		this.defaultBankAccess = defaultBankAccess;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public String getName() {
		return this.name;
	}

	public String getPlural() {
		return this.plural;
	}
	
	public Integer getRankNumber() {
		return this.rank;
	}

	public Boolean canAccessBankByDefault() {
		return this.defaultBankAccess;
	}

	public static GuildRank getByIdentifier(String identifier) {
		for (GuildRank rank : values()) {
			if (rank.getIdentifier().equalsIgnoreCase(identifier)) {
				return rank;
			}
		}
		return null;
	}
	
	public static GuildRank getByName(String name) {
		switch (name) {
			case "Member":
				return GuildRank.MEMBER;
			case "Trusted":
				return GuildRank.TRUSTED;
			case "Recruiter":
				return GuildRank.RECRUITER;
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
				return GuildRank.TRUSTED;
			case 5:
				return GuildRank.MEMBER;
		}
		return GuildRank.MEMBER;
	}
	
}
