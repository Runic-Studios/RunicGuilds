package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.rdb.model.SessionDataRedis;
import com.runicrealms.runicguilds.guild.GuildRank;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SettingsData implements SessionDataRedis {
    private HashMap<GuildRank, Boolean> bankSettingsMap = new HashMap<>();

    @SuppressWarnings("unused")
    public SettingsData() {
        // Default constructor for Spring
        for (GuildRank guildRank : GuildRank.values()) {
            bankSettingsMap.put(guildRank, guildRank.canAccessBankByDefault());
        }
    }

    /**
     * @param rank of the player
     * @return true if the player can access the guild bank
     */
    public boolean canAccessBank(GuildRank rank) {
        if (!bankSettingsMap.containsKey(rank)) return false;
        return bankSettingsMap.get(rank);
    }

    public Map<GuildRank, Boolean> getBankSettingsMap() {
        return bankSettingsMap;
    }

    public void setBankSettingsMap(HashMap<GuildRank, Boolean> bankSettingsMap) {
        this.bankSettingsMap = bankSettingsMap;
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
    public void writeToJedis(UUID uuid, Jedis jedis, int... ints) {

    }

}
