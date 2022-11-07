package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.database.MongoData;
import com.runicrealms.plugin.model.SessionData;
import com.runicrealms.runicguilds.guild.GuildRank;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsData implements SessionData {
    private static final String SETTINGS_DATA_KEY = "settings";
    private final String prefix; // of the guild
    private final Map<GuildRank, Boolean> bankSettings;

    /**
     * Builds the OwnerData object from mongo
     *
     * @param prefix    of the guild
     * @param mongoData the guild's mongo data
     */
    public SettingsData(String prefix, MongoData mongoData) {
        this.prefix = prefix;
        bankSettings = new HashMap<>();
        if (mongoData.has(SETTINGS_DATA_KEY)) {
            if (mongoData.getSection(SETTINGS_DATA_KEY).has("bank-access")) {
                for (String key : mongoData.getSection("settings.bank-access").getKeys()) {
                    GuildRank rank = GuildRank.getByIdentifier(key);
                    if (rank != null && rank != GuildRank.OWNER) {
                        bankSettings.put(rank, mongoData.get("settings.bank-access." + key, Boolean.class));
                    }
                }
            }
        }
        for (GuildRank rank : GuildRank.values()) {
            if (rank != GuildRank.OWNER && !bankSettings.containsKey(rank)) {
                bankSettings.put(rank, rank.canAccessBankByDefault());
            }
        }
    }

    /**
     * Builds the data from jedis
     *
     * @param prefix of the guild
     * @param jedis  the jedis resource
     */
    public SettingsData(String prefix, Jedis jedis) {
        this.prefix = prefix;
        Map<String, String> fieldsMap = getDataMapFromJedis(jedis);
        bankSettings = new HashMap<>();
        for (String key : fieldsMap.keySet()) {
            bankSettings.put(GuildRank.getByIdentifier(key), Boolean.parseBoolean(fieldsMap.get(key)));
        }
        for (GuildRank rank : GuildRank.values()) {
            if (rank != GuildRank.OWNER && !bankSettings.containsKey(rank)) {
                bankSettings.put(rank, rank.canAccessBankByDefault());
            }
        }
    }

    public Map<GuildRank, Boolean> getBankSettings() {
        return bankSettings;
    }

    @Override
    public Map<String, String> getDataMapFromJedis(Jedis jedis, int... ints) {
        Map<String, String> fieldsMap = new HashMap<>();
        List<String> fields = new ArrayList<>(getFields());
        String[] fieldsToArray = fields.toArray(new String[0]);
        List<String> values = jedis.hmget(GuildData.DATA_PATH + ":" + this.prefix + ":" + SETTINGS_DATA_KEY, fieldsToArray);
        for (int i = 0; i < fieldsToArray.length; i++) {
            fieldsMap.put(fieldsToArray[i], values.get(i));
        }
        return fieldsMap;
    }

    @Override
    public List<String> getFields() {
        return null;
    }

    @Override
    public Map<String, String> toMap() {
        return new HashMap<String, String>() {{
            for (GuildRank guildRank : bankSettings.keySet()) {
                put(guildRank.getName(), String.valueOf(bankSettings.get(guildRank)));
            }
        }};
    }

    @Override
    public void writeToJedis(Jedis jedis, int... ints) {
        String key = GuildData.DATA_PATH + ":" + this.prefix + ":" + SETTINGS_DATA_KEY;
        jedis.hmset(key, this.toMap());
    }

    @Override
    public MongoData writeToMongo(MongoData mongoData, int... ints) {
        return null;
    }

    public String getPrefix() {
        return prefix;
    }
}
