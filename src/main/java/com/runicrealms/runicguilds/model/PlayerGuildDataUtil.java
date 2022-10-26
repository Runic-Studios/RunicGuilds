package com.runicrealms.runicguilds.model;

import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.runicguilds.Plugin;
import org.bukkit.Bukkit;

public class PlayerGuildDataUtil {

    public static void setGuildForPlayer(String name, String uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                PlayerMongoData mongoData = new PlayerMongoData(uuid);
                mongoData.set("guild", name);
                mongoData.save();
            }
        });
    }

}
