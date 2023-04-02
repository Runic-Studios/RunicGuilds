package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.event.MongoSaveEvent;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.api.DataAPI;
import com.runicrealms.runicguilds.util.GuildUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manager for handling guild data and keeping it consistent across the network
 * Uses Redis as our shared whiteboard for guild data, and uses pub/sub to ensure data is consistent
 * across the network
 *
 * @author Skyfallin
 */
public class DataManager implements DataAPI, Listener {

    public DataManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicGuilds.getInstance());
        /*
        Tab update task
         */
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicGuilds.getInstance(), this::updateGuildTabs, 100L, 20L);
        Bukkit.getLogger().info("[RunicGuilds] All guilds have been loaded!");
    }


    @Override
    public GuildData checkRedisForGuildData(UUID uuid, Jedis jedis) {
        String key = GuildData.getJedisKey(uuid);
        if (jedis.exists(key)) {
            jedis.expire(key, RunicCore.getRedisAPI().getExpireTime());
            return new GuildData(uuid, slot, jedis);
        }
        return null;
    }

    @Override
    public CompletableFuture<GuildData> loadGuildData(UUID uuid, Jedis jedis) {
        CompletableFuture<GuildData> future = new CompletableFuture<>();
        // Step 1: Check Redis
        GuildData guildData = checkRedisForGuildData(uuid, jedis);
        if (guildData != null) {
            future.complete(guildData);
            return future;
        }
        // Step 2: Check the Mongo database
        Query query = new Query();
        query.addCriteria(Criteria.where(GuildDataField.UUID.getField()).is(uuid));
        MongoTemplate mongoTemplate = RunicCore.getDataAPI().getMongoTemplate();
        GuildData result = mongoTemplate.findOne(query, GuildData.class);
        if (result != null) {
            result.writeToJedis(jedis);
            future.complete(result);
            return future;
        }
        // No data found!
        return null;
    }

    /**
     * Saves player guild info when the server is shut down
     * Works even if the player is now offline
     */
    @EventHandler
    public void onMongoSave(MongoSaveEvent event) {
//        // Cancel the task timer
//        RunicGuilds.getMongoTask().getTask().cancel();
//        // Manually save all sync
//        RunicGuilds.getMongoTask().saveAllToMongo();
        // todo: complete
        event.markPluginSaved("guilds");
    }

    /**
     * Updates the guild section of tab for all online players
     */
    private void updateGuildTabs() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            GuildUtil.updateGuildTabColumn(player);
        }
    }
}
