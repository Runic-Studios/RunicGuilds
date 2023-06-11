package com.runicrealms.runicguilds.api;

import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.model.GuildInfo;
import com.runicrealms.runicguilds.model.MemberData;
import com.runicrealms.runicguilds.model.ScoreContainer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DataAPI {

    /**
     * Creates a local, in-memory cache of the guild's info for fast lookup
     *
     * @param guildInfo some container of guild info
     */
    void addGuildInfoToMemory(GuildInfo guildInfo);

    /**
     * Scans ALL guild documents to find the document matching player
     *
     * @param uuid of the player
     * @return the player's stored guild in Redis/Mongo
     */
    String loadGuildForPlayer(UUID uuid);

    /**
     * Returns a container of basic guild info for guild (if it exists)
     *
     * @param name of the guild to lookup
     * @return some basic info about the guild, like exp, name, etc.
     */
    GuildInfo getGuildInfo(String name);

    /**
     * Returns a container of basic guild info for the GUILD UUID (breaks if you pass in player uuid)
     *
     * @param guildUUID of the GUILD to lookup
     * @return some basic info about the guild, like exp, name, etc.
     */
    GuildInfo getGuildInfo(UUID guildUUID);

    /**
     * Returns a container of basic guild info for player (if it exists)
     *
     * @param player to lookup
     * @return some basic info about the PLAYER'S guild, like exp, name, etc.
     */
    GuildInfo getGuildInfo(Player player);

    /**
     * @return all in-memory guilds. Used for guild creation to ensure names are unique
     */
    HashMap<UUID, GuildInfo> getGuildInfoMap();

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
     * Loads the data for a single guild member. Checks Redis first, then falls back to a projection
     * in Mongo. Useful for retrieving the player's rank or score
     *
     * @param guildUUID of the guild
     * @param uuid      of the guild member
     * @return the data for the guild member
     */
    MemberData loadMemberData(UUID guildUUID, UUID uuid);

    /**
     * Loads the map of all members from the guildUUID
     *
     * @param guildUUID of the guild
     * @return a map of all member uuids to their data
     */
    Map<UUID, MemberData> loadMemberDataMap(UUID guildUUID);

}
