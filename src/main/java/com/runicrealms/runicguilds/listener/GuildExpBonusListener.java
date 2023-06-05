package com.runicrealms.runicguilds.listener;

import com.runicrealms.plugin.events.RunicCombatExpEvent;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.guild.stage.StageReward;
import com.runicrealms.runicguilds.model.GuildInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Listener for the guild stage exp bonus defined in the GuildStage enum
 */
public class GuildExpBonusListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerGainExperience(RunicCombatExpEvent event) {
        if (event.getRunicExpSource() != RunicCombatExpEvent.RunicExpSource.MOB)
            return; // Only mobs or party kills

        Player player = event.getPlayer();

        // Ensure this perk can be unlocked
        StageReward expStageReward = StageReward.EXP_BONUS;
        GuildStage guildStage = GuildStage.getFromReward(expStageReward);
        if (guildStage == null) return;

        // Ensure there is a guild
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (guildInfo == null) return;

        // Ensure guild has perk unlocked
        if (guildInfo.getExp() < guildStage.getExp()) return;

        event.setBonus(RunicCombatExpEvent.BonusType.GUILD, expStageReward.getBuffPercent());
    }

}
