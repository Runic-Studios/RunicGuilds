package com.runicrealms.runicguilds.boss;

import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.UUID;

public class GuildBossListener implements Listener {

    private double bossHealth = 0;
    private HashMap<UUID, Integer> bossKillers = new HashMap<>();

    @EventHandler
    public void onGuildBossEvent(GuildBossSpawnEvent e) {
        LivingEntity boss = e.getGuildBoss();
        bossHealth = boss.getMaxHealth();
    }

    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        trackBossDamage(e.getPlayer(), e.getEntity(), e.getAmount());
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {
        trackBossDamage(e.getPlayer(), e.getEntity(), e.getAmount());
    }

    @EventHandler
    public void onGuildBossDeath(MythicMobDeathEvent e) {
        // todo: distribute first 100 points
        // todo: extra 20 points
    }

    /**
     * Keeps track of damage during boss fight to determine guild point distribution
     */
    // todo: finish
    private void trackBossDamage(Player pl, Entity en, int eventAmt) {

        if (!MythicMobs.inst().getMobManager().getActiveMob(en.getUniqueId()).isPresent()) return;
        ActiveMob am = MythicMobs.inst().getMobManager().getActiveMob(en.getUniqueId()).get();

        UUID plID = pl.getUniqueId();
        UUID bossID = en.getUniqueId();
         // get current damage
        // put on hashmap w/ current damage + new amount
    }

}
