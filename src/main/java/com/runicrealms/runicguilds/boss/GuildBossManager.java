package com.runicrealms.runicguilds.boss;

import com.runicrealms.plugin.common.RunicCommon;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
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
            Optional<ActiveMob> optMob = MythicMobs.inst().getMobManager().getActiveMob(bossID);
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
        Pair<Map<UUID, Integer>, Double> pair = bossDamage.get(bossID);
        if (pair == null) return;
        Map<UUID, Integer> damageValues = pair.first;
        Double maxHealth = pair.second;
        // 75% of guild score is distributed according to how much damage each player did, 25% is split evenly for participation
        int damageScoreTotal = (int) Math.round(guildScore * 0.75);
        Map<UUID, Integer> damageScores = new HashMap<>();
        double participationScoreTotal = guildScore * 0.25;
        double participationScoreForEach = participationScoreTotal / ((double) damageValues.size());

        for (UUID damager : damageValues.keySet()) {
            double percentDamage = damageValues.get(damager) / maxHealth;
            damageScores.put(damager, (int) Math.round(damageScoreTotal * percentDamage + participationScoreForEach));
        }
        // Distribute guild score
        Bukkit.getScheduler().runTaskAsynchronously(RunicGuilds.getInstance(), () -> {
            for (UUID damager : damageScores.keySet()) {
                GuildInfo info = RunicGuilds.getDataAPI().getGuildInfo(Bukkit.getOfflinePlayer(damager));
                if (info == null) continue;
                MemberData data = RunicGuilds.getDataAPI().loadMemberData(info.getUUID(), damager);
                Bukkit.getScheduler().runTask(RunicGuilds.getInstance(), () -> {
                    RunicGuilds.getGuildsAPI().addGuildScore(info.getUUID(), data, damageScores.get(damager));
                    // TODO send "player gained guild score" to all online players
                });
            }
        });
        bossDamage.remove(bossID);
    }

}
