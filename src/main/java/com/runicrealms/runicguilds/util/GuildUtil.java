package com.runicrealms.runicguilds.util;

import com.keenant.tabbed.item.TextTabItem;
import com.keenant.tabbed.tablist.TableTabList;
import com.keenant.tabbed.util.Skins;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.GuildMember;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class GuildUtil {
    public static final String PREFIX = "&r&6&lGuilds »&r &e";

    /**
     * Gets the uuid of the OfflinePlayer found using name
     *
     * @param playerName of the player
     * @return their uuid
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
     * Gets the name of the OfflinePlayer found using uuid
     *
     * @param uuid of the player
     * @return their last known name
     */
    public static String getOfflinePlayerName(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.hasPlayedBefore()) {
            return player.getName();
        }
        return null;
    }

    /**
     * Retrieve an ItemStack from a base64 string (should be loss-less)
     *
     * @param item the base64 string
     * @return an ItemStack to set as the guild banner
     */
    private static ItemStack deserializeItemStack(String item) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(item));
        try {
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            dataInput.close();
            return (ItemStack) dataInput.readObject();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * Convert a GuildBanner ItemStack into base64 for storage and retrieval (should be loss-less)
     *
     * @param item the item stack associated with the guild banner
     * @return a string for jedis / mongo storage
     */
    private static String serializeItemStack(ItemStack item) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * Updates the guild section of the tab for the given player
     *
     * @param player to update tab for
     */
    public static void updateGuildTabColumn(Player player) {
        TableTabList tableTabList = RunicCore.getTabAPI().getPlayerTabList(player);
        if (tableTabList == null) {
            return; // tab not setup yet
        }
        Guild guild = RunicGuilds.getGuildsAPI().getGuild(player.getUniqueId());
        if (guild == null) {
            tableTabList.set(1, 0, new TextTabItem
                    (ChatColor.GOLD + "" + ChatColor.BOLD + "  Guild [0]", 0, Skins.getDot(ChatColor.GOLD)));
        } else {
            tableTabList.set(1, 0, new TextTabItem
                    (ChatColor.GOLD + "" + ChatColor.BOLD + "  Guild [" + RunicGuilds.getGuildsAPI().getOnlineMembersWithOwner(guild).size() + "]", 0, Skins.getDot(ChatColor.GOLD))); // +1 for owner
            int j = 0;
            for (GuildMember guildMember : RunicGuilds.getGuildsAPI().getOnlineMembersWithOwner(guild)) {
                if (j > 19) break;
                Player playerMember = Bukkit.getPlayer(guildMember.getUUID());
                if (playerMember == null) continue;
                tableTabList.set(1, j + 1, new TextTabItem
                        (
                                playerMember.getName(),
                                RunicCore.getTabAPI().getPing(playerMember),
                                Skins.getPlayer(playerMember)
                        ));
                j++;
            }
        }
    }

}
