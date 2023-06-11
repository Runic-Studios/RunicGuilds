package com.runicrealms.runicguilds.listener;

import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.guild.stage.StageReward;
import com.runicrealms.runicguilds.model.GuildInfo;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class RewardDamageListener implements Listener {

    /**
     * Applies combat experience bonus if the player is in a guild of the right stage. Only applies to monsters
     *
     * @param player            to check for bonus
     * @param victim            entity being attacked
     * @param damageBeforeBonus event damage
     */
    private int guildCombatBonus(Player player, LivingEntity victim, int damageBeforeBonus) {
        if (victim instanceof Player) return damageBeforeBonus;
        StageReward damageReward = StageReward.COMBAT_BONUS;
        GuildStage requiredStage = GuildStage.getFromReward(damageReward);
        if (requiredStage == null) return damageBeforeBonus;
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (guildInfo == null) return damageBeforeBonus;
        GuildStage guildStage = GuildStage.getFromExp(guildInfo.getExp());
        if (guildStage.getRank() < requiredStage.getRank())
            return damageBeforeBonus;
        int bonusDamage = (int) (damageBeforeBonus * damageReward.getBuffPercent());
        return damageBeforeBonus + bonusDamage;
    }

    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onSpellDamage(MagicDamageEvent event) {
        event.setAmount(guildCombatBonus(event.getPlayer(), event.getVictim(), event.getAmount()));
    }

    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onWeaponDamage(PhysicalDamageEvent event) {
        event.setAmount(guildCombatBonus(event.getPlayer(), event.getVictim(), event.getAmount()));
    }
}
