package com.runicrealms.runicguilds.model;

import com.mongodb.client.model.Filters;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.GuildMongoData;
import com.runicrealms.plugin.database.MongoData;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.model.SessionData;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.GuildMember;
import com.runicrealms.runicguilds.guild.GuildRank;
import com.runicrealms.runicguilds.util.GuildUtil;
import com.runicrealms.runicitems.DupeManager;
import com.runicrealms.runicitems.ItemManager;
import com.runicrealms.runicitems.config.ItemLoader;
import com.runicrealms.runicitems.item.RunicItem;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import redis.clients.jedis.Jedis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 *
 */
public class GuildData implements SessionData {

    private static final String DATA_PATH = "guilds";
    private final String prefix; // of the GUILD
    private Guild guild;

//    /**
//     * @param guild
//     * @param createNewDocument
//     */
//    public GuildData(Guild guild, boolean createNewDocument) { // todo: createNew should be false, just check if exists
//        this.prefix = guild.getGuildPrefix();
//        if (createNewDocument) {
//            RunicCore.getDatabaseManager().getGuildData().insertOne(new Document("prefix", guild.getGuildPrefix()));
//        }
//        this.guildData = new GuildMongoData(guild.getGuildPrefix());
//        this.save(guild, true);
//    }

    /**
     * @param prefix
     * @param guildMongoData
     * @param jedis
     */
    public GuildData(String prefix, GuildMongoData guildMongoData, Jedis jedis) {
        this.prefix = prefix;
        MongoDataSection ownerSection = guildMongoData.getSection("owner");
        UUID ownerUuid = UUID.fromString(ownerSection.getKeys().iterator().next());
        GuildMember owner = new GuildMember
                (
                        ownerUuid,
                        GuildRank.OWNER,
                        ownerSection.get(ownerUuid + ".score", Integer.class),
                        GuildUtil.getOfflinePlayerName(ownerUuid)
                );
        Set<GuildMember> members = new HashSet<>();
        if (guildMongoData.has("members")) {
            MongoDataSection membersSection = guildMongoData.getSection("members");
            for (String key : membersSection.getKeys()) {
                members.add(new GuildMember(UUID.fromString(key), GuildRank.getByName(membersSection.get(key + ".rank", String.class)), membersSection.get(key + ".score", Integer.class), GuildUtil.getOfflinePlayerName(UUID.fromString(key))));
            }
        }

        List<ItemStack> items = new ArrayList<>();
        if (((!guildMongoData.has("bank-type"))
                || !guildMongoData.get("bank-type", String.class).equalsIgnoreCase("runicitems"))
                && guildMongoData.has("bank")) {
            guildMongoData.remove("bank");
            Bukkit.getScheduler().runTaskAsynchronously(RunicGuilds.getInstance(), guildMongoData::save);
        } else if (guildMongoData.has("bank")) {
            for (int i = 0; i < guildMongoData.get("bank-size", Integer.class); i++) {
                if (guildMongoData.has("bank." + i)) {
                    try {
                        RunicItem item = ItemLoader.loadItem(guildMongoData.getSection("bank." + i), DupeManager.getNextItemId());
                        items.add(item.generateItem());
                    } catch (Exception exception) {
                        Bukkit.getLogger().log(Level.WARNING, "[RunicItems] ERROR loading item " + i + " for guild bank " + this.guild.getGuildPrefix());
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
        Map<GuildRank, Boolean> bankPermissions = new HashMap<>();
        if (guildMongoData.has("settings")) {
            if (guildMongoData.getSection("settings").has("bank-access")) {
                for (String key : guildMongoData.getSection("settings.bank-access").getKeys()) {
                    GuildRank rank = GuildRank.getByIdentifier(key);
                    if (rank != null && rank != GuildRank.OWNER) {
                        bankPermissions.put(rank, guildMongoData.get("settings.bank-access." + key, Boolean.class));
                    }
                }
            }
        }
        for (GuildRank rank : GuildRank.values()) {
            if (rank != GuildRank.OWNER && !bankPermissions.containsKey(rank)) {
                bankPermissions.put(rank, rank.canAccessBankByDefault());
            }
        }

        int guildEXP = 0;
        if (guildMongoData.has("guild-exp")) {
            guildEXP = guildMongoData.get("guild-exp", Integer.class);
        }

        if (!guildMongoData.has("guild-banner")) {
            this.guild = new Guild(members, owner, guildMongoData.get("name", String.class), guildMongoData.get("prefix", String.class), items, guildMongoData.get("bank-size", Integer.class), bankPermissions, guildEXP);
        } else {
            this.guild = new Guild(members, deserializeItemStack(guildMongoData.get("guild-banner", String.class)), owner, guildMongoData.get("name", String.class), guildMongoData.get("prefix", String.class), items, guildMongoData.get("bank-size", Integer.class), bankPermissions, guildEXP);
        }
    }

    public GuildData(String prefix, Jedis jedis) {
        this.prefix = prefix;
    }

    /**
     * @param item
     * @return
     */
    private static ItemStack deserializeItemStack(String item) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(item));
        try {
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            dataInput.close();
            return (ItemStack) dataInput.readObject();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    // todo: investigate this method
    public static void setGuildForPlayer(String name, String uuid) {
        // todo: update redis as well
        Bukkit.getScheduler().runTaskAsynchronously(RunicGuilds.getInstance(), () -> {
            PlayerMongoData mongoData = new PlayerMongoData(uuid);
            mongoData.set("guild", name);
            mongoData.save();
        });
    }

    /**
     * @param item
     * @return
     */
    private static String serializeItemStack(ItemStack item) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * @param jedis
     */
    public void delete(Jedis jedis) {
        RunicGuilds.getRunicGuildsAPI().getGuildDataMap().remove(this.prefix);
        jedis.del(DATA_PATH + ":" + this.prefix);
        RunicCore.getDatabaseManager().getGuildData().deleteOne(Filters.eq("prefix", this.guild.getGuildPrefix()));
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
    public Map<String, String> getDataMapFromJedis(Jedis jedis, int... ints) {
        return null;
    }

    @Override
    public void writeToJedis(Jedis jedis, int... ints) {

    }

    @Override
    public MongoData writeToMongo(MongoData mongoData, int... ints) {
        GuildMongoData guildMongoData = (GuildMongoData) mongoData;
        guildMongoData.remove("members");
        guildMongoData.remove("bank");
        if (guildMongoData.has("owner") && !guildMongoData.getSection("owner").getKeys().iterator().next().equalsIgnoreCase(guild.getOwner().getUUID().toString())) {
            guildMongoData.remove("owner");
        }
        guildMongoData.set("owner." + guild.getOwner().getUUID().toString() + ".score", guild.getOwner().getScore());
        for (GuildMember member : guild.getMembers()) {
            guildMongoData.set("members." + member.getUUID().toString() + ".rank", member.getRank().getName());
            guildMongoData.set("members." + member.getUUID().toString() + ".score", member.getScore());
        }
        guildMongoData.set("prefix", guild.getGuildPrefix());
        guildMongoData.set("name", guild.getGuildName());
        guildMongoData.set("bank-size", guild.getBankSize());
        for (int i = 0; i < guild.getBankSize(); i++) {
            if (guild.getBank().get(i) != null) {
                RunicItem runicItem = ItemManager.getRunicItemFromItemStack(guild.getBank().get(i));
                if (runicItem != null) {
                    runicItem.addToDataSection(guildMongoData, "bank." + i);
                }
            }
        }
        guildMongoData.set("score", guild.getScore());
        for (GuildRank rank : this.guild.getBankAccess().keySet()) {
            guildMongoData.set("settings.bank-access." + rank.getIdentifier(), this.guild.canAccessBank(rank));
        }
        guildMongoData.set("guild-exp", guild.getGuildExp());
        guildMongoData.set("guild-banner", serializeItemStack(guild.getGuildBanner().getBannerItem()));
        return mongoData;
    }

    public Guild getGuild() {
        return this.guild;
    }

    public void setGuild(Guild guild) {
        this.guild = guild;
    }
}
