package com.runicrealms.runicguilds.listeners;

import com.runicrealms.plugin.events.RunicExpEvent;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.guild.stage.StageReward;
import com.runicrealms.runicguilds.model.GuildData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Listener for the guild stage exp bonus defined in the GuildStage enum
 */
public class RewardExpListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerGainExperience(RunicExpEvent event) {

        Player player = event.getPlayer();

        // ensure this perk can be unlocked
        StageReward stageReward = StageReward.EXP_BONUS;
        GuildStage guildStage = GuildStage.getFromReward(stageReward);
        if (guildStage == null) return;

        // ensure there is a guild
        GuildData guildData = RunicGuilds.getRunicGuildsAPI().getGuildData(player.getUniqueId());
        if (guildData == null) return;

        // ensure guild has perk unlocked
        Guild guild = guildData.getGuild();
        if (guild.getGuildStage().getRank() < guildStage.getRank()) return;

        int eventExperience = event.getOriginalAmount(); // exp before other bonuses so we don't apply compound bonuses
        eventExperience *= stageReward.getBuffPercent(); // determine bonus amount

        event.setFinalAmount(event.getFinalAmount() + eventExperience);
    }

}
