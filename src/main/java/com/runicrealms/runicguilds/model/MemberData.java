package com.runicrealms.runicguilds.model;

import com.runicrealms.runicguilds.guild.GuildRank;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class MemberData {
    private GuildRank rank;
    private Integer score = 0;
    private UUID uuid; // Of the PLAYER. Not a redundant field. Needed when we project member data

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
        // todo: add a write operation method
//        String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
//        // Inform the server that this guild member should be saved to mongo on next task (jedis data is refreshed)
//        jedis.sadd(database + ":markedForSave:guilds", guildUUID.getUUID().toString());
//        String key = getJedisKey(guildUUID, playerUUID);
//        jedis.hmset(database + ":" + key, this.toMap(playerUUID));
//        jedis.expire(database + ":" + key, RunicDatabase.getAPI().getRedisAPI().getExpireTime());
    }

}
