package com.runicrealms.runicguilds.guilds;

import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class GuildBanner {
    private final ItemStack banner;

    public GuildBanner() {
        ItemStack item = new ItemStack(Material.WHITE_BANNER, 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ColorUtil.format("&r&6{Guild name}'s Banner"));
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

        item.setItemMeta(meta);
        this.banner = item;
    }

    public GuildBanner(ItemStack itemStack) {
        this.banner = itemStack;
    }

    public void setBanner(Material material, BannerMeta meta) {
        this.banner.setType(material);
        meta.setDisplayName(ColorUtil.format("&r&6{Guild name}'s Banner"));
        this.banner.setItemMeta(meta);
    }

    public ItemStack getBannerItem() {
        return this.banner;
    }
}