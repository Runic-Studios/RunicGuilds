package com.runicrealms.runicguilds.boss;

import com.runicrealms.runicguilds.Plugin;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.spawning.spawners.MythicSpawner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("deprecation")
public class GuildBossManager {

    private static final int SPAWN_TIMER = 90; // minutes (90?)
    private static final int KILL_POINTS = 20;

    public GuildBossManager() {

        // 30 min before
        Bukkit.getScheduler().scheduleAsyncDelayedTask(Plugin.getInstance(),
                () -> Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "A guild boss will spawn in 30 minutes!"), (SPAWN_TIMER-30)*60*20L);

        // 15 min before
        Bukkit.getScheduler().scheduleAsyncDelayedTask(Plugin.getInstance(),
                () -> Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "A guild boss will spawn in 15 minutes!"), (SPAWN_TIMER-15)*60*20L);

        // 5 min before
        Bukkit.getScheduler().scheduleAsyncDelayedTask(Plugin.getInstance(),
                () -> Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "A guild boss will spawn in 5 minutes!"), (SPAWN_TIMER-5)*60*20L);

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    GuildBossSpawnEvent event = new GuildBossSpawnEvent(randomGuildBoss());
                    Bukkit.getPluginManager().callEvent(event);
                } catch (Exception e) {
                    e.printStackTrace();
                    Plugin.getInstance().getLogger().info("Error: no guild boss found for mythic spawner.");
                }
            }
        }.runTaskLater(Plugin.getInstance(), SPAWN_TIMER*60*20L);
    }

    private LivingEntity randomGuildBoss() {
        MythicSpawner bossSpawner;
//        if (Math.random() < 0.5) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "A guild boss has spawned in the desert!");
            bossSpawner = MythicMobs.inst().getSpawnerManager().getSpawnerByName("GuildBoss1");
//        } else {
//            Bukkit.broadcastMessage(ChatColor.GOLD + "A guild boss has spawned in the volcano!");
//            bossSpawner = MythicMobs.inst().getSpawnerManager().getSpawnerByName("GuildBoss2");
//        }
        bossSpawner.ActivateSpawner();
        return (LivingEntity) Bukkit.getEntity(bossSpawner.getAssociatedMobs().stream().findFirst().get());
    }

    public int getKillPoints() {
        return KILL_POINTS;
    }
}
