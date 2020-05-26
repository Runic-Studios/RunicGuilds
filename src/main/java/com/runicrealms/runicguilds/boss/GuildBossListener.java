package com.runicrealms.runicguilds.boss;

import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.api.RunicGuildsAPI;
import com.runicrealms.runicrestart.api.ServerShutdownEvent;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.UUID;

public class GuildBossListener implements Listener {

    private double bossHealth = 0;
    private LivingEntity currentBoss;
    private HashMap<UUID, Integer> bossKillers = new HashMap<>();

    @EventHandler
    public void onGuildBossEvent(GuildBossSpawnEvent e) {
        currentBoss = e.getGuildBoss();
        bossHealth = currentBoss.getMaxHealth();
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
        if (!currentBoss.equals(e.getEntity())) return;
        Player pl = (Player) e.getKiller();
        if (RunicGuildsAPI.getGuild(pl.getUniqueId()) != null) {
            // todo: sounds, fireworks
            pl.sendTitle(ChatColor.GREEN + "Guild Boss Slain!",
                    ChatColor.GREEN + "You've earned an extra " +
                            ChatColor.YELLOW + Plugin.getGuildBossManager().getKillPoints() +
                            ChatColor.GREEN + " points!",
                    20, 100, 20);
            RunicGuildsAPI.addPlayerScore(pl.getUniqueId(), Plugin.getGuildBossManager().getKillPoints()); // todo: well, add the total then dist. evenly among party
        }
    }

    /*
    Remove active boss on server shutdown
     */
    @EventHandler
    public void onServerShutdown(ServerShutdownEvent e) {
        currentBoss.remove();
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
