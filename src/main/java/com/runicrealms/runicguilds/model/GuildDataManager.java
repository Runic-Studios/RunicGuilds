package com.runicrealms.runicguilds.model;

import com.mongodb.client.FindIterable;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.database.GuildMongoData;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.event.MongoSaveEvent;
import com.runicrealms.plugin.model.SessionData;
import com.runicrealms.plugin.model.SessionDataManager;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.api.RunicGuildsAPI;
import com.runicrealms.runicguilds.api.event.GuildCreationEvent;
import com.runicrealms.runicguilds.guild.*;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.util.GuildUtil;
import com.runicrealms.runicrestart.api.RunicRestartApi;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manager for handling guild data and keeping it consistent across the network
 *
 * @author Skyfallin
 */
public class GuildDataManager implements Listener, RunicGuildsAPI, SessionDataManager {
    
    private final Map<Object, SessionData> guildDataMap; // maps prefix to data

    /**
     * Initializes guilds and adds them to memory on plugin startup
     */
    public GuildDataManager() {
        this.guildDataMap = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, RunicGuilds.getInstance());
        /*
        Load guilds into memory from mongo / jedis on startup
         */
        Bukkit.getScheduler().runTaskAsynchronously(RunicGuilds.getInstance(), this::loadGuildsIntoMemory);
        /*
        Tab update task
         */
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicGuilds.getInstance(), this::updateGuildTabs, 100L, 20L);
        Bukkit.getLogger().info("[RunicGuilds] All guilds have been loaded!");
    }

    @Override
    public boolean addGuildScore(UUID player, Integer score) {
        if (isInGuild(player)) {
            GuildData guildData = RunicGuilds.getRunicGuildsAPI().getGuildData(player);
            if (guildData != null) {
                guildData.getGuild().increasePlayerScore(player, score);
                guildData.getGuild().recalculateScore();
            }
            return true;
        }
        return false;
    }

    @Override
    public GuildCreationResult createGuild(Player owner, String name, String prefix, boolean modCreated) {
        GuildCreationResult result = createGuild(owner.getUniqueId(), name, prefix);
        if (result == GuildCreationResult.SUCCESSFUL) {
            owner.playSound(owner.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            GuildData.updatePlayerJedisGuild(name, owner.toString());
            Bukkit.getServer().getPluginManager().callEvent(new GuildCreationEvent(RunicGuilds.getRunicGuildsAPI().getGuildData(prefix).getGuild(), modCreated));
        }
        return result;
    }

    @Override
    public List<Guild> getAllGuilds() {
        List<Guild> allGuilds = new ArrayList<>();
        for (Object obj : guildDataMap.keySet()) {
            String key = (String) obj;
            GuildData guildData = (GuildData) guildDataMap.get(key);
            allGuilds.add(guildData.getGuild());
        }
        return allGuilds;
    }

    @Override
    public Guild getGuild(UUID uuid) {
        GuildData data = RunicGuilds.getRunicGuildsAPI().getGuildData(uuid);
        if (data != null) {
            return data.getGuild();
        }
        return null;
    }

    @Override
    public Guild getGuild(String prefix) {
        GuildData data = RunicGuilds.getRunicGuildsAPI().getGuildData(prefix);
        if (data != null) {
            return data.getGuild();
        }
        return null;
    }

    @Override
    public GuildData getGuildData(UUID playerUuid) {
        for (Map.Entry<Object, SessionData> entry : guildDataMap.entrySet()) {
            GuildData guildData = (GuildData) entry.getValue();
            if (guildData.getGuild().getOwner().getUUID().toString().equalsIgnoreCase(playerUuid.toString())) {
                return guildData;
            }
            for (GuildMember member : guildData.getGuild().getMembers()) {
                if (member.getUUID().toString().equalsIgnoreCase(playerUuid.toString())) {
                    return guildData;
                }
            }
        }
        return null;
    }

    @Override
    public GuildData getGuildData(String prefix) {
        for (Map.Entry<Object, SessionData> entry : guildDataMap.entrySet()) {
            GuildData guildData = (GuildData) entry.getValue();
            if (guildData.getGuild().getGuildPrefix().equalsIgnoreCase(prefix)) {
                return guildData;
            }
        }
        return null;
    }

    @Override
    public Map<Object, SessionData> getGuildDataMap() {
        return this.guildDataMap;
    }

    @Override
    public Set<UUID> getGuildRecipients(UUID player) {
        if (!isInGuild(player)) {
            return null;
        }
        Set<UUID> recipients = new HashSet<>();
        Guild guild = getGuild(player);
        if (!guild.getOwner().getUUID().toString().equalsIgnoreCase(player.toString())) {
            recipients.add(guild.getOwner().getUUID());
        }
        for (GuildMember member : guild.getMembers()) {
            if (!member.getUUID().toString().equalsIgnoreCase(player.toString())) {
                recipients.add(member.getUUID());
            }
        }
        return recipients;
    }

    @Override
    public GuildStage getGuildStage(UUID uuid) {
        GuildData data = RunicGuilds.getRunicGuildsAPI().getGuildData(uuid);
        if (data != null) {
            return data.getGuild().getGuildStage();
        }
        return null;
    }

    @Override
    public GuildStage getGuildStage(String prefix) {
        GuildData data = RunicGuilds.getRunicGuildsAPI().getGuildData(prefix);
        if (data != null) {
            return data.getGuild().getGuildStage();
        }
        return null;
    }

    @Override
    public List<GuildMember> getOnlineMembersWithOwner(Guild guild) {
        List<GuildMember> online = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (guild.getMember(player.getUniqueId()) != null) {
                online.add(guild.getMember(player.getUniqueId()));
            }
        }
        return online;
    }

    @Override
    public boolean isInGuild(UUID uuid) {
        return RunicGuilds.getRunicGuildsAPI().getGuildData(uuid) != null;
    }

    @Override
    public GuildRenameResult renameGuild(GuildData guildData, String name) {
        if (name.length() > 16) {
            return GuildRenameResult.NAME_TOO_LONG;
        }
        for (Object obj : guildDataMap.keySet()) {
            String otherGuildPrefix = (String) obj;
            GuildData guildDataOther = (GuildData) guildDataMap.get(otherGuildPrefix);
            if (guildDataOther.getGuild().getGuildName().equalsIgnoreCase(name)) {
                return GuildRenameResult.NAME_NOT_UNIQUE;
            }
        }
        try {
            guildData.getGuild().setGuildName(name);
            for (GuildMember member : guildData.getGuild().getMembers()) {
                GuildData.updatePlayerJedisGuild(name, member.getUUID().toString());
            }
            GuildData.updatePlayerJedisGuild(name, guildData.getGuild().getOwner().toString());
            // guildData.queueToSave();
        } catch (Exception exception) {
            exception.printStackTrace();
            return GuildRenameResult.INTERNAL_ERROR;
        }
        return GuildRenameResult.SUCCESSFUL;
    }

    @Override
    public SessionData checkJedisForSessionData(Object object, Jedis jedis) {
        return null;
    }

    @Override
    public Map<Object, SessionData> getSessionDataMap() {
        return this.guildDataMap;
    }

    @Override
    public SessionData loadSessionData(Object object) {
        String prefix = (String) object;
        // Step 1: check if guild data is memoized
        GuildData guildData = (GuildData) this.guildDataMap.get(prefix);
        if (guildData != null) return guildData;
        // Step 2: check if achievement data is cached in redis
        try (Jedis jedis = RunicCoreAPI.getNewJedisResource()) {
            return loadSessionData(prefix, jedis);
        }
    }

    @Override
    public SessionData loadSessionData(Object object, Jedis jedis) {
        String prefix = (String) object;
        // Step 2: check if achievement data is cached in redis
        GuildData guildData = (GuildData) checkJedisForSessionData(prefix, jedis);
        if (guildData != null) return guildData;
        // Step 2: check mongo documents
        GuildMongoData guildMongoData = new GuildMongoData(prefix);
        return new GuildData(prefix, guildMongoData, jedis);
    }

    /**
     * Attempts to create a guild
     *
     * @param owner  of the guild
     * @param name   of the guild
     * @param prefix of the guild
     * @return the creation result
     */
    private GuildCreationResult createGuild(UUID owner, String name, String prefix) {
        if (prefix.length() < 3 || prefix.length() > 4 || prefix.equalsIgnoreCase("None")) {
            return GuildCreationResult.BAD_PREFIX;
        }
        Pattern pattern = Pattern.compile("[a-zA-Z]");
        Matcher matcher = pattern.matcher(prefix);
        if (!matcher.find()) {
            return GuildCreationResult.BAD_PREFIX;
        }
        if (name.length() > 16) {
            return GuildCreationResult.NAME_TOO_LONG;
        }
        for (Object obj : guildDataMap.keySet()) {
            String guildPrefix = (String) obj;
            GuildData guildData = (GuildData) guildDataMap.get(guildPrefix);
            if (guildData.getGuild().getGuildName().equalsIgnoreCase(name)) {
                return GuildCreationResult.NAME_NOT_UNIQUE;
            }
            if (guildData.getGuild().getOwner().getUUID().toString().equalsIgnoreCase(owner.toString())) {
                return GuildCreationResult.CREATOR_IN_GUILD;
            }
            for (GuildMember member : guildData.getGuild().getMembers()) {
                if (member.getUUID().toString().equalsIgnoreCase(owner.toString())) {
                    return GuildCreationResult.CREATOR_IN_GUILD;
                }
            }
            if (guildPrefix.equalsIgnoreCase(prefix)) {
                return GuildCreationResult.PREFIX_NOT_UNIQUE;
            }
        }
        List<ItemStack> bank = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            bank.add(null);
        }
        Map<GuildRank, Boolean> bankPermissions = new HashMap<>();
        for (GuildRank rank : GuildRank.values()) {
            if (rank != GuildRank.OWNER && !bankPermissions.containsKey(rank)) {
                bankPermissions.put(rank, rank.canAccessBankByDefault());
            }
        }
        try (Jedis jedis = RunicCoreAPI.getNewJedisResource()) {
            Guild guild = new Guild(new HashSet<>(), new GuildMember(owner, GuildRank.OWNER, 0, GuildUtil.getOfflinePlayerName(owner)), name, prefix, bank, 45, bankPermissions, 0);
            GuildMongoData guildMongoData = new GuildMongoData(guild.getGuildPrefix());
            GuildData guildData = new GuildData(guild.getGuildPrefix(), guildMongoData, jedis);
            guildDataMap.put(prefix, guildData);
            guildData.writeToJedis(jedis);
        }
        return GuildCreationResult.SUCCESSFUL;
    }

    /**
     * On startup, we call this method to grab all guilds from our in-memory mongo documents, then
     * cache them in jedis / memory for faster lookup during runtime
     */
    private void loadGuildsIntoMemory() {
        FindIterable<Document> iterable = RunicCore.getDatabaseManager().getGuildData().find();
        for (Document guildDocument : iterable) {
            String prefix = guildDocument.getString("prefix");
            loadSessionData(prefix);
        }
        RunicRestartApi.markPluginLoaded("guilds");
    }

    @EventHandler
    public void onMongoSave(MongoSaveEvent event) {
        for (UUID uuid : event.getPlayersToSave().keySet()) {
            PlayerMongoData playerMongoData = event.getPlayersToSave().get(uuid).getPlayerMongoData();
            Guild guild = RunicGuilds.getRunicGuildsAPI().getGuild(uuid);
            if (guild != null)
                playerMongoData.set("guild", guild.getGuildName());
        }
        for (Object prefix : this.guildDataMap.keySet()) {
            GuildData guildData = (GuildData) this.guildDataMap.get(prefix);
            GuildMongoData guildMongoData = new GuildMongoData(guildData.getGuild().getGuildPrefix());
            guildData.writeToMongo(guildMongoData);
        }
        event.markPluginSaved("guilds");
    }

    /**
     * Updates the guild section of tab for all online players
     */
    private void updateGuildTabs() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            GuildUtil.updateGuildTabColumn(player);
        }
    }
}
