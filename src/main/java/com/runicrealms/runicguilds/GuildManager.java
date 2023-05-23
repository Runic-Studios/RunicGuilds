package com.runicrealms.runicguilds;

import com.keenant.tabbed.item.TextTabItem;
import com.keenant.tabbed.tablist.TableTabList;
import com.keenant.tabbed.util.Skins;
import com.runicrealms.RunicChat;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runicguilds.api.GuildsAPI;
import com.runicrealms.runicguilds.api.event.GuildCreationEvent;
import com.runicrealms.runicguilds.api.event.GuildScoreChangeEvent;
import com.runicrealms.runicguilds.guild.GuildCreationResult;
import com.runicrealms.runicguilds.guild.GuildRank;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.model.GuildInfo;
import com.runicrealms.runicguilds.model.GuildUUID;
import com.runicrealms.runicguilds.model.MemberData;
import com.runicrealms.runicguilds.util.GuildBankUtil;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuildManager implements GuildsAPI, Listener {

//    public GuildManager() {
//        startTabUpdateTask();
//    }

    /**
     * Used in the running task to keep party health displays accurate.
     */
    public static void updateGuildTab(Player player) {
        TableTabList tableTabList = RunicCore.getTabAPI().getPlayerTabList(player);
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (guildInfo == null) {
            tableTabList.set(3, 0, new TextTabItem
                    (ChatColor.GOLD + "" + ChatColor.BOLD + "  Guild [0]", 0, Skins.getDot(ChatColor.GOLD)));
            // Reset members
            for (int i = 1; i < 20; i++) {
                tableTabList.remove(3, i);
            }
        } else {
            getMembersAndPopulate(tableTabList, guildInfo);
        }
    }

    private static void getMembersAndPopulate(TableTabList tableTabList, GuildInfo guildInfo) {
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            Map<UUID, MemberData> memberDataMap = RunicGuilds.getDataAPI().loadGuildMembers(guildInfo.getGuildUUID(), jedis);
            List<UUID> onlineMembers = memberDataMap.values().stream().map(MemberData::getUuid)
                    .filter(uuid -> Bukkit.getPlayer(uuid) != null).toList();
            tableTabList.set(3, 0, new TextTabItem
                    (ChatColor.GOLD + "" + ChatColor.BOLD + "  Guild [" + onlineMembers.size() + "]", 0, Skins.getDot(ChatColor.GOLD)));
            // Reset members
            for (int i = 1; i < 20; i++) {
                tableTabList.remove(3, i);
            }
            int k = 0;
            for (UUID guildMember : onlineMembers) {
                Player member = Bukkit.getPlayer(guildMember);
                if (member == null) continue; // Insurance
                if (k > 19) break;
                tableTabList.set(3, k + 1, new TextTabItem(member.getName(), member.getPing(), Skins.getPlayer(member)));
                k++;
            }
        }
    }

    @Override
    public void addBankViewer(GuildUUID guildUUID, UUID uuid) {
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            jedis.sadd(GuildBankUtil.getJedisKey(guildUUID, jedis), uuid.toString());
        }
    }

    @Override
    public boolean addGuildScore(GuildUUID guildUUID, MemberData memberData, Integer score) {
        UUID uuid = memberData.getUuid();
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(guildUUID);
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
            GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(owner);
            Bukkit.getServer().getPluginManager().callEvent(new GuildCreationEvent
                    (
                            guildInfo.getGuildUUID(),
                            owner.getUniqueId(),
                            modCreated
                    ));
        } else {
            // todo: Handle failure
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
                GuildData guildData = RunicGuilds.getDataAPI().loadGuildDataNoBank(guildUUID.getUUID());
                guildData.setExp(guildInfo.getExp());
                guildData.writeToJedis(jedis);
            }
        });
    }

    @Override
    public boolean isInGuild(OfflinePlayer offlinePlayer) {
        return RunicGuilds.getDataAPI().getGuildInfo(offlinePlayer) != null;
    }

    @Override
    public void removeBankViewer(GuildUUID guildUUID, UUID uuid) {
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            jedis.srem(GuildBankUtil.getJedisKey(guildUUID, jedis), uuid.toString());
        }
    }

//    /**
//     * Keeps party column updated w/ player health.
//     */
//    private void startTabUpdateTask() {
//        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicGuilds.getInstance(), () -> {
//            for (Player online : Bukkit.getOnlinePlayers()) {
//                if (RunicCore.getTabAPI().getPlayerTabList(online) == null) continue;
//                updateGuildColumn(online);
//            }
//        }, 200L, 5L);
//    }

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

        // Use a regular expression to filter bad words
        String regex = "(?i)\\b(" + String.join("|", RunicChat.getWordsToFilter()) + ")\\b";
        String filteredName = name.replaceAll(regex, "***");
        boolean nameWasFiltered = !filteredName.equals(name);
        if (nameWasFiltered) {
            return GuildCreationResult.INAPPROPRIATE_CONTENT;
        }
        String filteredPrefix = prefix.replaceAll(regex, "***");
        boolean prefixWasFiltered = !filteredPrefix.equals(prefix);
        if (prefixWasFiltered) {
            return GuildCreationResult.INAPPROPRIATE_CONTENT;
        }

        for (GuildInfo guildInfo : RunicGuilds.getDataAPI().getGuildInfoMap().values()) {
            String otherName = guildInfo.getName();
            String otherPrefix = guildInfo.getPrefix();
            if (otherName.equalsIgnoreCase(name)) {
                return GuildCreationResult.NAME_NOT_UNIQUE;
            }
            // todo: ensure members cannot make guild
//            for (GuildMember member : guildData.getGuild().getMembers()) {
//                if (member.getUUID().toString().equalsIgnoreCase(owner.toString())) {
//                    return GuildCreationResult.CREATOR_IN_GUILD;
//                }
//            }
            if (otherPrefix.equalsIgnoreCase(prefix)) {
                return GuildCreationResult.PREFIX_NOT_UNIQUE;
            }
        }
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
            guildData.addDocumentToMongo();
            guildData.writeToJedis(jedis);
            RunicGuilds.getDataAPI().setGuildForPlayer(ownerUuid, name);
            // Cache latency-sensitive fields in-memory
            GuildInfo guildInfo = new GuildInfo(guildData);
            RunicGuilds.getDataAPI().addGuildInfoToMemory(guildInfo);
            RunicGuilds.getDataAPI().getPlayerToGuildMap().put(ownerUuid, guildInfo.getGuildUUID().getUUID());
//            Bukkit.broadcastMessage("adding guild info to memory");
//            Bukkit.broadcastMessage("guildUUID is " + guildInfo.getGuildUUID().getUUID());
        }
        return GuildCreationResult.SUCCESSFUL;
    }

}
