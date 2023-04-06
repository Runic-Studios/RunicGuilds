package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.database.event.MongoSaveEvent;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.api.DataAPI;
import com.runicrealms.runicguilds.util.GuildUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
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
    // Maps a PLAYER uuid to a GUILD uuid
    private final HashMap<UUID, GuildUUID> playerToGuildMap;
    // Contains some latency-sensitive data for fastest lookup.
    private final HashMap<GuildUUID, GuildInfo> guildInfoMap; // todo: write to this on jedis lookups for specific fields

    public DataManager() {
        playerToGuildMap = new HashMap<>();
        this.guildInfoMap = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, RunicGuilds.getInstance());
        /*
        Tab update task
         */
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicGuilds.getInstance(), this::updateGuildTabs, 100L, 100L);
        Bukkit.getLogger().info("[RunicGuilds] All guilds have been loaded!");
    }

    @Override
    public void addGuildInfoToMemory(GuildInfo guildInfo) {
        this.guildInfoMap.put(guildInfo.getGuildUUID(), guildInfo);
    }

    @Override
    public GuildData checkRedisForGuildData(GuildUUID guildUUID, Jedis jedis) {
        String key = GuildData.getJedisKey(guildUUID);
        if (jedis.exists(key)) {
            jedis.expire(key, RunicCore.getRedisAPI().getExpireTime());
            return new GuildData(guildUUID, jedis);
        }
        return null;
    }

    @Override
    public GuildInfo getGuildInfo(UUID uuid) {
        if (this.playerToGuildMap.get(uuid) == null) return null;
        GuildUUID guildUUID = this.playerToGuildMap.get(uuid);
        return this.guildInfoMap.get(guildUUID);
    }

    @Override
    public GuildInfo getGuildInfo(GuildUUID guildUUID) {
        return this.guildInfoMap.get(guildUUID);
    }

    @Override
    public CompletableFuture<List<ScoreContainer>> loadAllGuildScores(Jedis jedis) {
        return null;
    }

    @Override
    public CompletableFuture<GuildData> loadGuildData(GuildUUID guildUUID, Jedis jedis) {
        CompletableFuture<GuildData> future = new CompletableFuture<>();
        // Step 1: Check Redis
        GuildData guildData = checkRedisForGuildData(guildUUID, jedis);
        if (guildData != null) {
            future.complete(guildData);
            return future;
        }
        // Step 2: Check the Mongo database
        Query query = new Query();
        query.addCriteria(Criteria.where(GuildDataField.GUILD_UUID.getField()).is(guildUUID));
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

    @Override
    public CompletableFuture<GuildData> loadGuildDataNoBank(GuildUUID guildUUID, Jedis jedis) {
        // todo: redis
        // todo: mongo projection
        /*
                // Find our top-level document
        Query query = new Query(Criteria.where(CharacterField.PLAYER_UUID.getField()).is(this.uuid));
        // Project only the fields we need
        query.fields()
                .include("coreCharacterDataMap." + slot + "." + CharacterField.CLASS_TYPE.getField())
                .include("coreCharacterDataMap." + slot + "." + CharacterField.CLASS_LEVEL.getField())
                .include("coreCharacterDataMap." + slot + "." + CharacterField.CLASS_EXP.getField());
        CorePlayerData corePlayerData = RunicCore.getDataAPI().getMongoTemplate().findOne(query, CorePlayerData.class);
         */
        return null;
    }

    @Override
    public CompletableFuture<HashMap<UUID, MemberData>> loadGuildMembers(GuildUUID guildUUID, Jedis jedis) {
        return null;
    }

    @Override
    public CompletableFuture<MemberData> loadMemberData(GuildUUID guildUUID, UUID uuid, Jedis jedis) {
        return null;
    }

    @Override
    public void renameGuildInRedis(GuildUUID guildUUID, String name, Jedis jedis) {
        String root = GuildData.getJedisKey(guildUUID);
        // Write name
        jedis.set(root + ":" + GuildDataField.NAME.getField(), name);

    }

    public HashMap<GuildUUID, GuildInfo> getGuildInfoMap() {
        return guildInfoMap;
    }

    public HashMap<UUID, GuildUUID> getPlayerToGuildMap() {
        return playerToGuildMap;
    }

    @EventHandler(priority = EventPriority.LOW) // early
    public void onCharacterSelect(CharacterSelectEvent event) {
//        UUID uuid = event.getPlayer().getUniqueId();
//        GuildUUID guildUUID = findIfPlayerHasUUID
//        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
//            CompletableFuture<GuildData> future = loadGuildData(uuid, jedis);
//            // todo: create an index for player UUID in members and/or owner
//            // todo: no need to lookup entire guild here. just load the guild name
//            future.whenComplete((GuildData guildData, Throwable ex) -> {
//                if (ex != null) {
//                    Bukkit.getLogger().log(Level.SEVERE, "RunicGuilds failed to load on select for player " + uuid);
//                    ex.printStackTrace();
//                } else {
//                    this.playerToGuildMap.put(uuid, guildData.getUuid());
//                }
//            });
//        }
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
