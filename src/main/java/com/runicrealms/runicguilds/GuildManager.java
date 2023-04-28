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
import com.runicrealms.runicguilds.util.GuildBankUtil;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuildManager implements GuildsAPI, Listener {

    @Override
    public void addBankViewer(GuildUUID guildUUID, UUID uuid) {
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            jedis.sadd(GuildBankUtil.getJedisKey(guildUUID, jedis), uuid.toString());
        }
    }

    @Override
    public boolean addGuildScore(GuildUUID guildUUID, MemberData memberData, Integer score) {
        UUID uuid = memberData.getUuid();
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(uuid);
        if (guildInfo == null) {
            Bukkit.getLogger().info("A guild was not found to add guild score for " + uuid);
            return false;
        }
        Bukkit.getPluginManager().callEvent(new GuildScoreChangeEvent
                (
                        guildInfo.getGuildUUID(),
                        memberData,
                        score
                ));
        return true;
    }

    @Override
    public GuildCreationResult createGuild(Player owner, String name, String prefix, boolean modCreated) {
        GuildCreationResult result = createGuild(owner.getUniqueId(), name, prefix);
        if (result == GuildCreationResult.SUCCESSFUL) {
            owner.playSound(owner.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(owner.getUniqueId());
            Bukkit.getServer().getPluginManager().callEvent(new GuildCreationEvent
                    (
                            guildInfo.getGuildUUID(),
                            owner.getUniqueId(),
                            modCreated
                    ));
        }
        return result;
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
        Bukkit.getScheduler().runTaskAsynchronously(RunicGuilds.getInstance(), () -> {
            try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
                // Update in-memory
                GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(guildUUID);
                guildInfo.setExp(guildInfo.getExp() + exp);
                // Update in Redis
                GuildData guildData = RunicGuilds.getDataAPI().loadGuildDataNoBank(guildUUID, jedis);
                guildData.setExp(guildInfo.getExp());
                guildData.writeToJedis(jedis);
            }
        });
    }

    @Override
    public boolean isInGuild(UUID uuid) {
        return RunicGuilds.getDataAPI().getGuildInfo(uuid) != null;
    }

    @Override
    public void removeBankViewer(GuildUUID guildUUID, UUID uuid) {
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            jedis.srem(GuildBankUtil.getJedisKey(guildUUID, jedis), uuid.toString());
        }
    }

    @Override
    public void removeGuildMember(GuildUUID guildUUID, UUID toRemove) {
        Bukkit.getScheduler().runTaskAsynchronously(RunicGuilds.getInstance(), () -> {
            try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
                GuildData guildDataNoBank = RunicGuilds.getDataAPI().loadGuildDataNoBank(guildUUID, jedis);
                guildDataNoBank.removeMember(toRemove, jedis);
                guildDataNoBank.writeToJedis(jedis);
                // todo: edit their 'guild' tag in Redis?
            }
        });
    }

    @Override
    public GuildRenameResult renameGuild(GuildUUID guildUUID, String name) {
        if (name.length() > 16) {
            return GuildRenameResult.NAME_TOO_LONG;
        }
        // todo: ensure prefix unique
//        for (Object obj : guildDataMap.keySet()) {
//            String otherGuildPrefix = (String) obj;
//            GuildData guildDataOther = (GuildData) guildDataMap.get(otherGuildPrefix);
//            if (guildDataOther.getGuild().getGuildName().equalsIgnoreCase(name)) {
//                return GuildRenameResult.NAME_NOT_UNIQUE;
//            }
//        }
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(guildUUID);
            guildInfo.setName(name);
            RunicGuilds.getDataAPI().renameGuildInRedis(guildUUID, name, jedis);
        } catch (Exception exception) {
            exception.printStackTrace();
            return GuildRenameResult.INTERNAL_ERROR;
        }
        return GuildRenameResult.SUCCESSFUL;
    }

    /**
     * Attempts to create a guild
     *
     * @param ownerUuid of the guild owner
     * @param name      of the guild
     * @param prefix    of the guild
     * @return the creation result
     */
    private GuildCreationResult createGuild(UUID ownerUuid, String name, String prefix) {
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
        // todo: Ensure guild name and prefix are unique?
//        for (Object obj : guildDataMap.keySet()) {
//            String guildPrefix = (String) obj;
//            GuildData guildData = (GuildData) guildDataMap.get(guildPrefix);
//            if (guildData.getGuild().getGuildName().equalsIgnoreCase(name)) {
//                return GuildCreationResult.NAME_NOT_UNIQUE;
//            }
//            if (guildData.getGuild().getOwner().getUUID().toString().equalsIgnoreCase(owner.toString())) {
//                return GuildCreationResult.CREATOR_IN_GUILD;
//            }
//            for (GuildMember member : guildData.getGuild().getMembers()) {
//                if (member.getUUID().toString().equalsIgnoreCase(owner.toString())) {
//                    return GuildCreationResult.CREATOR_IN_GUILD;
//                }
//            }
//            if (guildPrefix.equalsIgnoreCase(prefix)) {
//                return GuildCreationResult.PREFIX_NOT_UNIQUE;
//            }
//        }
        // Setup empty bank
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            // Create new guild, write to Redis (will mark it for Mongo save)
            GuildData guildData = new GuildData
                    (
                            new ObjectId(),
                            new GuildUUID(UUID.randomUUID()),
                            name,
                            prefix,
                            new MemberData(ownerUuid, GuildRank.OWNER, 0)
                    );
            guildData.writeToJedis(jedis);
            RunicGuilds.getDataAPI().setGuildForPlayer(ownerUuid, name);
            // Ensure there is a local copy of some fields for fast lookup
            GuildInfo guildInfo = new GuildInfo(guildData);
            RunicGuilds.getDataAPI().addGuildInfoToMemory(guildInfo);
        }
        return GuildCreationResult.SUCCESSFUL;
    }

}
