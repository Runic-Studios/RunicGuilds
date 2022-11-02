package com.runicrealms.runicguilds.guild;

public enum GuildRank {

    OWNER("owner", "Owner", "Owners", 1, true),
    OFFICER("officer", "Officer", "Officers", 2, true),
    RECRUITER("recruiter", "Recruiter", "Recruiters", 3, true),
    MEMBER("member", "Member", "Members", 4, true),
    RECRUIT("recruit", "Recruit", "Recruits", 5, false);

    private final String identifier;
    private final String name;
    private final String plural;
    private final Integer rank;
    private final Boolean defaultBankAccess;

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
            case "Recruit":
                return GuildRank.RECRUIT;
            case "Member":
                return GuildRank.MEMBER;
            case "Recruiter":
                return GuildRank.RECRUITER;
            case "Officer":
                return GuildRank.OFFICER;
            case "Owner":
                return GuildRank.OWNER;
        }
        return GuildRank.RECRUIT;
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
            case 5:
                return GuildRank.RECRUIT;
        }
        return GuildRank.RECRUIT;
    }

}
