package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.database.MongoData;
import com.runicrealms.plugin.model.SessionData;
import com.runicrealms.runicguilds.guild.GuildRank;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsData implements SessionData {
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
        if (mongoData.has("settings")) {
            if (mongoData.getSection("settings").has("bank-access")) {
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

    public Map<GuildRank, Boolean> getBankSettings() {
        return bankSettings;
    }

    @Override
    public Map<String, String> getDataMapFromJedis(Jedis jedis, int... ints) {
        return null;
    }

    @Override
    public List<String> getFields() {
        return null;
    }

    @Override
    public Map<String, String> toMap() {
        return null;
    }

    @Override
    public void writeToJedis(Jedis jedis, int... ints) {
    }

    @Override
    public MongoData writeToMongo(MongoData mongoData, int... ints) {
        return null;
    }

    public String getPrefix() {
        return prefix;
    }
}
