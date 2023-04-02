package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.WriteCallback;
import com.runicrealms.plugin.model.SessionDataRedis;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OwnerData implements SessionDataRedis {

    private UUID uuid;
    private MemberData memberData;

    @SuppressWarnings("unused")
    public OwnerData() {
        // Default constructor for Spring
    }

    /**
     * Constructor to load values from Redis
     *
     * @param uuid       of the owner
     * @param memberData their score and rank
     */
    public OwnerData(UUID uuid, MemberData memberData) {
        this.uuid = uuid;
        this.memberData = memberData;
    }

    @Override
    public Map<String, String> getDataMapFromJedis(UUID uuid, Jedis jedis, int... ints) {
        return null;
    }

    @Override
    public List<String> getFields() {
        return null;
    }

    @Override
    public Map<String, String> toMap(UUID uuid, int... ints) {
        return null;
    }

    @Override
    public void writeToJedis(UUID uuid, Jedis jedis, WriteCallback writeCallback, int... ints) {

    }

    public MemberData getMemberData() {
        return memberData;
    }

    public void setMemberData(MemberData memberData) {
        this.memberData = memberData;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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
