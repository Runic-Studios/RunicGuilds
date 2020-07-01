package com.runicrealms.runicguilds.data;

import com.runicrealms.runicguilds.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class TaskSavingQueue {

    private static final LinkedList<GuildData> queue = new LinkedList<>();
    private static BukkitTask queueTask;

    public static void add(GuildData guildData) {
        if (queue.contains(guildData)) {
            return;
        }
        queue.add(guildData);
    }

    public static void scheduleTask() {
        queueTask = Bukkit.getScheduler().runTaskTimerAsynchronously(Plugin.getInstance(), () -> {
            for (int i = 0; i < (int) Math.ceil(queue.size() * 0.50); i++) {
                GuildData data = queue.pop();
                data.save(data.getData(), true);
            }
        }, 0L, 20L * 15L);
    }

    public static void emptyQueue() {
        queueTask.cancel();
        for (int i = 0; i < queue.size(); i++) {
            GuildData data = queue.iterator().next();//queue.pop();
            data.save(data.getData(), false);
            queue.remove(data);
        }
    }

}
