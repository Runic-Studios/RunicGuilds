package com.runicrealms.runicguilds.order;

import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.order.config.OrderConfigLoader;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;

/**
 * Resets the work order every week
 */
public class WorkOrderManager {
    private static final int RESET_CHECK_TIME = 180; // Seconds
    private static final String CURRENT_WORK_ORDER_KEY = "currentWorkOrderName";
    private static final String RESET_TIMESTAMP_KEY = "nextResetTimestamp";
    private final WorkOrder currentWorkOrder;
    File configFile;
    OrderConfigLoader loader;

    public WorkOrderManager() throws IOException, InvalidConfigurationException {
        configFile = new File(RunicGuilds.getInstance().getDataFolder(), "orders.yml");
        loader = new OrderConfigLoader(configFile);
        currentWorkOrder = initializeWorkOrder();
        scheduleResetTask();
    }

    /**
     * Grabs the current network-wide work order (if it exists), or picks a new one
     *
     * @return the work order object that is live
     */
    private WorkOrder initializeWorkOrder() {
        String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
        String currentWorkOrderName;
        String nextResetTimestamp;
        // Check Redis for the work order key
        try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
            currentWorkOrderName = jedis.get(database + ":" + CURRENT_WORK_ORDER_KEY);
            nextResetTimestamp = jedis.get(database + ":" + RESET_TIMESTAMP_KEY);
        }
        ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        if (currentWorkOrderName != null && nextResetTimestamp != null) {
            ZonedDateTime nextReset = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(nextResetTimestamp)), ZoneId.systemDefault());
            if (now.compareTo(nextReset) < 0) {
                // Load current work order from name if current date time is still before next reset
                return loader.loadOrder(CURRENT_WORK_ORDER_KEY);
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
                try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
                    ZonedDateTime nextReset = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(jedis.get(RESET_TIMESTAMP_KEY))), ZoneId.systemDefault());
                    if (now.compareTo(nextReset) >= 0) {
                        resetGlobalWorkOrder();
                    }
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
        // After resetting work order, update the values in Jedis
        try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
            jedis.set(CURRENT_WORK_ORDER_KEY, currentWorkOrder.getName());
            jedis.set(RESET_TIMESTAMP_KEY, String.valueOf(calculateNextReset().toInstant().toEpochMilli()));
            jedis.expire(CURRENT_WORK_ORDER_KEY, 1_209_600);
            jedis.expire(RESET_TIMESTAMP_KEY, 1_209_600);
        }
        // Reset the progress of each guild
        RunicGuilds.getDataAPI().getGuildInfoMap().forEach((guildUUID, guildInfo) -> {
            RunicGuilds.getGuildWriteOperation().updateGuildData
                    (
                            guildUUID,
                            "orderMap",
                            new HashMap<>(),
                            () -> {

                            }
                    );
        });
        // Choose a new random order
        return loader.chooseRandomOrder();
    }

    /**
     * @return time remaining before the next order reset
     */
    private ZonedDateTime calculateNextReset() {
        ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        ZonedDateTime nextMidnightWednesday = now.with(DayOfWeek.WEDNESDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);
        if (now.compareTo(nextMidnightWednesday) > 0) {
            nextMidnightWednesday = nextMidnightWednesday.plusWeeks(1);
        }
        return nextMidnightWednesday;
    }

}
