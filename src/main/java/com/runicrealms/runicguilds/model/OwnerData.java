package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.database.MongoData;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.plugin.model.SessionData;
import com.runicrealms.runicguilds.guild.GuildMember;
import com.runicrealms.runicguilds.guild.GuildRank;
import com.runicrealms.runicguilds.util.GuildUtil;
import redis.clients.jedis.Jedis;

import java.util.*;

public class OwnerData implements SessionData {
    public static final List<String> FIELDS = new ArrayList<String>() {{
        add(GuildDataField.MEMBER_UUID.getField());
        add(GuildDataField.MEMBER_SCORE.getField());
    }};
    private static final String OWNER_DATA_KEY = "owner";
    private final String prefix; // of the guild
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
    public OwnerData(String prefix, MongoDataSection ownerSection) {
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

//    /**
//     * Builds the OwnerData from jedis
//     *
//     * @param prefix of the guild
//     * @param jedis  the jedis resource
//     */
//    public OwnerData(String prefix, Jedis jedis) {
//        this.prefix = prefix;
//        Map<String, String> fieldsMap = getDataMapFromJedis(jedis);
//        UUID ownerUuid = UUID.fromString(fieldsMap.get(GuildDataField.MEMBER_UUID.getField()));
//        this.owner = new GuildMember
//                (
//                        ownerUuid,
//                        GuildRank.OWNER,
//                        Integer.parseInt(fieldsMap.get(GuildDataField.MEMBER_SCORE.getField())),
//                        GuildUtil.getOfflinePlayerName(ownerUuid)
//                );
//    }

    @Override
    public Map<String, String> getDataMapFromJedis(Jedis jedis, int... slot) {
        Map<String, String> fieldsMap = new HashMap<>();
        List<String> fields = new ArrayList<>(getFields());
        String[] fieldsToArray = fields.toArray(new String[0]);
        String key = GuildData.DATA_PATH + ":" + this.prefix + ":" + OWNER_DATA_KEY;
        List<String> values = jedis.hmget(key, fieldsToArray);
        for (int i = 0; i < fieldsToArray.length; i++) {
            fieldsMap.put(fieldsToArray[i], values.get(i));
        }
        return fieldsMap;
    }

    @Override
    public List<String> getFields() {
        return FIELDS;
    }

    @Override
    public Map<String, String> toMap() {
        return new HashMap<String, String>() {{
            put(GuildDataField.MEMBER_UUID.getField(), String.valueOf(owner.getUUID()));
            put(GuildDataField.MEMBER_SCORE.getField(), String.valueOf(owner.getScore()));
        }};
    }

    @Override
    public void writeToJedis(Jedis jedis, int... slot) {
        String key = GuildData.DATA_PATH + ":" + this.prefix + ":" + OWNER_DATA_KEY;
        jedis.hmset(key, this.toMap());
    }

    @Override
    public MongoData writeToMongo(MongoData mongoData, int... slot) {
        if (mongoData.has("owner") && !mongoData.getSection("owner").getKeys().iterator().next().equalsIgnoreCase(this.owner.getUUID().toString())) {
            mongoData.remove("owner");
        }
        mongoData.set("owner." + this.owner.getUUID().toString() + ".score", this.owner.getScore());
        return mongoData;
    }

    public GuildMember getOwner() {
        return owner;
    }

    public String getPrefix() {
        return prefix;
    }
}
