package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.model.SessionDataMongo;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
    private OwnerData owner;
    private List<MemberData> members = new ArrayList<>();
    private GuildBankData bank;
    private SettingsData settings;

    @SuppressWarnings("unused")
    public GuildData() {
        // Default constructor for Spring
    }

    /**
     * Constructor for new players
     *
     * @param id     of the guild document in mongo
     * @param uuid   of the guild
     * @param name   of the guild
     * @param prefix of the guild's name
     * @param owner  player owner of the guild
     */
    public GuildData(ObjectId id, UUID uuid, String name, String prefix, OwnerData owner) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.prefix = prefix;
        this.owner = owner;
    }

    @SuppressWarnings("unchecked")
    @Override
    public GuildData addDocumentToMongo() {
        MongoTemplate mongoTemplate = RunicCore.getDataAPI().getMongoTemplate();
        return mongoTemplate.save(this);
    }

    public GuildBankData getBank() {
        return bank;
    }

    public void setBank(GuildBankData bank) {
        this.bank = bank;
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

    public List<MemberData> getMembers() {
        return members;
    }

    public void setMembers(List<MemberData> members) {
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OwnerData getOwner() {
        return owner;
    }

    public void setOwner(OwnerData owner) {
        this.owner = owner;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public SettingsData getSettings() {
        return settings;
    }

    public void setSettings(SettingsData settings) {
        this.settings = settings;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }


}
