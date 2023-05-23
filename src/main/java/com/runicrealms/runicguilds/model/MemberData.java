package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.model.SessionDataRedis;
import com.runicrealms.runicguilds.guild.GuildRank;
import redis.clients.jedis.Jedis;

import java.util.*;

public class MemberData implements SessionDataRedis {
    public static final List<String> FIELDS = new ArrayList<>() {{
        add(GuildDataField.RANK.getField());
        add(GuildDataField.SCORE.getField());
        add(GuildDataField.UUID.getField());
    }};
    private GuildRank rank;
    private Integer score = 0;
    private UUID uuid; // Of the PLAYER. Not a redundant field. Needed when we project member data

    @SuppressWarnings("unused")
    public MemberData() {
        // Default constructor for Spring
    }

    /**
     * Constructor to load values from Jedis
     *
     * @param guildUUID of the guild
     * @param uuid      of the member
     * @param jedis     a jedis resource
     */
    public MemberData(GuildUUID guildUUID, UUID uuid, Jedis jedis) {
        Map<String, String> fieldsMap = new HashMap<>();
        String[] fieldsToArray = FIELDS.toArray(new String[0]);
        String key = GuildData.getJedisKey(guildUUID);
        List<String> values = jedis.hmget(key + ":members:" + uuid, fieldsToArray);
        for (int i = 0; i < fieldsToArray.length; i++) {
            fieldsMap.put(fieldsToArray[i], values.get(i));
        }
        this.uuid = uuid;
        this.rank = GuildRank.getByIdentifier((fieldsMap.get(GuildDataField.RANK.getField())));
        try {
            this.score = Integer.parseInt(fieldsMap.get(GuildDataField.SCORE.getField()));
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            this.score = 0;
        }
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
    public Map<String, String> getDataMapFromJedis(UUID uuid, Jedis jedis, int... ints) {
        return null;
    }

    @Override
    public List<String> getFields() {
        return FIELDS;
    }

    @Override
    public Map<String, String> toMap(UUID uuid, int... ints) {
        return new HashMap<>() {{
            put(GuildDataField.RANK.getField(), rank.getIdentifier());
            put(GuildDataField.SCORE.getField(), String.valueOf(score));
            put(GuildDataField.UUID.getField(), String.valueOf(uuid));
        }};
    }

    @Override
    public void writeToJedis(UUID guildUUID, Jedis jedis, int... ignored) {

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
        jedis.hmset(database + ":" + key, this.toMap(playerUUID));
        jedis.expire(database + ":" + key, RunicCore.getRedisAPI().getExpireTime());
    }

}
