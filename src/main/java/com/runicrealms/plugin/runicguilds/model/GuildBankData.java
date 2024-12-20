package com.runicrealms.plugin.runicguilds.model;

import com.runicrealms.plugin.rdb.model.SessionDataRedis;
import com.runicrealms.plugin.runicitems.item.RunicItem;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GuildBankData implements SessionDataRedis {
    private List<RunicItem> runicItems = new ArrayList<>();

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
    public void writeToJedis(UUID uuid, Jedis jedis, int... ints) {

    }

    public List<RunicItem> getRunicItems() {
        return runicItems;
    }

    public void setRunicItems(List<RunicItem> runicItems) {
        this.runicItems = runicItems;
    }


}
