package com.runicrealms.runicguilds.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class GuildUtil {
    public static final String PREFIX = "&r&6&lGuilds »&r &e";

    /**
     * @param playerName
     * @return
     */
    public static UUID getOfflinePlayerUUID(String playerName) {
        @SuppressWarnings("deprecation")
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player.hasPlayedBefore()) {
            return player.getUniqueId();
        }
        return null;
    }

    /**
     * @param uuid
     * @return
     */
    public static String getOfflinePlayerName(UUID uuid) {
        @SuppressWarnings("deprecation")
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.hasPlayedBefore()) {
            return player.getName();
        }
        return null;
    }

}
