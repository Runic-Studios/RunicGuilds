package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.WriteCallback;
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

    @SuppressWarnings("unused")
    public MemberData() {
        // Default constructor for Spring
    }

    /**
     * Constructor to retrieve data from Redis
     *
     * @param rank  of the player
     * @param score of the player
     */
    public MemberData(GuildRank rank, Integer score) {
        this.rank = rank;
        this.score = score;
    }

    /**
     * ?
     *
     * @param uuid of the guild
     * @return
     */
    public static String getJedisKey(GuildUUID guildUUID, UUID uuid) {
        return guildUUID + ":members:" + uuid;
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
    public void writeToJedis(UUID guildUUID, Jedis jedis, WriteCallback writeCallback, int... ignored) {

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

    /**
     * ?
     *
     * @param guildUUID
     * @param playerUUID
     * @param jedis
     */
    public void writeToJedis(GuildUUID guildUUID, UUID playerUUID, Jedis jedis) {
        // Inform the server that this guild member should be saved to mongo on next task (jedis data is refreshed)
        jedis.sadd("markedForSave:guilds", guildUUID.toString());
        String key = getJedisKey(guildUUID, playerUUID);
        jedis.hmset(key, this.toMap(guildUUID.getUUID()));
        jedis.expire(key, RunicCore.getRedisAPI().getExpireTime());
    }

}
