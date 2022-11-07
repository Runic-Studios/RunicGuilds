package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.database.GuildMongoData;
import com.runicrealms.plugin.database.MongoData;
import com.runicrealms.plugin.model.SessionDataNested;
import com.runicrealms.runicguilds.guild.GuildMember;
import com.runicrealms.runicitems.DupeManager;
import com.runicrealms.runicitems.config.ItemLoader;
import com.runicrealms.runicitems.item.RunicItem;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuildBankData implements SessionDataNested {
    public static final List<String> FIELDS = new ArrayList<String>() {{
        add(GuildDataField.MEMBER_UUID.getField());
        add(GuildDataField.MEMBER_SCORE.getField());
    }};
    private static final String MEMBERS_DATA_KEY = "bank";
    private final String prefix; // of the guild
    private final List<ItemStack> items;

    /**
     * Builds the data object from mongo
     *
     * @param prefix         of the guild
     * @param guildMongoData of the guild's mongo data
     */
    public GuildBankData(String prefix, GuildMongoData guildMongoData) {
        this.prefix = prefix;
        this.items = new ArrayList<>();
        if (guildMongoData.has("bank")) {
            for (int i = 0; i < guildMongoData.get("bank-size", Integer.class); i++) {
                if (guildMongoData.has("bank." + i)) {
                    try {
                        RunicItem item = ItemLoader.loadItem(guildMongoData.getSection("bank." + i), DupeManager.getNextItemId());
                        items.add(item.generateItem());
                    } catch (Exception exception) {
                        Bukkit.getLogger().warning("[RunicItems] ERROR loading item " + i + " for guild bank " + this.prefix);
                        exception.printStackTrace();
                        items.add(null);
                    }
                } else {
                    items.add(null);
                }
            }
        } else {
            for (int i = 0; i < guildMongoData.get("bank-size", Integer.class); i++) {
                items.add(null);
            }
        }
    }

//    /**
//     * Builds the data from jedis
//     *
//     * @param prefix of the guild
//     * @param jedis  the jedis resource
//     */
//    public GuildBankData(String prefix, Jedis jedis) {
//        this.prefix = prefix;
//        this.members = new HashSet<>();
//
//        String key = GuildData.DATA_PATH + ":" + this.prefix + ":" + MEMBERS_DATA_KEY;
//        for (String achievementId : RedisUtil.getNestedKeys(key, jedis)) {
//            Map<String, String> fieldsMap = getDataMapFromJedis(jedis, achievementId);
//            UUID memberUuid = UUID.fromString(fieldsMap.get(GuildDataField.MEMBER_UUID.getField()));
//            GuildRank guildRank = GuildRank.getByName(fieldsMap.get(GuildDataField.MEMBER_RANK.getField()));
//            int score = Integer.parseInt(GuildDataField.MEMBER_SCORE.getField());
//            GuildMember guildMember = new GuildMember(memberUuid, guildRank, score, GuildUtil.getOfflinePlayerName(memberUuid));
//            members.add(guildMember);
//        }
//    }

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
//        String key = GuildData.DATA_PATH + ":" + this.prefix + ":" + MEMBERS_DATA_KEY;
//        for (GuildMember member : members) {
//            jedis.hmset(key + ":" + member.getUUID(), this.toMap(member));
//            jedis.expire(key + ":" + member.getUUID(), RedisUtil.EXPIRE_TIME);
//        }
    }

    @Override
    public MongoData writeToMongo(MongoData mongoData, int... ints) {
        GuildMongoData guildMongoData = (GuildMongoData) mongoData;
//        for (int i = 0; i < guild.getBankSize(); i++) {
//            if (guild.getBank().get(i) != null) {
//                RunicItem runicItem = ItemManager.getRunicItemFromItemStack(guild.getBank().get(i));
//                if (runicItem != null) {
//                    runicItem.addToDataSection(guildMongoData, "bank." + i);
//                }
//            }
//        }
        return guildMongoData;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public String getPrefix() {
        return prefix;
    }
}
