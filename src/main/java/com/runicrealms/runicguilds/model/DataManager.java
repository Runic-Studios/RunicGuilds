package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.event.MongoSaveEvent;
import com.runicrealms.plugin.model.CorePlayerData;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.api.DataAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * Manager for handling guild data and keeping it consistent across the network
 * Uses Redis as our shared whiteboard for guild data, and uses pub/sub to ensure data is consistent
 * across the network
 *
 * @author Skyfallin
 */
public class DataManager implements DataAPI, Listener {
    // Maps a PLAYER uuid to a GUILD uuid
    private final HashMap<UUID, UUID> playerToGuildMap;
    // Contains some latency-sensitive data for fastest lookup.
    private final HashMap<UUID, GuildInfo> guildInfoMap;

    public DataManager() {
        playerToGuildMap = new HashMap<>();
        this.guildInfoMap = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, RunicGuilds.getInstance());
        /*
        Loads guilds into memory on startup
         */
        // todo: prevent startup until this is finished (does it need a delay?)
        Bukkit.getScheduler().runTaskLater(RunicGuilds.getInstance(), () -> {
            Set<UUID> guildUuids = getGuildUuidsFromMongo();
            if (guildUuids.isEmpty()) return; // No guilds created
            for (UUID guildUUID : guildUuids) {
                GuildData guildData = loadGuildDataNoBank(guildUUID);
                guildInfoMap.put(guildUUID, new GuildInfo(guildData));
            }

        }, 10 * 20L);
        Bukkit.getLogger().info("[RunicGuilds] All guilds have been loaded!");
    }

    @Override
    public void addGuildInfoToMemory(GuildInfo guildInfo) {
        this.guildInfoMap.put(guildInfo.getGuildUUID().getUUID(), guildInfo);
    }

    @Override
    public GuildData checkRedisForGuildData(UUID guildUUID, Jedis jedis) {
        String key = GuildData.getJedisKey(new GuildUUID(guildUUID));
        if (jedis.exists(key)) {
            jedis.expire(key, RunicCore.getRedisAPI().getExpireTime());
            return new GuildData(guildUUID, jedis);
        }
        return null;
    }

    @Override
    public String getGuildForPlayer(UUID uuid) {
        // 1. Load from memory
        if (playerToGuildMap.get(uuid) != null) {
            GuildInfo guildInfo = guildInfoMap.get(playerToGuildMap.get(uuid));
            return guildInfo.getName();
        }
        // 2. Load from Redis
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            String key = database + ":" + uuid + ":guild";
            if (jedis.exists(key)) {
                return jedis.get(key);
            } else {
                return "None";
            }
        }
        // 3. TODO: load from mongo
    }

    @Override
    public GuildInfo getGuildInfo(String name) {
        for (GuildInfo guildInfo : this.guildInfoMap.values()) {
            if (guildInfo.getName().equalsIgnoreCase(name))
                return guildInfo;
        }
        return null;
    }

    @Override
    public GuildInfo getGuildInfo(GuildUUID guildUUID) {
        return this.guildInfoMap.get(guildUUID.getUUID());
    }

    @Override
    public GuildInfo getGuildInfo(OfflinePlayer player) {
        if (this.playerToGuildMap.get(player.getUniqueId()) == null) return null;
        UUID guildUUID = this.playerToGuildMap.get(player.getUniqueId());
        return this.guildInfoMap.get(guildUUID);
    }

    @Override
    public HashMap<UUID, GuildInfo> getGuildInfoMap() {
        return guildInfoMap;
    }

    @Override
    public HashMap<UUID, UUID> getPlayerToGuildMap() {
        return playerToGuildMap;
    }

    @Override
    public List<ScoreContainer> loadAllGuildScores() {
        List<ScoreContainer> scoreContainers = new ArrayList<>();
        for (UUID guildUUID : this.guildInfoMap.keySet()) {
            scoreContainers.add(new ScoreContainer(guildInfoMap.get(guildUUID).getGuildUUID(), guildInfoMap.get(guildUUID).getScore()));
        }
        return scoreContainers;
    }

    @Override
    public GuildData loadGuildData(UUID guildUUID) {
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            // Step 1: Check Redis
            GuildData guildData = checkRedisForGuildData(guildUUID, jedis);
            if (guildData != null) return guildData;
//            // Step 2: Check the Mongo database
//            Query query = new Query();
//            query.addCriteria(Criteria.where(GuildDataField.GUILD_UUID.getField()).is(guildUUID));
//            MongoTemplate mongoTemplate = RunicCore.getDataAPI().getMongoTemplate();
//            GuildData result = mongoTemplate.findOne(query, GuildData.class);
//            if (result != null) {
//                result.writeToJedis(jedis);
//                return result;
//            }
            // No data found!
            return null;
        }
    }

    @Override
    public GuildData loadGuildDataNoBank(UUID guildUUID) {
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            // Step 1: Check Redis
            // todo: omit bank data
            GuildData guildData = checkRedisForGuildData(guildUUID, jedis);
            if (guildData != null) return guildData;
            // Step 2: Check the Mongo database
            Query query = new Query();
            query.addCriteria(Criteria.where(GuildDataField.GUILD_UUID.getField() + ".uuid").is(guildUUID));
            MongoTemplate mongoTemplate = RunicCore.getDataAPI().getMongoTemplate();
            GuildData result = mongoTemplate.findOne(query, GuildData.class);
            if (result != null) {
                result.writeToJedis(jedis);
                return result;
            }
        }
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
        // No data found!
        return null;
    }

    @Override
    public HashMap<UUID, MemberData> loadGuildMembers(GuildUUID guildUUID, Jedis jedis) {
        HashMap<UUID, MemberData> result = new HashMap<>();
        String key = GuildData.getJedisKey(guildUUID);
        Set<String> memberKeys = jedis.keys(key + ":members*");
        memberKeys.forEach(memberKey -> {
            String[] subKeys = memberKey.split(":");
            String uuidString = subKeys[subKeys.length - 1];
            MemberData memberData = loadMemberData(guildUUID, UUID.fromString(uuidString));
            result.put(memberData.getUuid(), memberData);
        });
        return result;
    }

    @Override
    public MemberData loadMemberData(GuildUUID guildUUID, UUID uuid) {
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            return new MemberData(guildUUID, uuid, jedis);
        }
    }

    @Override
    public GuildData loadSettingsData(GuildUUID guildUUID, Jedis jedis) {
        // todo: load the guild data and only project basic fields and settings
        return null;
    }

    @Override
    public void setGuildForPlayer(UUID uuid, String name) {
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        // Set the guild name
        if (!name.equalsIgnoreCase("none")) {
            GuildInfo guildInfo = this.getGuildInfo(name);
            if (guildInfo != null) {
                this.playerToGuildMap.put(uuid, guildInfo.getGuildUUID().getUUID());
            }
        } else {
            this.playerToGuildMap.remove(uuid);
        }
        // Save the data in Redis / core (which saves in Mongo)
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            String key = database + ":" + uuid + ":guild";
            if (name.equalsIgnoreCase("none")) {
                jedis.del(key);
            } else {
                jedis.set(key, name);
                jedis.expire(key, RunicCore.getRedisAPI().getExpireTime());
            }
            // Update the memoized guild name in core and prepare for mongo save
            CorePlayerData corePlayerData = RunicCore.getDataAPI().loadCorePlayerData(uuid);
            corePlayerData.setGuild(name);
            corePlayerData.writeToJedis(jedis);
        }
    }

    /**
     * @return the uuid of every guild in mongo
     */
    public Set<UUID> getGuildUuidsFromMongo() {
        MongoTemplate mongoTemplate = RunicCore.getDataAPI().getMongoTemplate();
        Query query = new Query();
        // Project Only the UUID field in the result documents
        query.fields().include(GuildDataField.GUILD_UUID.getField());
        List<GuildData> guildDataList = mongoTemplate.find(query, GuildData.class);
        Set<UUID> guildUUIDSet = new HashSet<>();
        for (GuildData guildData : guildDataList) {
            // Assuming the getGuildUUID() method returns the GuildUUID object and getUUID() returns the UUID
            guildUUIDSet.add(guildData.getGuildUUID().getUUID());
        }
        return guildUUIDSet;
    }

    /**
     * Saves player guild info when the server is shut down
     * Works even if the player is now offline
     */
    @EventHandler
    public void onMongoSave(MongoSaveEvent event) {
        // Cancel the task timer
        RunicGuilds.getMongoTask().getTask().cancel();
        // Manually save all data (flush players marked for save)
        RunicGuilds.getMongoTask().saveAllToMongo(() -> event.markPluginSaved("guilds"));
    }

}
