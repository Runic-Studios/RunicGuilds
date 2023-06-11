package com.runicrealms.runicguilds.listener;

import com.runicrealms.plugin.api.event.ScoreboardUpdateEvent;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.model.GuildInfo;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class GuildScoreboardListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH) // late
    public void onScoreboardUpdate(ScoreboardUpdateEvent event) {
        try {
            GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(event.getPlayer());
            event.setGuild(guildInfo.getName().equals("") ? "None" : guildInfo.getName());
        } catch (Exception ex) {
            Bukkit.getLogger().warning("RunicProfessions failed to update scoreboard for " + event.getPlayer().getUniqueId());
        }
    }

}
