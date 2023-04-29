package com.runicrealms.runicguilds.api;

import com.runicrealms.runicguilds.model.*;
import org.bukkit.OfflinePlayer;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
    GuildData checkRedisForGuildData(UUID guildUUID, Jedis jedis);

    /**
     * @param uuid of the player
     * @return the player's stored guild in Redis/Mongo
     */
    String getGuildForPlayer(UUID uuid);

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
     * @param guildUUID of the guild to lookup
     * @return some basic info about their guilds, like exp, name, etc.
     */
    GuildInfo getGuildInfo(GuildUUID guildUUID);

    /**
     * Returns a container of basic guild info for player (if it exists)
     *
     * @param player to lookup
     * @return some basic info about their guilds, like exp, name, etc.
     */
    GuildInfo getGuildInfo(OfflinePlayer player);

    /**
     * @return all in-memory guilds. Used for guild creation to ensure names are unique
     */
    HashMap<UUID, GuildInfo> getGuildInfoMap();

    /**
     * Data structure that maps a PLAYER uuid to a GUILD uuid
     *
     * @return map of player uuid to guild uuid
     */
    HashMap<UUID, UUID> getPlayerToGuildMap();

    /**
     * Checks Redis for all guilds' score field, then returns a list of containers
     * with the guild's uuid and score. Used for banners/leaderboards
     *
     * @return all guild scores
     */
    List<ScoreContainer> loadAllGuildScores();

    /**
     * Loads the guild data from redis and/or mongo (if it exists!)
     *
     * @param guildUUID of the GUILD
     * @return a GuildData object
     */
    GuildData loadGuildData(UUID guildUUID);

    /**
     * Loads the guild data from redis and/or mongo (if it exists!)
     * Uses projection to exclude Bank data from the result
     *
     * @param guildUUID of the GUILD
     * @return a GuildData object with no bank data
     */
    GuildData loadGuildDataNoBank(GuildUUID guildUUID);

    /**
     * Loads only the guild member map from redis/mongo
     */
    HashMap<UUID, MemberData> loadGuildMembers(GuildUUID guildUUID, Jedis jedis);

    /**
     * Loads the data for a single guild member. Checks Redis first, then falls back to a projection
     * in Mongo. Useful for retrieving the player's rank or score
     *
     * @param guildUUID of the guild
     * @param uuid      of the guild member
     * @return the data for the guild member
     */
    MemberData loadMemberData(GuildUUID guildUUID, UUID uuid);

    /**
     * Loads the settings for this guild (currently only bank settings)
     *
     * @param guildUUID of the guild
     * @param jedis     a new jedis resource
     * @return the settings of the guild
     */
    GuildData loadSettingsData(GuildUUID guildUUID, Jedis jedis);

    /**
     * Player's have a 'foreign' key that specifies the name of their guild.
     * This is much faster to lookup than parsing through the list of members
     *
     * @param uuid of the player
     * @param name of the guild "None" if none
     */
    void setGuildForPlayer(UUID uuid, String name);

}
