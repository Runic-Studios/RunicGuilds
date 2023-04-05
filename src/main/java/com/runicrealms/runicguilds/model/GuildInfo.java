package com.runicrealms.runicguilds.model;

/**
 * A container for several latency-sensitive fields about guilds
 */
public class GuildInfo {
    private final GuildUUID guildUUID;
    private String name;
    private String prefix;
    private int exp;

    public GuildInfo(GuildUUID guildUUID, String name, String prefix, int exp) {
        this.guildUUID = guildUUID;
        this.name = name;
        this.prefix = prefix;
        this.exp = exp;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public GuildUUID getGuildUUID() {
        return guildUUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
