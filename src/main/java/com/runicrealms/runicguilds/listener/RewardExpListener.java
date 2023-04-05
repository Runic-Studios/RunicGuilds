package com.runicrealms.runicguilds.listener;

import com.runicrealms.plugin.events.RunicExpEvent;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.guild.stage.StageReward;
import com.runicrealms.runicguilds.model.GuildInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * Listener for the guild stage exp bonus defined in the GuildStage enum
 */
public class RewardExpListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerGainExperience(RunicExpEvent event) {
        if (event.getRunicExpSource() != RunicExpEvent.RunicExpSource.MOB
                && event.getRunicExpSource() != RunicExpEvent.RunicExpSource.PARTY)
            return; // Only mobs or party kills

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Ensure this perk can be unlocked
        StageReward expStageReward = StageReward.EXP_BONUS;
        GuildStage guildStage = GuildStage.getFromReward(expStageReward);
        if (guildStage == null) return;

        // Ensure there is a guild
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(uuid);
        if (guildInfo == null) return;

        // Ensure guild has perk unlocked
        if (guildInfo.getExp() < guildStage.getExp()) return;

        int eventExperience = event.getOriginalAmount(); // exp before other bonuses so we don't apply compound bonuses
        eventExperience *= expStageReward.getBuffPercent(); // determine bonus amount

        event.setFinalAmount(event.getFinalAmount() + eventExperience);
    }

}
