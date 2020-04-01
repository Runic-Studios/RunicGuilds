package com.runicrealms.runicguilds.config;

import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.guilds.Guild;
import org.bukkit.Bukkit;

import java.util.*;

public class TaskSavingQueue {

    private volatile static LinkedList<Guild> queue = new LinkedList<Guild>();

    public static void add(Guild guild) {
        if (queue.contains(guild)) {
            return;
        }
        for (Guild queueGuild : queue) {
            if (queueGuild.getGuildPrefix().equalsIgnoreCase(guild.getGuildPrefix())) {
                return;
            }
        }
        queue.add(guild);
    }

    public static void scheduleTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Plugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                int max = (int) Math.ceil(queue.size() / 2);
                for (int i = 0; i < max; i++) {
                    GuildUtil.saveGuildToFile(queue.pop());
                }
            }
        }, 0L, 20L * 15L);
    }

    public static void emptyQueue() {
        for (int i = 0; i < queue.size(); i++) {
            GuildUtil.saveGuildToFile(queue.pop());
        }
    }

}
