package com.runicrealms.runicguilds;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runicguilds.api.GuildsAPI;
import com.runicrealms.runicguilds.api.event.GuildCreationEvent;
import com.runicrealms.runicguilds.api.event.GuildScoreChangeEvent;
import com.runicrealms.runicguilds.guild.GuildCreationResult;
import com.runicrealms.runicguilds.guild.GuildRank;
import com.runicrealms.runicguilds.guild.GuildRenameResult;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.model.GuildInfo;
import com.runicrealms.runicguilds.model.GuildUUID;
import com.runicrealms.runicguilds.model.MemberData;
import com.runicrealms.runicguilds.util.GuildUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuildManager implements GuildsAPI, Listener {


    @Override
    public boolean addGuildScore(UUID player, Integer score) {
        if (isInGuild(player)) {
            GuildData guildData = RunicGuilds.getGuildsAPI().getGuildData(player);
            if (guildData != null) {
                Bukkit.getPluginManager().callEvent(new GuildScoreChangeEvent
                        (
                                guildData,
                                guildData.getGuild().getMember(player),
                                score,
                                false
                        ));
                return true;
            }
        }
        return false;
    }

    @Override
    public GuildCreationResult createGuild(Player owner, String name, String prefix, boolean modCreated) {
        GuildCreationResult result = createGuild(owner.getUniqueId(), name, prefix);
        if (result == GuildCreationResult.SUCCESSFUL) {
            owner.playSound(owner.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            RunicGuilds.getGuildsAPI().setJedisGuild(owner.getUniqueId(), name);
            Bukkit.getServer().getPluginManager().callEvent(new GuildCreationEvent(RunicGuilds.getGuildsAPI().getGuild(prefix).getGuild(), modCreated));
        }
        return result;
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
    public GuildStage getGuildStage(GuildUUID guildUUID) {
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(guildUUID);
        if (guildInfo == null) {
            return GuildStage.STAGE_0;
        } else {
            return GuildStage.getFromExp(guildInfo.getExp());
        }
    }

    @Override
    public void giveExperience(GuildUUID guildUUID, int exp) {
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(guildUUID);
            guildInfo.setExp(guildInfo.getExp() + exp);
            CompletableFuture<GuildData> future = RunicGuilds.getDataAPI().loadGuildData(guildUUID, jedis);
            future.whenComplete((GuildData guildData, Throwable ex) -> {
                if (ex != null) {
                    Bukkit.getLogger().log(Level.SEVERE, "There was an error trying to give guild experience!");
                    ex.printStackTrace();
                } else {
                    guildData.writeToJedis(jedis);
                }
            });
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, "There was an error trying to give guild experience!");
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isInGuild(UUID uuid) {
        return RunicGuilds.getDataAPI().getGuildInfo(uuid) != null;
    }

    @Override
    public void removeGuildMember(GuildUUID guildUUID, UUID toRemove) {
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            CompletableFuture<GuildData> future = RunicGuilds.getDataAPI().loadGuildData(guildUUID, jedis);
            future.whenComplete((GuildData guildData, Throwable ex) -> {
                if (ex != null) {
                    Bukkit.getLogger().log(Level.SEVERE, "RunicGuilds failed to remove player from guild");
                    ex.printStackTrace();
                } else {
                    Map<UUID, MemberData> memberDataMap = guildData.getMemberDataMap();
                    memberDataMap.remove(toRemove);
                    guildData.writeToJedis(jedis);
                }
            });
        }
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
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            guildData.getGuild().setGuildName(name);
            guildData.writeToJedis(jedis);
        } catch (Exception exception) {
            exception.printStackTrace();
            return GuildRenameResult.INTERNAL_ERROR;
        }
        return GuildRenameResult.SUCCESSFUL;
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
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            Guild guild = new Guild(new HashSet<>(), new GuildMember(owner, GuildRank.OWNER, 0, GuildUtil.getOfflinePlayerName(owner)), name, prefix, bank, 45, bankPermissions, 0);
            GuildData guildData = new GuildData(guild);
            guildDataMap.put(prefix, guildData);
            guildData.writeToJedis(jedis);
            GuildMongoData guildMongoData = new GuildMongoData(guild.getGuildPrefix());
            guildData.writeToMongo(guildMongoData);
            guildMongoData.save();
        }
        return GuildCreationResult.SUCCESSFUL;
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
        GuildData data = RunicGuilds.getGuildsAPI().getGuildData(uuid);
        if (data != null) {
            return data.getGuild();
        }
        return null;
    }

    @Override
    public GuildData getGuild(String prefix) {
        for (Map.Entry<Object, SessionData> entry : guildDataMap.entrySet()) {
            GuildData guildData = (GuildData) entry.getValue();
            if (guildData.getGuild().getGuildPrefix().equalsIgnoreCase(prefix)) {
                return guildData;
            }
        }
        return null;
    }

    @Override
    public Guild getGuild(String prefix) {
        GuildData data = RunicGuilds.getGuildsAPI().getGuild(prefix);
        if (data != null) {
            return data.getGuild();
        }
        return null;
    }

    @Override
    public GuildData getGuildData(UUID playerUuid) {
        // Bukkit.broadcastMessage(guildDataMap.size() + " is how many guilds loaded into memory");
        for (Map.Entry<Object, SessionData> entry : guildDataMap.entrySet()) {
            GuildData guildData = (GuildData) entry.getValue();
            if (guildData.getGuild().getOwner().getUUID().equals(playerUuid)) {
                return guildData;
            }
            for (GuildMember member : guildData.getGuild().getMembers()) {
                if (member.getUUID().equals(playerUuid)) {
                    return guildData;
                }
            }
        }
        return null;
    }

    @Override
    public GuildStage getGuildStage(UUID uuid) {
        GuildData data = RunicGuilds.getGuildsAPI().getGuildData(uuid);
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
    public Map<Object, SessionData> getSessionDataMap() {
        return this.guildDataMap;
    }
}
