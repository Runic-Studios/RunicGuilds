package com.runicrealms.plugin.runicguilds.model;

import java.util.UUID;

/**
 * A simple container which contains a guild's UUID and score.
 * Used for banner spawning and leaderboards
 */
public record ScoreContainer(UUID guildUUID, int score) {

}
