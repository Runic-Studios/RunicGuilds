package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.api.WriteCallback;
import com.runicrealms.plugin.model.SessionDataRedis;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicitems.DupeManager;
import com.runicrealms.runicitems.ItemManager;
import com.runicrealms.runicitems.config.ItemLoader;
import com.runicrealms.runicitems.item.RunicItem;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GuildBankData implements SessionDataRedis {
    private final String prefix; // of the guild
    private final Guild guild;
    private final List<ItemStack> items;

    /**
     * Builds the data object from mongo
     *
     * @param prefix         of the guild
     * @param guildMongoData of the guild's mongo data
     */
    public GuildBankData(String prefix, GuildMongoData guildMongoData) {
        this.prefix = prefix;
        this.guild = null;
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

    @Override
    public Map<String, String> getDataMapFromJedis(Jedis jedis, Object nestedObject, int... ints) {
        return null;
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

    public List<ItemStack> getItems() {
        return items;
    }

    public String getPrefix() {
        return prefix;
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
        for (int i = 0; i < guild.getBankSize(); i++) {
            if (guild.getBankContents().get(i) != null) {
                RunicItem runicItem = ItemManager.getRunicItemFromItemStack(guild.getBankContents().get(i));
                if (runicItem != null) {
                    runicItem.addToDataSection(guildMongoData, "bank." + i);
                }
            }
        }
        return guildMongoData;
    }
}
