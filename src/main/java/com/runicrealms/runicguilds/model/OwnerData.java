package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.api.WriteCallback;
import com.runicrealms.plugin.model.SessionDataRedis;
import com.runicrealms.runicguilds.guild.GuildMember;
import com.runicrealms.runicguilds.guild.GuildRank;
import com.runicrealms.runicguilds.util.GuildUtil;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OwnerData implements SessionDataRedis {
    private final GuildMember owner;

    /**
     * Builds the OwnerData object with literal values, supplied from a given guild
     *
     * @param prefix of the guild
     * @param uuid   of the owner
     * @param score  of the OWNER
     */
    public OwnerData(String prefix, UUID uuid, int score) {
        this.prefix = prefix;
        this.owner = new GuildMember
                (
                        uuid,
                        GuildRank.OWNER,
                        score,
                        GuildUtil.getOfflinePlayerName(uuid)
                );
    }

    /**
     * Builds the OwnerData object from mongo
     *
     * @param prefix       of the guild
     * @param ownerSection the section of the guild's mongo data
     */
    public OwnerData(String prefix, GuildMongoData ownerSection) {
        this.prefix = prefix;
        UUID ownerUuid = UUID.fromString(ownerSection.getKeys().iterator().next());
        this.owner = new GuildMember
                (
                        ownerUuid,
                        GuildRank.OWNER,
                        ownerSection.get(ownerUuid + ".score", Integer.class),
                        GuildUtil.getOfflinePlayerName(ownerUuid)
                );
    }

    /**
     * @param guild
     */
    public OwnerData(Guild guild) {
        this.prefix = guild.getGuildPrefix();
        UUID ownerUuid = guild.getOwner().getUUID();
        this.owner = new GuildMember
                (
                        ownerUuid,
                        GuildRank.OWNER,
                        guild.getOwner().getScore(),
                        GuildUtil.getOfflinePlayerName(ownerUuid)
                );
    }

    @Override
    public Map<String, String> getDataMapFromJedis(Jedis jedis, int... slot) {
        return null;
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

    public GuildMember getOwner() {
        return owner;
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    public Map<String, String> toMap() {
        return null;
    }

    @Override
    public void writeToJedis(Jedis jedis, int... slot) {

    }

    @Override
    public MongoData writeToMongo(MongoData mongoData, int... slot) {
        if (mongoData.has("owner") && !mongoData.getSection("owner").getKeys().iterator().next().equalsIgnoreCase(this.owner.getUUID().toString())) {
            mongoData.remove("owner");
        }
        mongoData.set("owner." + this.owner.getUUID().toString() + ".score", this.owner.getScore());
        return mongoData;
    }
}
