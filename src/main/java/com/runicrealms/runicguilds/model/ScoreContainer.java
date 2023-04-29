package com.runicrealms.runicguilds.model;

/**
 * A simple container which contains a guild's UUID and score.
 * Used for banner spawning and leaderboards
 */
public record ScoreContainer(GuildUUID guildUUID, int score) {

}
