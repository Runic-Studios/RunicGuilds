package com.runicrealms.runicguilds.util;

import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.model.GuildInfo;
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
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);

        switch (lowerArg) {
            case "name":
                if (guildInfo != null) {
                    return guildInfo.getName();
                } else {
                    return "";
                }
            case "prefix":
                if (guildInfo != null) {
                    return "[" + guildInfo.getPrefix() + "] ";
                } else {
                    return "";
                }
//            case "rank":
////                if (guildInfo != null) {
////                    return guild.getMember(player.getUniqueId()).getRank().getName();
////                } else {
////                    return GuildRank.MEMBER.getName();
////                }
//                return GuildRank.MEMBER.getName();
//            case "score":
////                if (guildInfo != null && guild.getMember(player.getUniqueId()) != null) {
////                    return String.valueOf(guild.getMember(player.getUniqueId()).getScore());
////                } else {
//                return "0";
//                }
            default:
                return "";
        }
    }
}
