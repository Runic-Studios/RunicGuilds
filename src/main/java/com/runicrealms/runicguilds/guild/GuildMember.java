package com.runicrealms.runicguilds.guild;

import java.util.UUID;

public class GuildMember implements Cloneable {

    private final UUID uuid;
    private GuildRank rank;
    private Integer score;
    private final String name;

    public GuildMember(UUID uuid, GuildRank rank, Integer score, String name) {
        this.uuid = uuid;
        this.rank = rank;
        this.score = score;
        this.name = name;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public GuildRank getRank() {
        return this.rank;
    }

    public Integer getScore() {
        return this.score;
    }

    public void setRank(GuildRank rank) {
        this.rank = rank;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getLastKnownName() {
        return this.name;
    }

    @Override
    public GuildMember clone() {
        return new GuildMember(this.uuid, this.rank, this.score, this.name);
    }

}