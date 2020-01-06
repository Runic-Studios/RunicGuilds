package com.runicrealms.runicguilds;

import com.runicrealms.runicguilds.config.GuildLoader;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildMember;

import java.util.List;
import java.util.UUID;

public class RunicGuildsAPI {

    public static void createGuild(UUID owner, String name, String prefix) {
        GuildLoader.createGuild(owner, name, prefix);
    }

    public static Guild getGuild(UUID uuid) {
        for (Guild guild : Plugin.getGuilds()) {
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
        for (Guild guild : Plugin.getGuilds()) {
            if (guild.getGuildPrefix().equalsIgnoreCase(prefix)) {
                return guild;
            }
        }
        return null;
    }

    public static List<Guild> getAllGuilds() {
        return GuildLoader.getAllGuilds();
    }

}
