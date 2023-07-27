package com.runicrealms.plugin.runicguilds.listener;

import com.runicrealms.plugin.api.event.ScoreboardUpdateEvent;
import com.runicrealms.plugin.runicguilds.RunicGuilds;
import com.runicrealms.plugin.runicguilds.model.GuildInfo;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class GuildScoreboardListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH) // late
    public void onScoreboardUpdate(ScoreboardUpdateEvent event) {
        try {
            GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(event.getPlayer());
            String guild = "None";
            if (guildInfo != null && !guildInfo.getName().equals("")) guild = guildInfo.getName();
            event.setGuild(guild);
        } catch (Exception ex) {
            Bukkit.getLogger().warning("RunicGuilds failed to update scoreboard for " + event.getPlayer().getUniqueId());
        }
    }

}
