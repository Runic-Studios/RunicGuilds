package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.model.SessionDataMongo;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This is our top-level Data Transfer Object (DTO) that handles read-writing to redis and mongo
 */
@Document(collection = "guilds")
public class GuildData implements SessionDataMongo {
    @Id
    private ObjectId id;
    @Field("guildUuid")
    private UUID uuid;
    private String name = "";
    private String prefix = "";
    private int exp = 0;
    private OwnerData ownerData;
    private List<MemberData> memberDataList = new ArrayList<>();
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
     * @param uuid      of the guild
     * @param name      of the guild
     * @param prefix    of the guild's name
     * @param ownerData player owner of the guild
     */
    public GuildData(ObjectId id, UUID uuid, String name, String prefix, OwnerData ownerData) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.prefix = prefix;
        this.ownerData = ownerData;
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

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public List<MemberData> getMemberDataList() {
        return memberDataList;
    }

    public void setMemberDataList(List<MemberData> memberDataList) {
        this.memberDataList = memberDataList;
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

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * A jedis write method that writes the underlying data structures
     *
     * @param jedis some new jedis resource
     */
    public void writeToJedis(Jedis jedis) {
//        this.ownerData.writeToJedis(uuid, jedis, () -> {
//        });
//        for (MemberData memberData : this.memberDataList) {
//            memberData.writeToJedis();
//        }
//        this.bankData.writeToJedis();
//        this.settingsData.writeToJedis();
    }

}
