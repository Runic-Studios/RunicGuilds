package com.runicrealms.runicguilds.util;

import com.keenant.tabbed.item.TextTabItem;
import com.keenant.tabbed.tablist.TableTabList;
import com.keenant.tabbed.util.Skins;
import com.runicrealms.libs.taskchain.TaskChain;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.model.GuildInfo;
import com.runicrealms.runicguilds.model.MemberData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import redis.clients.jedis.Jedis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
     * @param player      the item represents
     * @param name        of the player
     * @param description of this itemStack
     * @return an ItemStack to display in the ui menu
     */
    public static ItemStack guildMemberItem(Player player, String name, String description) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        SkullMeta skullMeta = (SkullMeta) meta;
        skullMeta.setOwningPlayer(player);

        ArrayList<String> lore = new ArrayList<>();
        meta.setDisplayName(ColorUtil.format(name));
        String[] desc = description.split("\n");
        for (String line : desc) {
            lore.add(ColorUtil.format(line));
        }
        meta.setLore(lore);
        ((Damageable) meta).setDamage(5);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
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
        Bukkit.broadcastMessage("updating tab");
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (guildInfo == null) {
            tableTabList.set(1, 0, new TextTabItem
                    (ChatColor.GOLD + "" + ChatColor.BOLD + "  Guild [0]", 0, Skins.getDot(ChatColor.GOLD)));
        } else {
            // Load all the members and owner
//            Bukkit.broadcastMessage("guild info found for tab");
            try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
                getMembersAndPopulate(tableTabList, guildInfo, jedis);
            }
        }
    }

    private static void getMembersAndPopulate(TableTabList tableTabList, GuildInfo guildInfo, Jedis jedis) {
        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> RunicGuilds.getDataAPI().loadGuildMembers(guildInfo.getGuildUUID(), jedis))
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicGuilds failed to load member data!")
                .syncLast(memberDataMap -> {
                    List<UUID> onlineMembers = memberDataMap.values().stream().map(MemberData::getUuid)
                            .filter(uuid -> Bukkit.getPlayer(uuid) != null).toList();
                    tableTabList.set(1, 0, new TextTabItem
                            (ChatColor.GOLD + "" + ChatColor.BOLD + "  Guild [" + onlineMembers.size() + "]", 0, Skins.getDot(ChatColor.GOLD)));
                    int j = 0;
                    for (UUID guildMember : onlineMembers) {
                        if (j > 19) break;
                        Player playerMember = Bukkit.getPlayer(guildMember);
                        if (playerMember == null) continue; // Insurance
                        tableTabList.set(1, j + 1, new TextTabItem
                                (
                                        playerMember.getName(),
                                        RunicCore.getTabAPI().getPing(playerMember),
                                        Skins.getPlayer(playerMember)
                                ));
                        j++;
                    }
                })
                .execute();
    }

}
