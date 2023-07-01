package com.runicrealms.runicguilds.boss;

import com.runicrealms.plugin.common.RunicCommon;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.runicguilds.RunicGuilds;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class GuildBossManager implements Listener {

    private final Map<UUID, Pair<Map<UUID, Integer>, Double>> bossDamage = new HashMap<>(); // first uuid is boss entity ID, map is of player -> damage, last integer is boss max health
    private final Set<String> bosses = new HashSet<>();

    public GuildBossManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicGuilds.getInstance());
        Bukkit.getScheduler().runTaskAsynchronously(RunicGuilds.getInstance(), () -> {
            FileConfiguration config = RunicCommon.getConfigAPI().getYamlConfigFromFile("guild-bosses.yml", RunicGuilds.getInstance().getDataFolder());
            bosses.addAll(config.getStringList("guild-bosses"));
        });

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPhysicalDamageBoss(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        handleBossDamage(event.getPlayer(), event.getVictim().getUniqueId(), event.getAmount());
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onMagicDamageBoss(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        handleBossDamage(event.getPlayer(), event.getVictim().getUniqueId(), event.getAmount());
    }

    public void handleBossDamage(Player damager, UUID bossID, int damageAmount) {
        if (bossDamage.containsKey(bossID)) {
            Map<UUID, Integer> damage = bossDamage.get(bossID).first;
            if (!damage.containsKey(damager.getUniqueId())) {
                damage.put(damager.getUniqueId(), 0);
            }
            damage.put(damager.getUniqueId(), damage.get(damager.getUniqueId()) + damageAmount);
        } else {
            Optional<ActiveMob> optMob = MythicBukkit.inst().getMobManager().getActiveMob(bossID);
            if (optMob.isPresent()) {
                ActiveMob activeMob = optMob.get();
                if (!bosses.contains(activeMob.getMobType())) return;
                Map<UUID, Integer> damage = new HashMap<>();
                damage.put(damager.getUniqueId(), damageAmount);
                double maxHealth = activeMob.getEntity().getMaxHealth();
                bossDamage.put(bossID, new Pair<>(damage, maxHealth));
            }
        }
    }

    public void handleBossDeath(UUID bossID, int guildScore) {
        Map<UUID, Integer> damageScores = new HashMap<>();
        if (bossDamage.get(bossID).first.size() == 1) {
            UUID player = bossDamage.get(bossID).first.keySet().stream().findFirst().orElseThrow();
            damageScores.put(player, guildScore);
        } else {
            Pair<Map<UUID, Integer>, Double> pair = bossDamage.get(bossID);
            if (pair == null) return;
            Map<UUID, Integer> damageValues = pair.first;
            Double maxHealth = pair.second;
            // 75% of guild score is distributed according to how much damage each player did, 25% is split evenly for participation
            int damageScoreTotal = (int) Math.round(guildScore * 0.75);
            double participationScoreTotal = guildScore * 0.25;
            double participationScoreForEach = participationScoreTotal / ((double) damageValues.size());

            for (UUID damager : damageValues.keySet()) {
                double percentDamage = damageValues.get(damager) / maxHealth;
                damageScores.put(damager, (int) Math.round(damageScoreTotal * percentDamage + participationScoreForEach));
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(RunicGuilds.getInstance(), () -> RunicGuilds.getGuildsAPI().addBulkGuildScore(damageScores, true));
        bossDamage.remove(bossID);
    }

}
