package com.runicrealms.runicguilds.model;

import com.runicrealms.runicguilds.guild.GuildRank;

import java.util.UUID;

public class MemberData {
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
     * @param rank  of the player
     * @param score of the player
     */
    public MemberData(UUID uuid, GuildRank rank, Integer score) {
        this.uuid = uuid;
        this.rank = rank;
        this.score = score;
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
