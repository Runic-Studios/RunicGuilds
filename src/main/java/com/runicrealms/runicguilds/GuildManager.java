package com.runicrealms.runicguilds;

import com.keenant.tabbed.item.TextTabItem;
import com.keenant.tabbed.tablist.TableTabList;
import com.keenant.tabbed.util.Skins;
import com.runicrealms.RunicChat;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.TabUpdateEvent;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.runicguilds.api.GuildsAPI;
import com.runicrealms.runicguilds.api.event.GuildCreationEvent;
import com.runicrealms.runicguilds.api.event.GuildScoreChangeEvent;
import com.runicrealms.runicguilds.guild.GuildCreationResult;
import com.runicrealms.runicguilds.guild.GuildRank;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.model.GuildInfo;
import com.runicrealms.runicguilds.model.MemberData;
import com.runicrealms.runicguilds.util.GuildUtil;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuildManager implements GuildsAPI, Listener {

    public GuildManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicGuilds.getInstance());
    }

    /**
     * Updates the player's guild column in tab
     *
     * @param player       to update
     * @param tableTabList from some tab update event
     */
    public static void updateGuildTab(Player player, TableTabList tableTabList) {
        tableTabList.set(3, 0, new TextTabItem
                (ChatColor.GOLD + String.valueOf(ChatColor.BOLD) + "  Guild [0]", 0, Skins.getDot(ChatColor.GOLD)));
        GuildUtil.updateGuildTabColumn(player, tableTabList);
    }

    public static void updateGuildTab(Player player) {
        updateGuildTab(player, RunicCore.getTabAPI().getPlayerTabList(player));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTabUpdate(TabUpdateEvent event) {
        updateGuildTab(event.getPlayer(), event.getTableTabList());
    }

    @Override
    public boolean addGuildScore(UUID guildUUID, MemberData memberData, Integer score) {
        UUID uuid = memberData.getUuid();
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(guildUUID);
        if (guildInfo == null) {
            Bukkit.getLogger().info("A guild was not found to add guild score for " + uuid);
            return false;
        }
        Bukkit.getPluginManager().callEvent(new GuildScoreChangeEvent
                (
                        guildInfo.getUUID(),
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
                            guildInfo.getUUID(),
                            owner.getUniqueId(),
                            modCreated
                    ));
        } else {
            // todo: Handle failure
        }
        return result;
    }

    @Override
    public GuildStage getGuildStage(UUID guildUUID) {
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(guildUUID);
        if (guildInfo == null) {
            return GuildStage.STAGE_0;
        } else {
            return GuildStage.getFromExp(guildInfo.getExp());
        }
    }

    @Override
    public void giveExperience(UUID guildUUID, int exp) {
        Bukkit.getScheduler().runTaskAsynchronously(RunicGuilds.getInstance(), () -> {
            try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
                // Update in-memory
                GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(guildUUID);
                guildInfo.setExp(guildInfo.getExp() + exp);
                // Update in Redis
                GuildData guildData = RunicGuilds.getDataAPI().loadGuildData(guildUUID);
                guildData.setExp(guildInfo.getExp());
                guildData.writeToJedis(jedis);
            }
        });
    }

    @Override
    public boolean isInGuild(UUID player) {
        return RunicGuilds.getDataAPI().getGuildInfo(player) != null;
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
        try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
            // Create new guild, write to Redis (will mark it for Mongo save)
            GuildData guildData = new GuildData
                    (
                            new ObjectId(),
                            UUID.randomUUID(),
                            name,
                            prefix,
                            new MemberData(ownerUuid, GuildRank.OWNER, 0)
                    );
            guildData.addDocumentToMongo();
            guildData.writeToJedis(jedis);
            // Cache latency-sensitive fields in-memory
            GuildInfo guildInfo = new GuildInfo(guildData);
            RunicGuilds.getDataAPI().addGuildInfoToMemory(guildInfo);
//            RunicGuilds.getDataAPI().getPlayerToGuildMap().put(ownerUuid, guildInfo.getUUID());
//            Bukkit.broadcastMessage("adding guild info to memory");
//            Bukkit.broadcastMessage("guildUUID is " + guildInfo.getUUID().getUUID());
        }
        return GuildCreationResult.SUCCESSFUL;
    }

}
