package com.runicrealms.runicguilds.boss;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.parties.Party;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.api.RunicGuildsAPI;
import com.runicrealms.runicrestart.api.ServerShutdownEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;

public class GuildBossListener implements Listener {

    private double bossHealth = 0;
    private LivingEntity currentBoss;
    private final HashMap<Party, Integer> bossKillers = new HashMap<>();

    @EventHandler
    public void onGuildBossEvent(GuildBossSpawnEvent e) {
        currentBoss = e.getGuildBoss();
        bossHealth = currentBoss.getMaxHealth();
    }

    @EventHandler
    public void onGuildBossDeath(MythicMobDeathEvent e) {

        if (currentBoss == null) return;
        if (!currentBoss.equals(e.getEntity())) return;
        Player pl = (Player) e.getKiller();
        if (RunicGuildsAPI.getGuild(pl.getUniqueId()) != null
                && RunicCore.getPartyManager().getPlayerParty(pl) != null) {

            pl.getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.5f, 1.0f);
            pl.sendTitle(ChatColor.GREEN + "Guild Boss Slain!",
                    ChatColor.GREEN + "Your party has earned an extra " +
                            ChatColor.YELLOW + Plugin.getGuildBossManager().getKillPoints() +
                            ChatColor.GREEN + " points!",
                    20, 100, 20);
            // distribute the extra 20 points
            for (Player mem : RunicCore.getPartyManager().getPlayerParty(pl).getMembersWithLeader()) {
                RunicGuildsAPI.addPlayerScore(mem.getUniqueId(),
                        Plugin.getGuildBossManager().getKillPoints() / RunicCore.getPartyManager().getPlayerParty(pl).getSize());
            }

            // distribute remaining points
            distributePoints();
        }
    }

    /*
    Remove active boss on server shutdown
     */
    @EventHandler
    public void onServerShutdown(ServerShutdownEvent e) {
        currentBoss.remove();
    }

    /*
    Track the current damage of each player on the bossKillers HashMap
     */
    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        trackPoints(e.getPlayer(), e.getEntity(), e.getAmount());
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {
        trackPoints(e.getPlayer(), e.getEntity(), e.getAmount());
    }

    /*
    Store total damage of each party to boss for distribution
     */
    private void trackPoints(Player pl, Entity en, int amount) {
        if (currentBoss == null) return;
        if (!en.equals(currentBoss)) return;
        if (RunicCore.getPartyManager().getPlayerParty(pl) == null) return;
        int current = 0;
        if (bossKillers.get(RunicCore.getPartyManager().getPlayerParty(pl)) != null) {
            current = bossKillers.get(RunicCore.getPartyManager().getPlayerParty(pl));
        }
        bossKillers.put(RunicCore.getPartyManager().getPlayerParty(pl), current + amount);
    }

    /*
    Dishes out boss kill points evenly among party
     */
    private void distributePoints() {
        for (Party party : bossKillers.keySet()) {
            double amount = bossKillers.get(party);
            double percent = amount / bossHealth;
            double points = percent * 100;
            for (Player mem : party.getMembersWithLeader()) {
                RunicGuildsAPI.addPlayerScore(mem.getUniqueId(), (int) points / party.getSize());
                mem.sendMessage(ChatColor.GREEN + "You have earned " +
                        ChatColor.YELLOW + (int) points / party.getSize() +
                        ChatColor.GREEN + " guild points!");
            }
        }
        bossKillers.clear();
    }

}
