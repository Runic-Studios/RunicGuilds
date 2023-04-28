package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.model.SessionDataRedis;
import com.runicrealms.runicguilds.guild.GuildRank;
import redis.clients.jedis.Jedis;

import java.util.*;

public class MemberData implements SessionDataRedis {
    public static final List<String> FIELDS = new ArrayList<String>() {{
        add(GuildDataField.RANK.getField());
        add(GuildDataField.SCORE.getField());
    }};
    private GuildRank rank;
    private Integer score;
    private UUID uuid; // Not a redundant field. Needed when we project member data

    @SuppressWarnings("unused")
    public MemberData() {
        // Default constructor for Spring
    }

    /**
     * Constructor to retrieve data from Redis
     *
     * @param uuid  of the player
     * @param rank  of the player
     * @param score of the player
     */
    public MemberData(UUID uuid, GuildRank rank, Integer score) {
        this.uuid = uuid;
        this.rank = rank;
        this.score = score;
    }

    /**
     * @param guildUUID of the guild
     * @param uuid      of the member
     * @return the root key in Jedis
     */
    public static String getJedisKey(GuildUUID guildUUID, UUID uuid) {
        return "guilds:" + guildUUID.getUUID() + ":members:" + uuid;
    }

    @Override
    public Map<String, String> getDataMapFromJedis(UUID uuid, Jedis jedis, int... ignored) {
        return null;
    }

    @Override
    public List<String> getFields() {
        return FIELDS;
    }

    @Override
    public Map<String, String> toMap(UUID uuid, int... ints) {
        return new HashMap<String, String>() {{
            put(GuildDataField.RANK.getField(), rank.getIdentifier());
            put(GuildDataField.SCORE.getField(), String.valueOf(score));
        }};
    }

    @Override
    public void writeToJedis(UUID guildUUID, Jedis jedis, int... ignored) {

    }

    /**
     * ?
     *
     * @param guildUUID
     * @param playerUuid
     * @param jedis
     * @return
     */
    public Map<String, String> getDataMapFromRedis(GuildUUID guildUUID, UUID playerUuid, Jedis jedis) {
        Map<String, String> fieldsMap = new HashMap<>();
        List<String> fields = new ArrayList<>(getFields());
        String[] fieldsToArray = fields.toArray(new String[0]);
        List<String> values = jedis.hmget(getJedisKey(guildUUID, playerUuid), fieldsToArray);
        for (int i = 0; i < fieldsToArray.length; i++) {
            fieldsMap.put(fieldsToArray[i], values.get(i));
        }
        return fieldsMap;
    }

    public GuildRank getRank() {
        return rank;
    }

    public void setRank(GuildRank rank) {
        this.rank = rank;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Writes all member data for the guild
     *
     * @param guildUUID  of the guild
     * @param playerUUID of this member
     * @param jedis      a jedis resource/thread
     */
    public void writeToJedis(GuildUUID guildUUID, UUID playerUUID, Jedis jedis) {
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        // Inform the server that this guild member should be saved to mongo on next task (jedis data is refreshed)
        jedis.sadd(database + ":markedForSave:guilds", guildUUID.getUUID().toString());
        String key = getJedisKey(guildUUID, playerUUID);
        jedis.hmset(database + ":" + key, this.toMap(guildUUID.getUUID()));
        jedis.expire(database + ":" + key, RunicCore.getRedisAPI().getExpireTime());
    }

}
