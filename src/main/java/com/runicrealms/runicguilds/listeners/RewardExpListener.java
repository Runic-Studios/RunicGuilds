package com.runicrealms.runicguilds.listeners;

import com.runicrealms.plugin.events.RunicExpEvent;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.guild.stage.StageReward;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.util.GuildUtil;
import org.bukkit.Bukkit;
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
        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        if (guildData == null) return;

        // ensure guild has perk unlocked
        Guild guild = guildData.getData();
        if (guild.getGuildStage().getRank() < guildStage.getRank()) return;

        int eventExperience = event.getOriginalAmount(); // exp before other bonuses so we don't apply compound bonuses
        eventExperience *= stageReward.getBuffPercent(); // determine bonus amount

        Bukkit.broadcastMessage("old was: " + event.getFinalAmount() + " and new is " + (event.getFinalAmount() + eventExperience));
        event.setFinalAmount(event.getFinalAmount() + eventExperience);
    }

}
