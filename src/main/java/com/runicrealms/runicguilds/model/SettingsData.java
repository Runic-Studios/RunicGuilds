package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.api.WriteCallback;
import com.runicrealms.plugin.model.SessionDataRedis;
import com.runicrealms.runicguilds.guild.GuildRank;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SettingsData implements SessionDataRedis {
    private HashMap<GuildRank, Boolean> bankSettings = new HashMap<>();

    @SuppressWarnings("unused")
    public SettingsData() {
        // Default constructor for Spring
        for (GuildRank guildRank : GuildRank.values()) {
            bankSettings.put(guildRank, guildRank.canAccessBankByDefault());
        }
    }

    /**
     * @param rank of the player
     * @return true if the player can access the guild bank
     */
    public boolean canAccessBank(GuildRank rank) {
        if (!bankSettings.containsKey(rank)) return false;
        return bankSettings.get(rank);
    }

    public Map<GuildRank, Boolean> getBankSettings() {
        return bankSettings;
    }

    public void setBankSettings(HashMap<GuildRank, Boolean> bankSettings) {
        this.bankSettings = bankSettings;
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

}
