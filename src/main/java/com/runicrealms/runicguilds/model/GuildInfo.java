package com.runicrealms.runicguilds.model;

import java.util.UUID;

/**
 * A container for several latency-sensitive fields about guilds
 */
public class GuildInfo {
    private final GuildUUID guildUUID;
    private UUID ownerUuid;
    private String name;
    private String prefix;
    private int exp;
    private int score;

    public GuildInfo(GuildUUID guildUUID, UUID ownerUuid, String name, String prefix, int exp, int score) {
        this.guildUUID = guildUUID;
        this.ownerUuid = ownerUuid;
        this.name = name;
        this.prefix = prefix;
        this.exp = exp;
        this.score = score;
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

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
