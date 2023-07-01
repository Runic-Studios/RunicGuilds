package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.common.api.guilds.GuildRank;

import java.util.UUID;

public class MemberData {
    private String lastKnownName; // For when the player is offline
    private GuildRank rank;
    private Integer score = 0;
    private UUID uuid; // Of the PLAYER. Not a redundant field. Needed when we project member data

    @SuppressWarnings("unused")
    public MemberData() {
        // Default constructor for Spring
    }

    /**
     * Constructor to retrieve data from Redis
     *
     * @param uuid  of the player
     * @param name  of the player
     * @param rank  of the player
     * @param score of the player
     */
    public MemberData(UUID uuid, String name, GuildRank rank, Integer score) {
        this.uuid = uuid;
        this.lastKnownName = name;
        this.rank = rank;
        this.score = score;
    }

    public String getLastKnownName() {
        return lastKnownName;
    }

    public void setLastKnownName(String lastKnownName) {
        this.lastKnownName = lastKnownName;
    }

    public GuildRank getRank() {
        return rank;
    }

    public void setRank(GuildRank rank) {
        this.rank = rank;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

}
