package com.runicrealms.runicguilds.listeners;

import com.runicrealms.plugin.events.RunicExpEvent;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.guild.stage.StageReward;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.util.GuildUtil;
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
        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        if (guildData == null) return;
        Guild guild = guildData.getData();
        if (guild.getGuildLevel().getGuildStage().getExp() < GuildStage.getMaxStage().getExp()) return;

        int eventExperience = event.getOriginalAmount(); // exp before other bonuses so we don't apply compound bonuses
        StageReward stageReward = StageReward.COMBAT_BONUS;
        eventExperience *= stageReward.getBuffPercent(); // determine bonus amount

        event.setFinalAmount(event.getFinalAmount() + eventExperience);
    }

}
