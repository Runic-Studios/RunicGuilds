package com.runicrealms.runicguilds.api;

import java.util.List;
import java.util.UUID;

import com.runicrealms.runicguilds.config.GuildUtil;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.result.GuildCreationResult;

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

}
