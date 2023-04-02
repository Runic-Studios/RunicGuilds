package com.runicrealms.runicguilds.api;

import com.runicrealms.runicguilds.model.GuildData;
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
     * Loads the guild data from redis and/or mongo (if it exists!)
     *
     * @param uuid  of the GUILD
     * @param jedis a new jedis resource
     * @return a future, which will eventually have the data
     */
    CompletableFuture<GuildData> loadGuildData(UUID uuid, Jedis jedis);
}
