package com.runicrealms.runicguilds.listeners;

import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.guild.stage.StageReward;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.util.GuildUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class RewardDamageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpellDamage(MagicDamageEvent event) {
        event.setAmount(guildCombatBonus(event.getPlayer(), event.getVictim(), event.getAmount()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWeaponDamage(PhysicalDamageEvent event) {
        event.setAmount(guildCombatBonus(event.getPlayer(), event.getVictim(), event.getAmount()));
    }

    /**
     * Applies combat experience bonus if the player is in a guild of the right stage. Only applies to monsters
     *
     * @param player            to check for bonus
     * @param victim            entity being attacked
     * @param damageBeforeBonus event damage
     */
    private int guildCombatBonus(Player player, LivingEntity victim, int damageBeforeBonus) {
        if (victim instanceof Player) return damageBeforeBonus;
        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        if (guildData == null) return damageBeforeBonus;
        Guild guild = guildData.getData();
        if (guild.getGuildLevel().getGuildStage().getExp() < GuildStage.STAGE4.getExp()) return damageBeforeBonus;
        // todo: ensure any hard reference to stage has correct reward
        StageReward stageReward = StageReward.COMBAT_BONUS;
        int bonusDamage = (int) (damageBeforeBonus * stageReward.getBuffPercent());
        return damageBeforeBonus + bonusDamage;
    }
}
