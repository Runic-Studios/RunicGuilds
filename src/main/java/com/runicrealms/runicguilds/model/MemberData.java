package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.database.GuildMongoData;
import com.runicrealms.plugin.database.MongoData;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.plugin.model.SessionDataNested;
import com.runicrealms.plugin.redis.RedisUtil;
import com.runicrealms.runicguilds.guild.GuildMember;
import com.runicrealms.runicguilds.guild.GuildRank;
import com.runicrealms.runicguilds.util.GuildUtil;
import redis.clients.jedis.Jedis;

import java.util.*;

public class MemberData implements SessionDataNested {
    public static final List<String> FIELDS = new ArrayList<String>() {{
        add(GuildDataField.MEMBER_UUID.getField());
        add(GuildDataField.MEMBER_SCORE.getField());
    }};
    private static final String MEMBERS_DATA_KEY = "members";
    private final String prefix; // of the guild
    private final Set<GuildMember> members;

    /**
     * Builds the data object from mongo
     *
     * @param prefix         of the guild
     * @param guildMongoData of the guild's mongo data
     */
    public MemberData(String prefix, GuildMongoData guildMongoData) {
        this.prefix = prefix;
        this.members = new HashSet<>();
        if (guildMongoData.has("members")) {
            MongoDataSection membersSection = guildMongoData.getSection("members");
            for (String key : membersSection.getKeys()) {
                members.add
                        (
                                new GuildMember(UUID.fromString(key),
                                        GuildRank.getByName(membersSection.get(key + ".rank", String.class)),
                                        membersSection.get(key + ".score", Integer.class), GuildUtil.getOfflinePlayerName(UUID.fromString(key)))
                        );
            }
        }
    }

    /**
     * Builds the data from jedis
     *
     * @param prefix of the guild
     * @param jedis  the jedis resource
     */
    public MemberData(String prefix, Jedis jedis) {
        this.prefix = prefix;
        this.members = new HashSet<>();

        String key = GuildData.DATA_PATH + ":" + this.prefix + ":" + MEMBERS_DATA_KEY;
        for (String achievementId : RedisUtil.getNestedKeys(key, jedis)) {
            Map<String, String> fieldsMap = getDataMapFromJedis(jedis, achievementId);
            UUID memberUuid = UUID.fromString(fieldsMap.get(GuildDataField.MEMBER_UUID.getField()));
            GuildRank guildRank = GuildRank.getByName(fieldsMap.get(GuildDataField.MEMBER_RANK.getField()));
            int score = Integer.parseInt(GuildDataField.MEMBER_SCORE.getField());
            GuildMember guildMember = new GuildMember(memberUuid, guildRank, score, GuildUtil.getOfflinePlayerName(memberUuid));
            members.add(guildMember);
        }
    }

    @Override
    public Map<String, String> getDataMapFromJedis(Jedis jedis, Object nestedObject, int... ints) {
        String memberUuid = (String) nestedObject;
        String memberKey = GuildData.DATA_PATH + ":" + this.prefix + ":" + MEMBERS_DATA_KEY + memberUuid;
        return jedis.hgetAll(memberKey);
    }

    @Override
    public List<String> getFields() {
        return FIELDS;
    }

    @Override
    public Map<String, String> toMap(Object nestedObject) {
        GuildMember guildMember = (GuildMember) nestedObject;
        return new HashMap<String, String>() {{
            put(GuildDataField.MEMBER_UUID.getField(), String.valueOf(guildMember.getUUID()));
            put(GuildDataField.MEMBER_RANK.getField(), String.valueOf(guildMember.getRank()));
            put(GuildDataField.MEMBER_SCORE.getField(), String.valueOf(guildMember.getScore()));
        }};
    }

    @Override
    public void writeToJedis(Jedis jedis, int... slot) {
        String key = GuildData.DATA_PATH + ":" + this.prefix + ":" + MEMBERS_DATA_KEY;
        for (GuildMember member : members) {
            jedis.hmset(key + ":" + member.getUUID(), this.toMap(member));
            jedis.expire(key + ":" + member.getUUID(), RedisUtil.EXPIRE_TIME);
        }
    }

    @Override
    public MongoData writeToMongo(MongoData mongoData, int... ints) {
        GuildMongoData guildMongoData = (GuildMongoData) mongoData;
        for (GuildMember member : this.members) {
            guildMongoData.set("members." + member.getUUID().toString() + ".rank", member.getRank().getName());
            guildMongoData.set("members." + member.getUUID().toString() + ".score", member.getScore());
        }
        return mongoData;
    }

    public Set<GuildMember> getMembers() {
        return members;
    }

    public String getPrefix() {
        return prefix;
    }
}
