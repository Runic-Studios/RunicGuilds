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
                    return "[" + RunicGuildsAPI.getGuild(pl.getUniqueId()).getGuildPrefix() + "] ";
                } else {
                    return "";
                }
            default:
                return "";
        }
    }
}
