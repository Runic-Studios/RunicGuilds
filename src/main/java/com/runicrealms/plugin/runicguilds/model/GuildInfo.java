package com.runicrealms.plugin.runicguilds.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
    private final UUID guildUUID;
    private Set<UUID> membersUuids;
    private String name;
    private String prefix;
    private int exp;
    private int score;
    private String serializedBanner;
    private UUID ownerUuid;
    private Map<String, Integer> workOrderMap = new HashMap<>();

    /**
     * Updates our in-memory guild info cache from some retrieved GuildData object from Redis/Mongo
     *
     * @param guildData an object retrieved from a database call
     */
    public GuildInfo(GuildData guildData) {
        this.guildUUID = guildData.getUUID();
        this.membersUuids = new HashSet<>(guildData.getMemberDataMap().keySet()); // Copy set so that it is mutable
        this.ownerUuid = guildData.getOwnerUuid();
        this.name = guildData.getName();
        this.prefix = guildData.getPrefix();
        this.exp = guildData.getExp();
        this.score = guildData.calculateGuildScore();
        this.serializedBanner = guildData.getSerializedBanner();
        // Update order map with persistent data (if it exists)
        if (guildData.getWorkOrderMap() != null) {
            this.workOrderMap = guildData.getWorkOrderMap();
        }
    }

    public Map<String, Integer> getWorkOrderMap() {
        return workOrderMap;
    }

    public void setWorkOrderMap(Map<String, Integer> workOrderMap) {
        this.workOrderMap = workOrderMap;
    }

    public Set<UUID> getMembersUuids() {
        return membersUuids;
    }

    public void setMembersUuids(Set<UUID> membersUuids) {
        this.membersUuids = membersUuids;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public String getSerializedBanner() {
        return serializedBanner;
    }

    public void setSerializedBanner(String serializedBanner) {
        this.serializedBanner = serializedBanner;
    }

    public UUID getUUID() {
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
