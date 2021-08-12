package com.runicrealms.runicguilds.listeners;

import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.runicguilds.data.GuildData;
import com.runicrealms.runicguilds.data.GuildUtil;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildStage;
import com.runicrealms.runicguilds.util.GuildRewardUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class GuildRewardDamageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpellDamage(SpellDamageEvent e) {
        e.setAmount(guildCombatBonus(e.getPlayer(), e.getVictim(), e.getAmount()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWeaponDamage(WeaponDamageEvent e) {
        e.setAmount(guildCombatBonus(e.getPlayer(), e.getVictim(), e.getAmount()));
    }

    /**
     * Applies combat experience bonus if the player is in a guild of the right stage. Only applies to monsters
     *
     * @param player to check for bonus
     * @param victim entity being attacked
     * @param damageBeforeBonus event damage
     */
    private int guildCombatBonus(Player player, LivingEntity victim, int damageBeforeBonus) {
        if (victim instanceof Player) return damageBeforeBonus;
        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        if (guildData == null) return damageBeforeBonus;
        Guild guild = guildData.getData();
        if (guild.getGuildLevel().getGuildStage().getExp() < GuildStage.STAGE6.getExp()) return damageBeforeBonus;
        int bonusDamage = (int) (damageBeforeBonus * GuildRewardUtil.getGuildCombatBuff());
        return damageBeforeBonus + bonusDamage;
    }
}
