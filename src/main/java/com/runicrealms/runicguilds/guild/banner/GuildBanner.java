package com.runicrealms.runicguilds.guild.banner;

import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.model.GuildInfo;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.springframework.data.annotation.Transient;

import java.util.UUID;

public class GuildBanner {
    @Transient // Doesn't get persisted
    private ItemStack banner = new ItemStack(Material.STONE);
    private String serializedBanner = "";

    @SuppressWarnings("unused")
    public GuildBanner() {
        // Default constructor for Spring
    }

    public GuildBanner(UUID guildUUID) {
        this.banner = this.makeDefaultBanner(guildUUID);
    }

//    public GuildBanner(UUID guildUUID, ItemStack itemStack) {
////        this.guildUUID = guildUUID;
//
////        if (!itemStack.hasItemMeta() || !(itemStack.getItemMeta() instanceof BannerMeta)) {
////            this.banner = this.makeDefaultBanner(guildUUID);
////        } else {
////            this.banner = itemStack;
////        }
//    }

    public ItemStack getBannerItem() {
        return this.banner;
    }

    public String getSerializedBanner() {
        return serializedBanner;
    }

    public void setSerializedBanner(String serializedBanner) {
        this.serializedBanner = serializedBanner;
    }

    private ItemStack makeDefaultBanner(UUID guildUUID) {
        ItemStack item = new ItemStack(Material.WHITE_BANNER, 1);
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

    public void setBanner(UUID guildUUID, Material material, BannerMeta meta) {
        this.banner.setType(material);
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(guildUUID);
        if (guildInfo != null) {
            meta.setDisplayName(ColorUtil.format("&r&6" + ChatColor.stripColor(guildInfo.getName() + "'s Banner")));
        }
        this.banner.setItemMeta(meta);
    }
}