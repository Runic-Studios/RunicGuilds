package com.runicrealms.runicguilds.api;

import com.runicrealms.runicguilds.model.*;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DataAPI {

    /**
     * Creates a local, in-memory cache of the guild's info for fast lookup
     *
     * @param guildInfo some container of guild info
     */
    void addGuildInfoToMemory(GuildInfo guildInfo);

    /**
     * Checks redis to see if the currently selected GuildData is cached
     *
     * @param guildUUID of the guild to check
     * @param jedis     the jedis resource
     * @return a GuildData object if it is found in redis
     */
    GuildData checkRedisForGuildData(GuildUUID guildUUID, Jedis jedis);

    /**
     * Returns a container of basic guild info for guild (if it exists)
     *
     * @param name of the guild to lookup
     * @return some basic info about their guilds, like exp, name, etc.
     */
    GuildInfo getGuildInfo(String name);

    /**
     * Returns a container of basic guild info for player (if it exists)
     *
     * @param uuid of the player to lookup
     * @return some basic info about their guilds, like exp, name, etc.
     */
    GuildInfo getGuildInfo(UUID uuid);

    /**
     * Returns a container of basic guild info for guild (if it exists)
     *
     * @param guildUUID of the guild
     * @return some basic info like exp, name, etc.
     */
    GuildInfo getGuildInfo(GuildUUID guildUUID);

    /**
     * Checks Redis for all guilds' score field, then returns a list of containers
     * with the guild's uuid and score. Used for banners/leaderboards
     *
     * @param jedis a new jedis resource
     * @return a future, which will eventually have the scores
     */
    CompletableFuture<List<ScoreContainer>> loadAllGuildScores(Jedis jedis);

    /**
     * Loads the guild data from redis and/or mongo (if it exists!)
     *
     * @param guildUUID of the GUILD
     * @param jedis     a new jedis resource
     * @return a future, which will eventually have the data
     */
    CompletableFuture<GuildData> loadGuildData(GuildUUID guildUUID, Jedis jedis);

    /**
     * Loads the guild data from redis and/or mongo (if it exists!)
     * Uses projection to exclude Bank data from the future
     *
     * @param guildUUID of the GUILD
     * @param jedis     a new jedis resource
     * @return a future, which will eventually have the data
     */
    CompletableFuture<GuildData> loadGuildDataNoBank(GuildUUID guildUUID, Jedis jedis);

    /**
     * Loads only the guild member map from redis/mongo
     */
    CompletableFuture<HashMap<UUID, MemberData>> loadGuildMembers(GuildUUID guildUUID, Jedis jedis);

    /**
     * Loads the data for a single guild member. Checks Redis first, then falls back to a projection
     * in Mongo. Useful for retrieving the player's rank or score
     *
     * @param guildUUID of the guild
     * @param uuid      of the guild member
     * @param jedis     the jedis resource
     * @return the data for the guild member
     */
    CompletableFuture<MemberData> loadMemberData(GuildUUID guildUUID, UUID uuid, Jedis jedis);

    /**
     * Loads the settings for this guild (currently only bank settings)
     *
     * @param guildUUID of the guild
     * @param jedis     a new jedis resource
     * @return the settings of the guild
     */
    CompletableFuture<GuildData> loadSettingsData(GuildUUID guildUUID, Jedis jedis);

    /**
     * Renames the guild by updating its shared field in redis
     *
     * @param guildUUID uuid of the guild
     * @param name      the new name of the guild
     * @param jedis     a new Jedis resource
     */
    void renameGuildInRedis(GuildUUID guildUUID, String name, Jedis jedis);

    /**
     * Player's have a 'foreign' key that specifies the name of their guild.
     * This is much faster to lookup than parsing through the list of members
     *
     * @param uuid of the player
     * @param name of the guild "None" if none
     */
    void setGuildForPlayer(UUID uuid, String name, Jedis jedis);

}
