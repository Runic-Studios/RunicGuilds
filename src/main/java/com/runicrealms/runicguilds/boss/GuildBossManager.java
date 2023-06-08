package com.runicrealms.runicguilds.boss;

import com.runicrealms.plugin.common.RunicCommon;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.RunicDamageEvent;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.model.GuildInfo;
import com.runicrealms.runicguilds.model.MemberData;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class GuildBossManager implements Listener {

    private final Map<UUID, Pair<Map<UUID, Integer>, Double>> bossDamage = new HashMap<>(); // first uuid is boss entity ID, map is of player -> damage, last integer is boss max health
    private Set<String> bosses;

    public GuildBossManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicGuilds.getInstance());
        Bukkit.getScheduler().runTaskAsynchronously(RunicGuilds.getInstance(), () -> {
            FileConfiguration config = RunicCommon.getConfigAPI().getYamlConfigFromFile("guild-bosses.yml", RunicGuilds.getInstance().getDataFolder());
            bosses.addAll(config.getStringList("guild-bosses"));
        });

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamageBoss(RunicDamageEvent event) {
        if (event.isCancelled()) return;
        Player player = null;
        if (event instanceof MagicDamageEvent magicDamageEvent) {
            player = magicDamageEvent.getPlayer();
        } else if (event instanceof PhysicalDamageEvent physicalDamageEvent) {
            player = physicalDamageEvent.getPlayer();
        }
        if (player == null) return;
        if (bossDamage.containsKey(event.getVictim().getUniqueId())) {
            Map<UUID, Integer> damage = bossDamage.get(event.getVictim().getUniqueId()).first;
            if (!damage.containsKey(player.getUniqueId())) {
                damage.put(player.getUniqueId(), 0);
            }
            damage.put(player.getUniqueId(), damage.get(player.getUniqueId()) + event.getAmount());
        } else {
            if (!MythicMobs.inst().getMobManager().isActiveMob(event.getVictim().getUniqueId())) return;
            Optional<ActiveMob> optMob = MythicMobs.inst().getMobManager().getActiveMob(event.getVictim().getUniqueId());
            if (optMob.isPresent()) {
                ActiveMob activeMob = optMob.get();
                Map<UUID, Integer> damage = new HashMap<>();
                damage.put(player.getUniqueId(), event.getAmount());

                bossDamage.put(event.getVictim().getUniqueId(), new Pair<>(damage, activeMob.getEntity().getMaxHealth()));
            }
        }
    }

    public void handleBossDeath(UUID bossID, int guildScore) {
        Pair<Map<UUID, Integer>, Double> pair = bossDamage.get(bossID);
        if (pair == null) return;
        Map<UUID, Integer> damageValues = pair.first;
        Double maxHealth = pair.second;
        // 75% of guild score is distributed according to how much damage each player did, 25% is split evenly for participation
        int damageScoreTotal = (int) Math.round(guildScore * 0.75);
        Map<UUID, Integer> damageScores = new HashMap<>();
        for (UUID damager : damageValues.keySet()) {
            double percentDamage = damageValues.get(damager) / maxHealth;
            damageScores.put(damager, (int) Math.round(damageScoreTotal * percentDamage));
        }
        int participationScoreTotal = (int) Math.round(guildScore * 0.25);
        int participationScoreForEach = (int) Math.round(participationScoreTotal / ((double) damageValues.size()));
        for (UUID damager : damageValues.keySet()) {
            damageScores.put(damager, damageScores.get(damager) + participationScoreForEach);
        }
        // Distribute guild score
        Bukkit.getScheduler().runTaskAsynchronously(RunicGuilds.getInstance(), () -> {
            for (UUID damager : damageScores.keySet()) {
                GuildInfo info = RunicGuilds.getDataAPI().getGuildInfo(Bukkit.getOfflinePlayer(damager));
                MemberData data = RunicGuilds.getDataAPI().loadMemberData(info.getGuildUUID(), damager);
                RunicGuilds.getGuildsAPI().addGuildScore(info.getGuildUUID(), data, damageScores.get(damager));
            }
        });
    }

}
