package com.runicrealms.runicguilds.util;

import com.runicrealms.runicguilds.api.RunicGuildsAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceholderAPI extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "runic";
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
    public String onPlaceholderRequest(Player pl, String arg) {

        if (pl == null)  return null;

        String lowerArg = arg.toLowerCase();

        switch (lowerArg) {
            case "guild_prefix":
                if (RunicGuildsAPI.getGuild(pl.getUniqueId()) != null) {
                    return "[" + RunicGuildsAPI.getGuild(pl.getUniqueId()).getGuildPrefix()
                            + "|"
                            + RunicGuildsAPI.getGuild(pl.getUniqueId()).getScore() + "] ";
                } else {
                    return "";
                }
            case "guild_score":
                if (RunicGuildsAPI.getGuild(pl.getUniqueId()) != null
                        && RunicGuildsAPI.getGuild(pl.getUniqueId()).getMember(pl.getUniqueId()) != null) {
                    return String.valueOf(RunicGuildsAPI.getGuild(pl.getUniqueId()).getMember(pl.getUniqueId()).getScore());
                } else {
                    return "0";
                }
            default:
                return "";
        }
    }
}
