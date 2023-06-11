package com.runicrealms.runicguilds.model;

import co.aikar.taskchain.TaskChain;
import com.mongodb.bulk.BulkWriteResult;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.api.MongoTaskOperation;
import com.runicrealms.plugin.rdb.api.WriteCallback;
import com.runicrealms.plugin.taskchain.TaskChainUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import redis.clients.jedis.Jedis;

import java.util.Set;
import java.util.UUID;

/**
 * Manages the task that writes data from Redis --> MongoDB periodically
 *
 * @author Skyfallin
 */
public class MongoTask implements MongoTaskOperation {
    private static final int MONGO_TASK_TIME = 30; // seconds
    private final BukkitTask task;

    public MongoTask() {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously
                (
                        RunicGuilds.getInstance(),
                        () -> saveAllToMongo(() -> {
                        }),
                        MONGO_TASK_TIME * 20L,
                        MONGO_TASK_TIME * 20L
                );
    }

    @Override
    public String getCollectionName() {
        return "guilds";
    }

    @Override
    public Query getQuery(UUID guildUUID) {
        return new Query(Criteria.where(GuildDataField.GUILD_UUID.getField()).is(guildUUID));
    }

    @Override
    public <T> Update getUpdate(T obj) {
        GuildData guildData = (GuildData) obj;
        Update update = new Update();
        /*
        Only update keys in mongo with data in memory.
        If, for example, there's 5 characters with data in mongo but only 1 in redis,
        this only updates the character with new data.
         */
        // Update exp
        update.set("exp", guildData.getExp());
        // Update member data
        update.set("memberDataMap", guildData.getMemberDataMap());
        return update;
    }

    /**
     * A task that saves all guilds with the 'markedForSave:{plugin}' key in redis to mongo.
     * Here's how this works:
     * - Whenever a guild's data is written to Jedis, their UUID is added to a set in Jedis
     * - When this task runs, it checks for all guilds who have not been saved from Jedis --> Mongo and flushes the data, saving each entry
     * - The guild is then no longer marked for save.
     */
    @Override
    public void saveAllToMongo(WriteCallback callback) {
        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(this::sendBulkOperation)
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicGuilds failed to write to Mongo!")
                .syncLast(bulkWriteResult -> {
                    if (bulkWriteResult.wasAcknowledged()) {
                        Bukkit.getLogger().info("RunicGuilds modified " + bulkWriteResult.getModifiedCount() + " documents.");
                    }
                    callback.onWriteComplete();
                })
                .execute();
    }

    @Override
    public BulkWriteResult sendBulkOperation() {
        try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
            Set<String> guildsToSave = jedis.smembers(getJedisSet());
            if (guildsToSave.isEmpty()) return BulkWriteResult.unacknowledged();
            BulkOperations bulkOperations = RunicDatabase.getAPI().getDataAPI().getMongoTemplate().bulkOps(BulkOperations.BulkMode.UNORDERED, getCollectionName());
            for (String uuidString : guildsToSave) {
                UUID guildUUID = UUID.fromString(uuidString);
                // Load their data
                GuildData guildData = RunicGuilds.getDataAPI().loadGuildData(guildUUID);
                // Guild is no longer marked for save
                jedis.srem(getJedisSet(), guildUUID.toString());
                // Find the correct document to update
                bulkOperations.updateOne(getQuery(guildUUID), getUpdate(guildData));
            }
            return bulkOperations.execute();
        }
    }

    public BukkitTask getTask() {
        return task;
    }

}
