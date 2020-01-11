package com.runicrealms.runicguilds.api;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.runicrealms.runicguilds.config.GuildUtil;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildMember;

public class RunicGuildsAPI {

    public static GuildCreationResult createGuild(UUID owner, String name, String prefix) {
        return GuildUtil.createGuild(owner, name, prefix);
    }

    public static Guild getGuild(UUID uuid) {
    	return GuildUtil.getGuild(uuid);
    }

    public static Guild getGuild(String prefix) {
        return GuildUtil.getGuild(prefix);
    }

    public static List<Guild> getAllGuilds() {
        return GuildUtil.getAllGuilds();
    }

    public static boolean isInGuild(UUID player) {
        return GuildUtil.getGuild(player) != null;
    }

    public static boolean addPlayerScore(UUID player, Integer score) {
        if (isInGuild(player)) {
            Guild guild = GuildUtil.getGuild(player);
            guild.increasePlayerScore(player, score);
            GuildUtil.saveGuild(guild);
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
