package com.runicrealms.runicguilds.guild.banner;

import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.model.GuildInfo;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class BannerUtil {

    /**
     * Creates a default BROWN banner for a guild
     *
     * @param guildUUID of the guild
     * @return a banner item stack
     */
    public static ItemStack makeDefaultBanner(UUID guildUUID) {
        ItemStack item = new ItemStack(Material.BROWN_BANNER, 1);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(guildUUID);
        if (guildInfo != null) {
            meta.setDisplayName(ColorUtil.format("&r&6" + ChatColor.stripColor(guildInfo.getName() + "'s Banner")));
        }
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Used in the banner editor
     *
     * @param guildUUID of the guild
     * @param material  of the banner
     * @param meta      of the banner's design
     * @return a banner item stack
     */
    public static ItemStack createUpdatedBanner(UUID guildUUID, Material material, BannerMeta meta) {
        ItemStack banner = makeDefaultBanner(guildUUID);
        banner.setType(material);
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(guildUUID);
        if (guildInfo != null) {
            meta.setDisplayName(ColorUtil.format("&r&6" + ChatColor.stripColor(guildInfo.getName() + "'s Banner")));
        }
        banner.setItemMeta(meta);
        return banner;
    }

    /**
     * Serializes a banner item to MongoDB
     *
     * @param itemStack of the banner
     * @return a string for storage
     */
    public static String serializeItemStack(ItemStack itemStack) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("item", itemStack);
        return config.saveToString();
    }

    /**
     * Retrieves the item stack from MongoDB
     *
     * @param itemString the serialized string
     * @return a banner item stack
     */
    public static ItemStack deserializeItemStack(String itemString) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(itemString);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        return config.getItemStack("item");
    }

}
