package com.runicrealms.runicguilds.chat;

import com.runicrealms.api.chat.ChatChannel;
import com.runicrealms.runicguilds.api.RunicGuildsAPI;
import com.runicrealms.runicguilds.data.GuildUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GuildChannel extends ChatChannel {

    @Override
    public String getPrefix() {
        return "&e[&6Guild&e]&r ";
    }

    @Override
    public String getName() {
        return "guild";
    }

    @Override
    public List<Player> getRecipients(Player player) {
        List<Player> recipients = new ArrayList<>();
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) != null) {
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target != null) {
                    if (GuildUtil.getPlayerCache().get(target.getUniqueId()) != null) {
                        if (GuildUtil.getPlayerCache().get(player.getUniqueId()).equalsIgnoreCase(GuildUtil.getPlayerCache().get(target.getUniqueId()))) {
                            recipients.add(target);
                        }
                    }
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "You must be in a guild to use guild chat!");
        }
        return recipients;
    }

    @Override
    public String getMessageFormat() {
        return "&e[%runic_guild_score%] %luckperms_meta_name_color%%player_name%: &e%message%";
    }

    // TODO: enable guild placeholders in guild chat
    private String displayScore(Player pl) {
        if (RunicGuildsAPI.getGuild(pl.getUniqueId()) != null
                && RunicGuildsAPI.getGuild(pl.getUniqueId()).getMember(pl.getUniqueId()) != null) {
            return String.valueOf(RunicGuildsAPI.getGuild(pl.getUniqueId()).getMember(pl.getUniqueId()).getScore());
        } else {
            return "0";
        }
    }
}
