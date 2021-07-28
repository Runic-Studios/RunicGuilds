package com.runicrealms.runicguilds.listeners;

import com.runicrealms.plugin.events.RunicExpEvent;
import com.runicrealms.runicguilds.data.GuildData;
import com.runicrealms.runicguilds.data.GuildUtil;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildStage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PlayerGainExperience implements Listener {

    private static final double EXP_BONUS = .05;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerGainExperience(RunicExpEvent event) {
        Player player = event.getPlayer();

        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        if (guildData == null) {
            return;
        }

        Guild guild = guildData.getData();

        if (guild.getGuildLevel().getGuildStage().getExp() < GuildStage.STAGE7.getExp()) {
            return;
        }

        int eventExperience = event.getOriginalAmount(); // exp before other bonuses so we don't apply compound bonuses
        eventExperience *= EXP_BONUS;

        event.setFinalAmount(event.getFinalAmount() + eventExperience);
    }

}
