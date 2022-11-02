package com.runicrealms.runicguilds.api;

import com.runicrealms.runicguilds.api.event.GuildCreationEvent;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.GuildCreationResult;
import com.runicrealms.runicguilds.guild.GuildMember;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.util.GuildUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RunicGuildsAPI {

    public static GuildCreationResult createGuild(UUID owner, String name, String prefix, boolean modCreated) {
        GuildCreationResult result = GuildUtil.createGuild(owner, name, prefix);
        if (result == GuildCreationResult.SUCCESSFUL) {
            Bukkit.getPlayer(owner).playSound(Bukkit.getPlayer(owner).getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            GuildData.setGuildForPlayer(name, owner.toString());
            Bukkit.getServer().getPluginManager().callEvent(new GuildCreationEvent(GuildUtil.getGuildData(prefix).getData(), modCreated));
        }
        return result;
    }

    public static Guild getGuild(UUID uuid) {
        GuildData data = GuildUtil.getGuildData(uuid);
        if (data != null) {
            return data.getData();
        }
        return null;
    }

    public static Guild getGuild(String prefix) {
        GuildData data = GuildUtil.getGuildData(prefix);
        if (data != null) {
            return data.getData();
        }
        return null;
    }

    public static GuildStage getGuildStage(UUID uuid) {
        GuildData data = GuildUtil.getGuildData(uuid);
        if (data != null) {
            return data.getData().getGuildLevel().getGuildStage();
        }
        return null;
    }

    public static GuildStage getGuildStage(String prefix) {
        GuildData data = GuildUtil.getGuildData(prefix);
        if (data != null) {
            return data.getData().getGuildLevel().getGuildStage();
        }
        return null;
    }

    public static List<Guild> getAllGuilds() {
        return GuildUtil.getAllGuilds();
    }

    public static boolean isInGuild(UUID player) {
        return GuildUtil.getGuildData(player).getData() != null;
    }

    /**
     * @param player
     * @param score
     * @return
     */
    // todo: save to redis here?
    public static boolean addPlayerScore(UUID player, Integer score) {
        if (isInGuild(player)) {
            GuildData guildData = GuildUtil.getGuildData(player);
            if (guildData != null) {
                guildData.getData().increasePlayerScore(player, score);
                guildData.getData().recalculateScore();
            }
            return true;
        }
        return false;
    }

    public static Set<UUID> getGuildRecipients(UUID player) {
        if (!isInGuild(player)) {
            return null;
        }
        Set<UUID> recipients = new HashSet<UUID>();
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
}
