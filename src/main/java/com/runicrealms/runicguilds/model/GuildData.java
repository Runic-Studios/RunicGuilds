package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.model.SessionDataMongo;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.UUID;

/**
 * This is our top-level Data Transfer Object (DTO) that handles read-writing to redis and mongo
 */
@Document(collection = "guilds")
@SuppressWarnings("unused")
public class GuildData implements SessionDataMongo {
    @Id
    private ObjectId id;
    private GuildUUID guildUUID;
    private String name = "";
    private String prefix = "";
    private int exp = 0;
    private OwnerData ownerData;
    private HashMap<UUID, MemberData> memberDataMap = new HashMap<>();
    private GuildBankData bankData;
    private SettingsData settingsData;

    @SuppressWarnings("unused")
    public GuildData() {
        // Default constructor for Spring
    }

    /**
     * Constructor for new players
     *
     * @param id        of the guild document in mongo
     * @param guildUUID of the guild
     * @param name      of the guild
     * @param prefix    of the guild's name
     * @param ownerData player owner of the guild
     */
    public GuildData(ObjectId id, GuildUUID guildUUID, String name, String prefix, OwnerData ownerData) {
        this.id = id;
        this.guildUUID = guildUUID;
        this.name = name;
        this.prefix = prefix;
        this.ownerData = ownerData;
    }

    /**
     * Constructor for retrieving data from Redis
     *
     * @param guildUUID of the GUILD in Redis
     * @param jedis     a new jedis resource
     */
    public GuildData(GuildUUID guildUUID, Jedis jedis) {
        this.guildUUID = guildUUID;
        // todo: initialize ALL other fields
    }

    /**
     * Grabs the root jedis key for this guild to determine if there is data stored in Redis
     *
     * @param guildUUID of the GUILD
     * @return the root key path
     */
    public static String getJedisKey(GuildUUID guildUUID) {
        return guildUUID + ":guildUUID";
    }

    @SuppressWarnings("unchecked")
    @Override
    public GuildData addDocumentToMongo() {
        MongoTemplate mongoTemplate = RunicCore.getDataAPI().getMongoTemplate();
        return mongoTemplate.save(this);
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

    public OwnerData getOwnerData() {
        return ownerData;
    }

    public void setOwnerData(OwnerData ownerData) {
        this.ownerData = ownerData;
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
     * A jedis write method that writes the underlying data structures
     *
     * @param jedis some new jedis resource
     */
    public void writeToJedis(Jedis jedis) {
        String root = getJedisKey(this.guildUUID);
        // Write basic fields
        jedis.set(root + ":" + GuildDataField.UUID.getField(), this.guildUUID.getUUID().toString());
        jedis.expire(root + ":" + GuildDataField.UUID.getField(), RunicCore.getRedisAPI().getExpireTime());
        jedis.set(root + ":" + GuildDataField.NAME.getField(), this.name);
        jedis.expire(root + ":" + GuildDataField.NAME.getField(), RunicCore.getRedisAPI().getExpireTime());
        jedis.set(root + ":" + GuildDataField.PREFIX.getField(), this.prefix);
        jedis.expire(root + ":" + GuildDataField.PREFIX.getField(), RunicCore.getRedisAPI().getExpireTime());
        jedis.set(root + ":" + GuildDataField.EXP.getField(), String.valueOf(this.exp));
        jedis.expire(root + ":" + GuildDataField.EXP.getField(), RunicCore.getRedisAPI().getExpireTime());
        // Write owner data
        this.ownerData.writeToJedis(this.guildUUID, jedis);
        // Write member data
        for (UUID uuid : this.memberDataMap.keySet()) {
            MemberData memberData = this.memberDataMap.get(uuid);
            memberData.writeToJedis(this.guildUUID, uuid, jedis);
        }
        // todo Write bank data
//        this.bankData.writeToJedis();
        // todo Write settings data
//        this.settingsData.writeToJedis();
    }

}
