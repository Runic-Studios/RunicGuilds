package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.database.GuildMongoData;
import com.runicrealms.plugin.database.MongoData;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.plugin.model.SessionDataNested;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.GuildMember;
import com.runicrealms.runicguilds.guild.GuildRank;
import com.runicrealms.runicguilds.util.GuildUtil;
import redis.clients.jedis.Jedis;

import java.util.*;

public class MemberData implements SessionDataNested {
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
     * @param guild
     */
    public MemberData(Guild guild) {
        this.prefix = guild.getGuildPrefix();
        this.members = guild.getMembers();
    }

    @Override
    public Map<String, String> getDataMapFromJedis(Jedis jedis, Object nestedObject, int... ints) {
        return null;
    }

    @Override
    public List<String> getFields() {
        return null;
    }

    @Override
    public Map<String, String> toMap(Object nestedObject) {
        return null;
    }

    @Override
    public void writeToJedis(Jedis jedis, int... slot) {

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
