package com.runicrealms.runicguilds.model;

import com.runicrealms.runicguilds.guild.banner.GuildBanner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A container for several latency-sensitive fields about guilds.
 * This is the closest thing we have to a local, in-memory cache of guild data
 * Stores all basic fields, but does not store high memory-footprint data like members or bank
 *
 * @author Skyfallin
 */
@SuppressWarnings("unused")
public class GuildInfo {
    private final GuildUUID guildUUID;
    private final List<UUID> membersUuids = new ArrayList<>(); // todo: needs to have a simple wrapper for in-memory members
    private String name;
    private String prefix;
    private int exp;
    private int score;
    private GuildBanner guildBanner;
    private SettingsData settingsData;
    private UUID ownerUuid;

    /**
     * Updates our in-memory guild info cache from some retrieved GuildData object from Redis/Mongo
     *
     * @param guildData an object retrieved from a database call
     */
    public GuildInfo(GuildData guildData) {
        this.guildUUID = guildData.getGuildUUID();
        this.ownerUuid = guildData.getOwnerUuid();
        this.name = guildData.getName();
        this.prefix = guildData.getPrefix();
        this.exp = guildData.getExp();
        this.score = guildData.calculateGuildScore();
        this.guildBanner = guildData.getGuildBanner();
        this.settingsData = guildData.getSettingsData();
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public GuildBanner getGuildBanner() {
        return guildBanner;
    }

    public void setGuildBanner(GuildBanner guildBanner) {
        this.guildBanner = guildBanner;
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

    public SettingsData getSettingsData() {
        return settingsData;
    }

    public void setSettingsData(SettingsData settingsData) {
        this.settingsData = settingsData;
    }
}
