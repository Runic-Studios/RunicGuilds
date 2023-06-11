package com.runicrealms.runicguilds.util;

import com.keenant.tabbed.item.TextTabItem;
import com.keenant.tabbed.tablist.TableTabList;
import com.keenant.tabbed.util.Skins;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.model.GuildInfo;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
     * Updates the guild section of the player tab list
     *
     * @param player       to update
     * @param tableTabList the tab list (probably from a tab list update event)
     */
    public static void updateGuildTabColumn(Player player, TableTabList tableTabList) {
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        // Reset guild column
        for (int i = 0; i < 19; i++) {
            tableTabList.remove(1, i);
        }
        tableTabList.set(3, 0, new TextTabItem
                (ChatColor.GOLD + String.valueOf(ChatColor.BOLD) + "  Guild [0]", 0, Skins.getDot(ChatColor.GOLD)));
        if (guildInfo == null) return;
        // Load all the members and owner
        getMembersAndPopulate(tableTabList, guildInfo);
    }

    private static void getMembersAndPopulate(TableTabList tableTabList, GuildInfo guildInfo) {
        Set<UUID> members = guildInfo.getMembersUuids();
        Set<UUID> onlineMembers = members.stream()
                .filter(member -> Bukkit.getPlayer(member) != null)
                .collect(Collectors.toSet());
        tableTabList.set(3, 0, new TextTabItem
                (ChatColor.GOLD + String.valueOf(ChatColor.BOLD) + "  Guild [" + onlineMembers.size() + "]", 0, Skins.getDot(ChatColor.GOLD)));
        // Reset guild column
        for (int i = 1; i < 19; i++) {
            tableTabList.remove(1, i);
        }
        int j = 0;
        for (UUID guildMember : onlineMembers) {
            if (j > 19) break;
            Player playerMember = Bukkit.getPlayer(guildMember);
            if (playerMember == null) continue; // Insurance
            tableTabList.set(1, j + 1, new TextTabItem
                    (
                            playerMember.getName(),
                            playerMember.getPing(),
                            Skins.getPlayer(playerMember)
                    ));
            j++;
        }
    }

}
