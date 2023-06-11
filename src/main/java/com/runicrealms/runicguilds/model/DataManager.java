package com.runicrealms.runicguilds.model;

import co.aikar.taskchain.TaskChain;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.api.WriteCallback;
import com.runicrealms.plugin.rdb.event.MongoSaveEvent;
import com.runicrealms.plugin.taskchain.TaskChainUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.api.DataAPI;
import com.runicrealms.runicguilds.api.GuildWriteOperation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Manager for handling guild data and keeping it consistent across the network
 * Uses Redis as our shared whiteboard for guild data, and uses pub/sub to ensure data is consistent
 * across the network
 *
 * @author Skyfallin
 */
public class DataManager implements DataAPI, GuildWriteOperation, Listener {
    // Contains some latency-sensitive data for fastest lookup.
    private final HashMap<UUID, GuildInfo> guildInfoMap;

    public DataManager() {
        this.guildInfoMap = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, RunicGuilds.getInstance());
        /*
        Loads guilds into memory on startup
         */
        // todo: prevent startup until this is finished (does it need a delay?)
        Bukkit.getScheduler().runTaskLater(RunicGuilds.getInstance(), () -> {
            Set<GuildData> guildDataSet = getGuildDataFromMongo();
            if (guildDataSet.isEmpty()) return; // No guilds created
            guildDataSet.forEach(guildData -> guildInfoMap.put(guildData.getUUID(), new GuildInfo(guildData)));
        }, 10 * 20L);
        Bukkit.getLogger().info("[RunicGuilds] All guilds have been loaded!");
    }

    @Override
    public void addGuildInfoToMemory(GuildInfo guildInfo) {
        this.guildInfoMap.put(guildInfo.getUUID(), guildInfo);
    }

    @Override
    public String loadGuildForPlayer(UUID uuid) {
        // 2. Search all in-memory guilds
        return findPlayerGuild(uuid);
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
    public GuildInfo getGuildInfo(UUID guildUUID) {
        return this.guildInfoMap.get(guildUUID);
    }

    @Override
    public GuildInfo getGuildInfo(Player player) {
        String guildName = loadGuildForPlayer(player.getUniqueId());
        if (guildName == null) return null;
        return getGuildInfo(guildName);
    }

    @Override
    public HashMap<UUID, GuildInfo> getGuildInfoMap() {
        return guildInfoMap;
    }

    @Override
    public List<ScoreContainer> loadAllGuildScores() {
        List<ScoreContainer> scoreContainers = new ArrayList<>();
        for (UUID guildUUID : this.guildInfoMap.keySet()) {
            scoreContainers.add(new ScoreContainer(guildInfoMap.get(guildUUID).getUUID(), guildInfoMap.get(guildUUID).getScore()));
        }
        return scoreContainers;
    }

    @Override
    public GuildData loadGuildData(UUID guildUUID) {
        // Step 1: Check the mongo database
        Query query = new Query();
        query.addCriteria(Criteria.where(GuildDataField.GUILD_UUID.getField()).is(guildUUID));
        MongoTemplate mongoTemplate = RunicDatabase.getAPI().getDataAPI().getMongoTemplate();
        return mongoTemplate.findOne(query, GuildData.class);
    }

    @Override
    public MemberData loadMemberData(UUID guildUUID, UUID uuid) {
        // Step 1: Check the Mongo database
        Query query = new Query(Criteria.where(GuildDataField.GUILD_UUID.getField()).is(guildUUID));
        // Project only the fields we need
        query.fields().include("memberDataMap");
        MongoTemplate mongoTemplate = RunicDatabase.getAPI().getDataAPI().getMongoTemplate();
        GuildData guildData = mongoTemplate.findOne(query, GuildData.class);
        if (guildData != null && guildData.getMemberDataMap().get(uuid) != null) {
            return guildData.getMemberDataMap().get(uuid);
        }
        return null; // Oh-no!
    }

    @Override
    public Map<UUID, MemberData> loadMemberDataMap(UUID guildUUID) {
        // Step 1: Check the Mongo database
        Query query = new Query(Criteria.where(GuildDataField.GUILD_UUID.getField()).is(guildUUID));
        // Project only the fields we need
        query.fields().include("memberDataMap");
        MongoTemplate mongoTemplate = RunicDatabase.getAPI().getDataAPI().getMongoTemplate();
        GuildData guildData = mongoTemplate.findOne(query, GuildData.class);
        if (guildData != null && guildData.getMemberDataMap() != null) {
            return guildData.getMemberDataMap();
        }
        return null;
    }

    /**
     * Matches a player to their guild by parsing through the in-memory guild set.
     * This will become inefficient as the number of guilds increases
     *
     * @param playerUuid to search
     * @return the name of their guild if it is found, else null
     */
    public String findPlayerGuild(UUID playerUuid) {
        for (GuildInfo guildInfo : this.guildInfoMap.values()) {
            Set<UUID> uuids = guildInfo.getMembersUuids();
            if (uuids.contains(playerUuid)) {
                // Found a match!
                return guildInfo.getName();
            }
        }
        return null;
    }

    /**
     * @return EVERY document in the guilds collection
     */
    public Set<GuildData> getGuildDataFromMongo() {
        MongoTemplate mongoTemplate = RunicDatabase.getAPI().getDataAPI().getMongoTemplate();
        Query query = new Query();
        List<GuildData> guildDataList = mongoTemplate.find(query, GuildData.class);
        return new HashSet<>(guildDataList);
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

    @Override
    public <T> void updateGuildData(UUID guildUUID, String fieldName, T newValue, WriteCallback callback) {
        MongoTemplate mongoTemplate = RunicDatabase.getAPI().getDataAPI().getMongoTemplate();
        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> {
                    // Define a query to find the GuildData for this player
                    Query query = new Query();
                    query.addCriteria(Criteria.where(GuildDataField.GUILD_UUID.getField()).is(guildUUID));

                    // Define an update to set the specific field
                    Update update = new Update();
                    update.set(fieldName, newValue);

                    // Execute the update operation
                    return mongoTemplate.updateFirst(query, update, GuildData.class);
                })
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicGuilds failed to write to " + fieldName + "!")
                .syncLast(updateResult -> callback.onWriteComplete())
                .execute();
    }
}
