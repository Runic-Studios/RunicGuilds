package com.runicrealms.runicguilds.model;

import java.util.UUID;

/**
 * A container for several latency-sensitive fields about guilds
 */
public class GuildInfo {

    private final UUID uuid; // of the guild
    private String name;
    private String prefix;
    private int exp;

    public GuildInfo(UUID uuid, String name, String prefix, int exp) {
        this.uuid = uuid;
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

    public UUID getUuid() {
        return uuid;
    }
}
