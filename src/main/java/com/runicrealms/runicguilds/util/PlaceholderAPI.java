package com.runicrealms.runicguilds.util;

import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.GuildRank;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceholderAPI extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "guild";
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
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String arg) {

        if (player == null) return null;

        String lowerArg = arg.toLowerCase();
        Guild guild = RunicGuilds.getGuildsAPI().getGuild(player.getUniqueId());

        switch (lowerArg) {
            case "name":
                if (guild != null) {
                    return guild.getGuildName();
                } else {
                    return "";
                }
            case "prefix":
                if (guild != null) {
                    return "[" + guild.getGuildPrefix() + "] ";
                } else {
                    return "";
                }
            case "rank":
                if (guild != null) {
                    return guild.getMember(player.getUniqueId()).getRank().getName();
                } else {
                    return GuildRank.MEMBER.getName();
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
