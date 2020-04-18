package com.runicrealms.runicguilds.data;

import com.mongodb.client.model.UpdateOptions;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runicguilds.Plugin;
import org.bukkit.Bukkit;

import java.util.*;

public class TaskSavingQueue {

    private volatile static LinkedList<GuildData> queue = new LinkedList<GuildData>();

    public static void add(GuildData guildData) {
        if (queue.contains(guildData)) {
            return;
        }
        queue.add(guildData);
    }

    public static void scheduleTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Plugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < (int) Math.ceil(queue.size() * 0.50); i++) {
                    GuildData data = queue.pop();
                    data.save(data.getData());
                }
            }
        }, 0L, 20L * 15L);
    }

    public static void emptyQueue() {
        for (int i = 0; i < queue.size(); i++) {
            GuildData data = queue.pop();
            data.saveSync(data.getData());
        }
    }

}
