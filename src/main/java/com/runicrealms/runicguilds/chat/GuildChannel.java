package com.runicrealms.runicguilds.chat;

import com.runicrealms.api.chat.ChatChannel;
import com.runicrealms.runicguilds.data.GuildUtil;
import org.bukkit.Bukkit;
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
        }
        return recipients;
    }

    @Override
    public String getMessageFormat() {
        return "&7%player_name%&8: &e%message%";
    }
}
