package com.runicrealms.runicguilds.util;

import com.runicrealms.runicguilds.api.RunicGuildsAPI;
import com.runicrealms.runicguilds.guilds.Guild;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceholderAPI extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "guild";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getAuthor() {
        return "Excel";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String arg) {

        if (player == null) return null;

        String lowerArg = arg.toLowerCase();
        Guild guild = RunicGuildsAPI.getGuild(player.getUniqueId());

        switch (lowerArg) {
            case "prefix":
                if (guild != null) {
                    return guild.getGuildPrefix();
                } else {
                    return "";
                }
            case "score":
                if (guild != null && guild.getMember(player.getUniqueId()) != null) {
                    return String.valueOf(guild.getMember(player.getUniqueId()).getScore());
                } else {
                    return "0";
                }
            default:
                return "";
        }
    }
}
