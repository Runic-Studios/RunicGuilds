package com.runicrealms.runicguilds.order;

import com.runicrealms.plugin.api.NpcClickEvent;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.order.config.OrderConfigLoader;
import com.runicrealms.runicguilds.order.ui.WorkOrderUI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Resets the work order every week
 */
public class WorkOrderManager implements Listener {
    private static final int RESET_CHECK_TIME = 180; // Seconds
    private static final Set<Integer> GUILD_FOREMAN_IDS = new HashSet<>();
    private static final String CURRENT_WORK_ORDER_KEY = "order:currentWorkOrderName";
    private static final String RESET_TIMESTAMP_KEY = "order:nextResetTimestamp";
    File configFile;
    OrderConfigLoader loader;
    private WorkOrder currentWorkOrder;

    public WorkOrderManager() throws IOException, InvalidConfigurationException {
        GUILD_FOREMAN_IDS.add(785);
        configFile = new File(RunicGuilds.getInstance().getDataFolder(), "orders.yml");
        loader = new OrderConfigLoader(configFile);
        Bukkit.getPluginManager().registerEvents(this, RunicGuilds.getInstance());
        // Delayed task to initialize work orders
        Bukkit.getScheduler().runTaskLater(RunicGuilds.getInstance(), () -> {
            currentWorkOrder = initializeWorkOrder();
            scheduleResetTask();
        }, 10 * 20L);
    }

    public WorkOrder getCurrentWorkOrder() {
        return currentWorkOrder;
    }

    @EventHandler
    public void onNPCInteract(NpcClickEvent event) {
        if (!GUILD_FOREMAN_IDS.contains(event.getNpc().getId())) return;
        if (currentWorkOrder == null) {
            Bukkit.getLogger().warning("A player tried to access the guild foreman, but the current order is null.");
            return; // Wait for it to load
        }
        event.getPlayer().openInventory(new WorkOrderUI(event.getPlayer()).getInventory());
    }

    /**
     * Grabs the current network-wide work order (if it exists), or picks a new one
     *
     * @return the work order object that is live
     */
    private WorkOrder initializeWorkOrder() {
        Bukkit.getLogger().info("Initializing guild work orders!");
        String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
        String currentWorkOrderName;
        String nextResetTimestamp;
        // Check Redis for the work order key
        try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
            ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
            if (jedis.exists(database + ":" + CURRENT_WORK_ORDER_KEY) && jedis.exists(database + ":" + RESET_TIMESTAMP_KEY)) {
                currentWorkOrderName = jedis.get(database + ":" + CURRENT_WORK_ORDER_KEY);
                nextResetTimestamp = jedis.get(database + ":" + RESET_TIMESTAMP_KEY);
                ZonedDateTime nextReset = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(nextResetTimestamp)), ZoneId.systemDefault());
                if (now.compareTo(nextReset) < 0) {
                    // Load current work order from name if current date time is still before next reset
                    Bukkit.getLogger().info("Loading currently existing work order.");
                    return loader.loadOrder(currentWorkOrderName);
                }
            }
        }

        // Reset work order if no current order exists, or it's time for a reset
        return resetGlobalWorkOrder();
    }

    /**
     * Runs a task every few minutes to check if the global work order should be reset
     */
    private void scheduleResetTask() {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
                String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
                try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
                    ZonedDateTime nextReset = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(jedis.get(database + ":" + RESET_TIMESTAMP_KEY))), ZoneId.systemDefault());
                    if (now.compareTo(nextReset) >= 0) {
                        resetGlobalWorkOrder();
                    }
                } catch (Exception ex) {
                    Bukkit.getLogger().warning("WorkOrderManager schedule reset task had a problem!");
                    ex.printStackTrace();
                }
            }
        };

        // Run the task every minute to check if it's time for a reset
        task.runTaskTimer(RunicGuilds.getInstance(), 0, RESET_CHECK_TIME * 20L);
    }

    /**
     * If it is not wednesday, and we need to reset the global work order, this:
     * - randomly picks a new order key
     * - sets that value in Redis
     * - resets progress for all guilds
     *
     * @return the new global work order
     */
    private WorkOrder resetGlobalWorkOrder() {
        String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
        WorkOrder workOrder = loader.chooseRandomOrder();
        // After resetting work order, update the values in Jedis
        try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
            jedis.set(database + ":" + CURRENT_WORK_ORDER_KEY, workOrder.getOrderId());
            jedis.set(database + ":" + RESET_TIMESTAMP_KEY, String.valueOf(calculateNextReset().toInstant().toEpochMilli()));
            jedis.expire(database + ":" + CURRENT_WORK_ORDER_KEY, 1_209_600); // 2 weeks to be safe
            jedis.expire(database + ":" + RESET_TIMESTAMP_KEY, 1_209_600);
        }
        // Reset the progress of each guild
        RunicGuilds.getDataAPI().getGuildInfoMap().forEach((guildUUID, guildInfo) -> RunicGuilds.getGuildWriteOperation().updateGuildData
                (
                        guildUUID,
                        "orderMap",
                        new HashMap<>(),
                        () -> {
                            // todo: send a message to the players here
                        }
                ));
        return workOrder;
    }

    /**
     * @return time remaining before the next order reset
     */
    private ZonedDateTime calculateNextReset() {
        ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        ZonedDateTime nextMidnightWednesday = now.with(DayOfWeek.WEDNESDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);

        // Debug logging
        Bukkit.getLogger().info("Now: " + now);
        Bukkit.getLogger().info("Next Midnight Wednesday (Before adjustment): " + nextMidnightWednesday);

        if (now.compareTo(nextMidnightWednesday) > 0) {
            nextMidnightWednesday = nextMidnightWednesday.plusWeeks(1);
        }

        // Debug logging
        Bukkit.getLogger().info("Next Midnight Wednesday (After adjustment): " + nextMidnightWednesday);

        return nextMidnightWednesday;
    }


}
