package com.runicrealms.runicguilds.boss;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.party.Party;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.api.RunicGuildsAPI;
import com.runicrealms.runicrestart.event.PreShutdownEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;

public class GuildBossListener implements Listener {

    private double bossHealth = 0;
    private double timeAtSpawn = 0;
    private LivingEntity currentBoss;
    private final HashMap<Party, Integer> bossKillers = new HashMap<>();

    @EventHandler
    public void onGuildBossEvent(GuildBossSpawnEvent event) {
        currentBoss = event.getGuildBoss();
        bossHealth = currentBoss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        timeAtSpawn = System.currentTimeMillis();
    }

    @EventHandler
    public void onGuildBossDeath(MythicMobDeathEvent event) {

        if (currentBoss == null) return;
        if (!currentBoss.equals(event.getEntity())) return;

        Bukkit.broadcastMessage
                (
                        ChatColor.GOLD + "" + ChatColor.BOLD + "Guild boss defeated in " +
                                (System.currentTimeMillis() - timeAtSpawn) / 100 + "s!"
                );

        Player player = (Player) event.getKiller();
        if (RunicGuildsAPI.getGuild(player.getUniqueId()) != null
                && RunicCore.getPartyManager().getPlayerParty(player) != null) {

            player.getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.5f, 1.0f);
            player.sendTitle(ChatColor.GREEN + "Guild Boss Slain!",
                    ChatColor.GREEN + "Your party has earned an extra " +
                            ChatColor.YELLOW + Plugin.getGuildBossManager().getKillPoints() +
                            ChatColor.GREEN + " points!",
                    20, 100, 20);
            // distribute the extra 20 points
            for (Player mem : RunicCore.getPartyManager().getPlayerParty(player).getMembersWithLeader()) {
                RunicGuildsAPI.addPlayerScore(mem.getUniqueId(),
                        Plugin.getGuildBossManager().getKillPoints() / RunicCore.getPartyManager().getPlayerParty(player).getSize());
            }

            // distribute remaining points
            distributePoints();
        }
    }

    /**
    Remove active boss on server shutdown
     */
    @EventHandler
    public void onServerShutdown(PreShutdownEvent event) {
        if (currentBoss != null)
            currentBoss.remove();
    }

    /**
    Track the current damage each player dealt on the bossKillers HashMap
     */
    @EventHandler
    public void onWeaponDamage(PhysicalDamageEvent event) {
        trackPoints(event.getPlayer(), event.getVictim(), event.getAmount());
    }

    @EventHandler
    public void onSpellDamage(MagicDamageEvent event) {
        trackPoints(event.getPlayer(), event.getVictim(), event.getAmount());
    }

    /*
    Store total damage each party (as a single unit) has done to boss for GP distribution
     */
    private void trackPoints(Player player, Entity entity, int amount) {
        if (currentBoss == null) return;
        if (!entity.equals(currentBoss)) return;
        if (RunicCore.getPartyManager().getPlayerParty(player) == null) return;
        int current = 0;
        if (bossKillers.get(RunicCore.getPartyManager().getPlayerParty(player)) != null) {
            current = bossKillers.get(RunicCore.getPartyManager().getPlayerParty(player));
        }
        bossKillers.put(RunicCore.getPartyManager().getPlayerParty(player), current + amount);
    }

    /**
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
