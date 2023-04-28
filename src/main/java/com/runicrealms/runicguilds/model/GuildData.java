package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.model.SessionDataMongo;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.api.event.GuildDisbandEvent;
import com.runicrealms.runicguilds.guild.GuildBanner;
import com.runicrealms.runicguilds.guild.GuildRank;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * This is our top-level Data Transfer Object (DTO) that handles read-writing to redis and mongo
 */
@Document(collection = "guilds")
@SuppressWarnings("unused")
public class GuildData implements SessionDataMongo {
    public static final List<String> FIELDS = new ArrayList<>() {{
        add(GuildDataField.EXP.getField());
        add(GuildDataField.NAME.getField());
        add(GuildDataField.PREFIX.getField());
    }};
    @Id
    private ObjectId id;
    private GuildUUID guildUUID;
    private String name = "";
    private String prefix = "";
    private int exp = 0;
    private HashMap<UUID, MemberData> memberDataMap = new HashMap<>();
    private GuildBankData bankData;
    private SettingsData settingsData;
    private GuildBanner guildBanner;

    @SuppressWarnings("unused")
    public GuildData() {
        // Default constructor for Spring
    }

    /**
     * Constructor for new guilds (creation)
     *
     * @param id         of the guild document in mongo
     * @param guildUUID  of the guild
     * @param name       of the guild
     * @param prefix     of the guild's name
     * @param memberData player owner of the guild
     */
    public GuildData(ObjectId id, GuildUUID guildUUID, String name, String prefix, MemberData memberData) {
        this.id = id;
        this.guildUUID = guildUUID;
        this.name = name;
        this.prefix = prefix;
        this.bankData = new GuildBankData();
        this.settingsData = new SettingsData();
        this.guildBanner = new GuildBanner(guildUUID);
        this.memberDataMap.put(memberData.getUuid(), memberData);
    }

    /**
     * Constructor for retrieving data from Redis
     *
     * @param uuid  of the GUILD in Redis
     * @param jedis a new jedis resource
     */
    public GuildData(UUID uuid, Jedis jedis) {
        this.guildUUID = new GuildUUID(uuid);
        Map<String, String> fieldsMap = new HashMap<>();
        String[] fieldsToArray = FIELDS.toArray(new String[0]);
        String key = GuildData.getJedisKey(guildUUID);
        List<String> values = jedis.hmget(key, fieldsToArray);
        for (int i = 0; i < fieldsToArray.length; i++) {
            fieldsMap.put(fieldsToArray[i], values.get(i));
        }
        this.name = fieldsMap.get(GuildDataField.NAME.getField());
        this.prefix = fieldsMap.get(GuildDataField.PREFIX.getField());
        this.exp = Integer.parseInt(fieldsMap.get(GuildDataField.EXP.getField()));
        this.memberDataMap = RunicGuilds.getDataAPI().loadGuildMembers(getGuildUUID(), jedis);
        // todo: remaining fields
//        this.bankData;
//        this.settingsData;
//        this.guildBanner;
    }

    /**
     * Grabs the root jedis key for this guild to determine if there is data stored in Redis
     *
     * @param guildUUID of the GUILD
     * @return the root key path
     */
    public static String getJedisKey(GuildUUID guildUUID) {
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        return database + ":guilds:" + guildUUID.getUUID().toString();
    }

    /**
     * Disbands this guild, removing its data from Redis/Mongo and memory
     *
     * @param guildUUID uuid of the data object wrapper
     * @param player    who disbanded the guild
     */
    public static void disband(GuildUUID guildUUID, Player player, boolean mod) {
        Bukkit.getServer().getPluginManager().callEvent(new GuildDisbandEvent(guildUUID, player, mod));
    }

    @SuppressWarnings("unchecked")
    @Override
    public GuildData addDocumentToMongo() {
        MongoTemplate mongoTemplate = RunicCore.getDataAPI().getMongoTemplate();
        return mongoTemplate.save(this);
    }

    /**
     * Calculates the total score for this guild as a simple aggregation of each member (and owner's)
     * score
     *
     * @return the combined score of the guild
     */
    public int calculateGuildScore() {
        int result = 0;
        for (MemberData memberData : this.memberDataMap.values()) {
            result += memberData.getScore();
        }
        return result;
    }

    public GuildBankData getBankData() {
        return bankData;
    }

    public void setBankData(GuildBankData bankData) {
        this.bankData = bankData;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public GuildBanner getGuildBanner() {
        return guildBanner;
    }

    public void setGuildBanner(GuildBanner guildBanner) {
        this.guildBanner = guildBanner;
    }

    public GuildUUID getGuildUUID() {
        return guildUUID;
    }

    public void setGuildUUID(GuildUUID guildUUID) {
        this.guildUUID = guildUUID;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public HashMap<UUID, MemberData> getMemberDataMap() {
        return memberDataMap;
    }

    public void setMemberDataMap(HashMap<UUID, MemberData> memberDataMap) {
        this.memberDataMap = memberDataMap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the uuid of the owner of this guild
     */
    public UUID getOwnerUuid() {
        for (MemberData memberData : this.memberDataMap.values()) {
            if (memberData.getRank() != GuildRank.OWNER) continue;
            return memberData.getUuid();
        }
        return null;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public SettingsData getSettingsData() {
        return settingsData;
    }

    public void setSettingsData(SettingsData settingsData) {
        this.settingsData = settingsData;
    }

    /**
     * Checks whether the given player has AT LEAST the specified rank
     *
     * @param player to check
     * @param rank   the minimum rank they must have achieved
     * @return true if player is at least rank
     */
    public boolean isAtLeastRank(Player player, GuildRank rank) {
        // Return true for all 'min rank' checks if the player is the owner
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (guildInfo.getOwnerUuid().equals(player.getUniqueId())) {
            return true;
        }
        MemberData memberData = this.memberDataMap.get(player.getUniqueId());
        if (memberData == null) return false;
        GuildRank currentRank = memberData.getRank();
        return currentRank.getRankNumber() <= rank.getRankNumber(); // 1 is owner
    }

    /**
     * @param uuid of the player to check
     * @return true if the player is in the guild
     */
    public boolean isInGuild(UUID uuid) {
        return this.memberDataMap.containsKey(uuid);
    }

    /**
     * Remove selected member from the guild
     */
    public void removeMember(UUID uuid, Jedis jedis) {
        int score = this.memberDataMap.get(uuid).getScore();
        this.memberDataMap.remove(uuid);
        RunicGuilds.getDataAPI().setGuildForPlayer(uuid, "None");
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(offlinePlayer);
        guildInfo.setScore(Math.max(0, guildInfo.getScore() - score));
    }

    /**
     * A jedis write method that writes the underlying data structures
     *
     * @param jedis some new jedis resource
     */
    public void writeToJedis(Jedis jedis) {
        String root = getJedisKey(this.guildUUID);
        // Include this guild in the guild set
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        jedis.sadd(database + ":guilds", this.guildUUID.getUUID().toString());
        // Write basic fields
        jedis.set(root + ":" + GuildDataField.GUILD_UUID.getField(), this.guildUUID.getUUID().toString());
        jedis.expire(root + ":" + GuildDataField.GUILD_UUID.getField(), RunicCore.getRedisAPI().getExpireTime());
        jedis.set(root + ":" + GuildDataField.NAME.getField(), this.name);
        jedis.expire(root + ":" + GuildDataField.NAME.getField(), RunicCore.getRedisAPI().getExpireTime());
        jedis.set(root + ":" + GuildDataField.PREFIX.getField(), this.prefix);
        jedis.expire(root + ":" + GuildDataField.PREFIX.getField(), RunicCore.getRedisAPI().getExpireTime());
        jedis.set(root + ":" + GuildDataField.EXP.getField(), String.valueOf(this.exp));
        jedis.expire(root + ":" + GuildDataField.EXP.getField(), RunicCore.getRedisAPI().getExpireTime());
        // Write member data (includes owner)
        if (memberDataMap != null) { // Exclude projection
            for (UUID uuid : this.memberDataMap.keySet()) {
                MemberData memberData = this.memberDataMap.get(uuid);
                memberData.writeToJedis(this.guildUUID, uuid, jedis);
            }
        }
        // Write bank data
        if (bankData != null) { // Exclude projection
            this.bankData.writeToJedis(this.guildUUID.getUUID(), jedis);
        }
        // Write settings data
        if (settingsData != null) { // Exclude projection
            this.settingsData.writeToJedis(this.guildUUID.getUUID(), jedis);
        }
    }

}
