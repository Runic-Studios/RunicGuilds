package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.model.SessionDataMongo;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.api.event.GuildDisbandEvent;
import com.runicrealms.runicguilds.guild.GuildRank;
import com.runicrealms.runicguilds.guild.banner.BannerUtil;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This is our top-level Data Transfer Object (DTO) that handles read-writing to redis and mongo
 */
@Document(collection = "guilds")
@SuppressWarnings("unused")
public class GuildData implements SessionDataMongo {
    public static final List<String> FIELDS = new ArrayList<>() {{
        add(GuildDataField.EXP.getField());
        add(GuildDataField.GUILD_UUID.getField());
        add(GuildDataField.NAME.getField());
        add(GuildDataField.PREFIX.getField());
    }};
    @Id
    private ObjectId id;
    private UUID guildUUID;
    private String name = "";
    private String prefix = "";
    private int exp = 0;
    private Map<UUID, MemberData> memberDataMap = new HashMap<>();
    private Map<String, Integer> workOrderMap = new HashMap<>();
    private String serializedBanner;

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
    public GuildData(ObjectId id, UUID guildUUID, String name, String prefix, MemberData memberData) {
        this.id = id;
        this.guildUUID = guildUUID;
        this.name = name;
        this.prefix = prefix;
        ItemStack banner = BannerUtil.makeDefaultBanner(guildUUID);
        this.serializedBanner = BannerUtil.serializeItemStack(banner);
        this.memberDataMap.put(memberData.getUuid(), memberData);
    }

    /**
     * Disbands this guild, removing its data from Redis/Mongo and memory
     *
     * @param guildUUID uuid of the data object wrapper
     * @param player    who disbanded the guild
     */
    public static void disband(UUID guildUUID, Player player, boolean mod) {
        Bukkit.getServer().getPluginManager().callEvent(new GuildDisbandEvent(guildUUID, player, mod));
    }

    public Map<String, Integer> getWorkOrderMap() {
        return workOrderMap;
    }

    public void setWorkOrderMap(Map<String, Integer> workOrderMap) {
        this.workOrderMap = workOrderMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public GuildData addDocumentToMongo() {
        MongoTemplate mongoTemplate = RunicDatabase.getAPI().getDataAPI().getMongoTemplate();
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

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public String getSerializedBanner() {
        return serializedBanner;
    }

    public void setSerializedBanner(String serializedBanner) {
        this.serializedBanner = serializedBanner;
    }

    public UUID getUUID() {
        return guildUUID;
    }

    public void setUUID(UUID guildUUID) {
        this.guildUUID = guildUUID;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Map<UUID, MemberData> getMemberDataMap() {
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
        this.memberDataMap.remove(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) { // Player is online
            GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
            guildInfo.setScore(Math.max(0, this.calculateGuildScore()));
        }
    }

}
