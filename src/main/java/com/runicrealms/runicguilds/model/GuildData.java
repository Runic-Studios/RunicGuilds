package com.runicrealms.runicguilds.model;

import com.mongodb.client.model.Filters;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.GuildMongoData;
import com.runicrealms.plugin.database.MongoData;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.plugin.model.SessionData;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.GuildMember;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import redis.clients.jedis.Jedis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A wrapper around a Guild object that is used to manage its data in redis / mongo
 */
public class GuildData implements SessionData {
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
     * Build a GuildData object from mongo, then cache in jedis / memory
     *
     * @param prefix         of the guild
     * @param guildMongoData a GuildMongoData for the guild
     * @param jedis          the jedis resource
     */
    public GuildData(String prefix, GuildMongoData guildMongoData, Jedis jedis) {
        this.prefix = prefix;
        MongoDataSection ownerSection = guildMongoData.getSection("owner");
        OwnerData ownerData = new OwnerData(this.prefix, ownerSection);
        MemberData memberData = new MemberData(this.prefix, guildMongoData);
        SettingsData settingsData = new SettingsData(this.prefix, guildMongoData);
        GuildBankData guildBankData = new GuildBankData(this.prefix, guildMongoData);

        int guildEXP = 0;
        if (guildMongoData.has("guild-exp")) {
            guildEXP = guildMongoData.get("guild-exp", Integer.class);
        }

        if (!guildMongoData.has("guild-banner")) {
            this.guild = new Guild
                    (
                            memberData.getMembers(),
                            ownerData.getOwner(),
                            guildMongoData.get("name", String.class),
                            guildMongoData.get("prefix", String.class),
                            guildBankData.getItems(),
                            guildMongoData.get("bank-size", Integer.class),
                            settingsData.getBankSettings(),
                            guildEXP
                    );
        } else {
            this.guild = new Guild
                    (
                            memberData.getMembers(),
                            deserializeItemStack(guildMongoData.get("guild-banner", String.class)),
                            ownerData.getOwner(),
                            guildMongoData.get("name", String.class),
                            guildMongoData.get("prefix", String.class),
                            guildBankData.getItems(),
                            guildMongoData.get("bank-size", Integer.class),
                            settingsData.getBankSettings(),
                            guildEXP
                    );
        }
        this.writeToJedis(jedis);
        RunicGuilds.getRunicGuildsAPI().getGuildDataMap().put(this.prefix, this);
    }

    /**
     * Retrieve an ItemStack from a base64 string (should be loss-less)
     *
     * @param item the base64 string
     * @return an ItemStack to set as the guild banner
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

    /**
     * Convert a GuildBanner ItemStack into base64 for storage and retrieval (should be loss-less)
     *
     * @param item the item stack associated with the guild banner
     * @return a string for jedis / mongo storage
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
     * Removes this GuildData object from memory and removes the guild document from memory
     */
    public void delete() {
        RunicGuilds.getRunicGuildsAPI().getGuildDataMap().remove(this.prefix);
        RunicCore.getDatabaseManager().getGuildData().deleteOne(Filters.eq("prefix", this.guild.getGuildPrefix()));
    }

    @Override
    public Map<String, String> getDataMapFromJedis(Jedis jedis, int... slot) {
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
        // Set guild for owner
        RunicGuilds.getRunicGuildsAPI().setJedisGuild(this.guild.getOwner().getUUID(), this.guild.getGuildName(), jedis);
        // Set guild for members
        for (GuildMember guildMember : this.guild.getMembers()) {
            RunicGuilds.getRunicGuildsAPI().setJedisGuild(guildMember.getUUID(), this.guild.getGuildName(), jedis);
        }
    }

    @Override
    public MongoData writeToMongo(MongoData mongoData, int... ints) {

        GuildMongoData guildMongoData = (GuildMongoData) mongoData;
        guildMongoData.remove("members");
        guildMongoData.remove("bank");

        guildMongoData.set("prefix", guild.getGuildPrefix());
        guildMongoData.set("name", guild.getGuildName());
        guildMongoData.set("bank-size", guild.getBankSize());
        guildMongoData.set("score", guild.getScore());
        guildMongoData.set("guild-exp", guild.getGuildExp());
        guildMongoData.set("guild-banner", serializeItemStack(guild.getGuildBanner().getBannerItem()));

        OwnerData ownerData = new OwnerData(guild);
        ownerData.writeToMongo(guildMongoData);
        MemberData memberData = new MemberData(guild);
        memberData.writeToMongo(guildMongoData);
        SettingsData settingsData = new SettingsData(guild);
        settingsData.writeToMongo(guildMongoData);
        GuildBankData guildBankData = new GuildBankData(guild);
        guildBankData.writeToMongo(guildMongoData);

        return mongoData;
    }

    public Guild getGuild() {
        return this.guild;
    }

    public void setGuild(Guild guild) {
        this.guild = guild;
    }
}
