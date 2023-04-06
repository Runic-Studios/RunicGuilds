package com.runicrealms.runicguilds.model;

/**
 * A simple container which contains a guild's UUID and score.
 * Used for banner spawning and leaderboards
 */
public class ScoreContainer {

    private final GuildUUID guildUUID;
    private final int score;

    public ScoreContainer(GuildUUID guildUUID, int score) {
        this.guildUUID = guildUUID;
        this.score = score;
    }

    public GuildUUID getGuildUUID() {
        return guildUUID;
    }

    public int getScore() {
        return score;
    }
}
