package com.runicrealms.runicguilds;

import com.runicrealms.RunicChat;
import com.runicrealms.plugin.common.api.GuildsAPI;
import com.runicrealms.plugin.common.api.guilds.GuildCreationResult;
import com.runicrealms.plugin.common.api.guilds.GuildRank;
import com.runicrealms.plugin.common.api.guilds.GuildStage;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.runicguilds.api.event.GuildCreationEvent;
import com.runicrealms.runicguilds.api.event.GuildScoreChangeEvent;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.model.GuildInfo;
import com.runicrealms.runicguilds.model.MemberData;
import com.runicrealms.runicguilds.util.GuildUtil;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuildManager implements GuildsAPI, Listener {

    public GuildManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicGuilds.getInstance());
    }

    @Override
    public boolean addGuildScore(UUID player, Integer score, boolean sendMessage) {
        GuildInfo info = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (info == null) return false;
        MemberData data = RunicGuilds.getDataAPI().loadMemberData(info.getUUID(), player);
        if (data == null) return false;
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(info.getUUID());
        if (guildInfo == null) return false;
        Bukkit.getPluginManager().callEvent(new GuildScoreChangeEvent
                (
                        guildInfo.getUUID(),
                        data,
                        score
                ));
        Player target = Bukkit.getPlayer(player);
        if (sendMessage && target != null) {
            info.getMembersUuids().forEach(memberUUID -> {
                Player online = Bukkit.getPlayer(memberUUID);
                if (online != null && !memberUUID.equals(player)) {
                    online.sendMessage(ColorUtil.format(GuildUtil.PREFIX + target.getName() + " has earned your guild &6&l" + score + "&r&e guild points!"));
                }
            });
        }
        return true;
    }

    @Override
    public void addBulkGuildScore(Map<UUID, Integer> scores, boolean sendMessage) {
        processPlayerQueue(scores, new LinkedList<>(scores.keySet()), sendMessage);
    }

    private void processPlayerQueue(Map<UUID, Integer> damageScores, LinkedList<UUID> playerQueue, boolean sendMessage) {
        if (playerQueue.isEmpty()) {
            return;
        }
        UUID player = playerQueue.pop();
        Bukkit.getScheduler().runTask(RunicGuilds.getInstance(), () -> {
            if (!RunicGuilds.getGuildsAPI().addGuildScore(player, damageScores.get(player), sendMessage)) {
                processPlayerQueue(damageScores, playerQueue, sendMessage);
                return;
            }
            Bukkit.getScheduler().runTaskAsynchronously(RunicGuilds.getInstance(), () -> processPlayerQueue(damageScores, playerQueue, sendMessage));
        });
    }

    @Override
    public GuildCreationResult createGuild(Player owner, String name, String prefix, boolean modCreated) {
        GuildCreationResult result = createGuild(owner, name, prefix);
        if (result == GuildCreationResult.SUCCESSFUL) {
            owner.playSound(owner.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(owner);
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
            // Update in-memory
            GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(guildUUID);
            guildInfo.setExp(guildInfo.getExp() + exp);
            // Update in Redis
            GuildData guildData = RunicGuilds.getDataAPI().loadGuildData(guildUUID);
            guildData.setExp(guildInfo.getExp());
            // todo: write to mongo
        });
    }

    @Override
    public boolean isInGuild(Player player) {
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
     * @param owner  the guild owner
     * @param name   of the guild
     * @param prefix of the guild
     * @return the creation result
     */
    private GuildCreationResult createGuild(Player owner, String name, String prefix) {
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
            if (guildInfo.getMembersUuids().contains(owner.getUniqueId())) {
                return GuildCreationResult.CREATOR_IN_GUILD;
            }
            if (otherPrefix.equalsIgnoreCase(prefix)) {
                return GuildCreationResult.PREFIX_NOT_UNIQUE;
            }
        }
        // Create new guild, save to Mongo
        GuildData guildData = new GuildData
                (
                        new ObjectId(),
                        UUID.randomUUID(),
                        name,
                        prefix,
                        new MemberData(owner.getUniqueId(), owner.getName(), GuildRank.OWNER, 0)
                );
        guildData.addDocumentToMongo();
        // Cache latency-sensitive fields in-memory
        GuildInfo guildInfo = new GuildInfo(guildData);
        RunicGuilds.getDataAPI().addGuildInfoToMemory(guildInfo);
        return GuildCreationResult.SUCCESSFUL;
    }

}
