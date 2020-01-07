package com.runicrealms.runicguilds;

import java.util.List;
import java.util.UUID;

import com.runicrealms.runicguilds.config.GuildUtil;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildMember;
import com.runicrealms.runicguilds.result.GuildCreationResult;

public class RunicGuildsAPI {

    public static GuildCreationResult createGuild(UUID owner, String name, String prefix) {
        return GuildUtil.createGuild(owner, name, prefix);
    }

    public static Guild getGuild(UUID uuid) {
        for (Guild guild : GuildUtil.getAllGuilds()) {
            if (guild.getOwner().getUUID().toString().equalsIgnoreCase(uuid.toString())) {
                return guild;
            }
            for (GuildMember member : guild.getMembers()) {
                if (member.getUUID().toString().equalsIgnoreCase(uuid.toString())) {
                    return guild;
                }
            }
        }
        return null;
    }

    public static Guild getGuild(String prefix) {
        for (Guild guild : GuildUtil.getAllGuilds()) {
            if (guild.getGuildPrefix().equalsIgnoreCase(prefix)) {
                return guild;
            }
        }
        return null;
    }

    public static List<Guild> getAllGuilds() {
        return GuildUtil.getAllGuilds();
    }

}
