package com.runicrealms.runicguilds.api;

import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.model.GuildInfo;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DataAPI {

    /**
     * Checks redis to see if the currently selected GuildData is cached
     *
     * @param uuid  of the guild to check
     * @param jedis the jedis resource
     * @return a GuildData object if it is found in redis
     */
    GuildData checkRedisForGuildData(UUID uuid, Jedis jedis);

    /**
     * Returns a container of basic guild info for player (if it exists)
     *
     * @param player the player to lookup
     * @return some basic info about their guilds, like exp, name, etc.
     */
    GuildInfo getGuildInfo(Player player);

    /**
     * Returns a container of basic guild info for guild (if it exists)
     *
     * @param guildUuid of the guild
     * @return some basic info like exp, name, etc.
     */
    GuildInfo getGuildInfo(UUID guildUuid);

    /**
     * Loads the guild data from redis and/or mongo (if it exists!)
     *
     * @param uuid  of the GUILD
     * @param jedis a new jedis resource
     * @return a future, which will eventually have the data
     */
    CompletableFuture<GuildData> loadGuildData(UUID uuid, Jedis jedis);
}
