package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.api.WriteCallback;
import com.runicrealms.plugin.model.SessionDataRedis;
import com.runicrealms.runicitems.item.RunicItem;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GuildBankData implements SessionDataRedis {
    private final List<RunicItem> runicItems = new ArrayList<>();

    @SuppressWarnings("unused")
    public GuildBankData() {
        // Default constructor for Spring
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
