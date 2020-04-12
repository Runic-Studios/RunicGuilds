package com.runicrealms.runicguilds.data;

import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.runicguilds.Plugin;
import org.bukkit.Bukkit;

public class PlayerGuildDataUtil {

    public static void setGuildForPlayer(String prefix, String uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                PlayerMongoData mongoData = new PlayerMongoData(uuid);
                mongoData.set("guild", prefix);
                mongoData.save();
            }
        });
    }

}
