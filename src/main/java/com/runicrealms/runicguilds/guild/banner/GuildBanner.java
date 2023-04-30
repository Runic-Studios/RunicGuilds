package com.runicrealms.runicguilds.guild.banner;

import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.model.GuildInfo;
import com.runicrealms.runicguilds.model.GuildUUID;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.springframework.data.annotation.Transient;

public class GuildBanner {
    //    private final GuildUUID guildUUID;
    @Transient // doesn't get persisted
    private ItemStack banner = new ItemStack(Material.STONE); // todo, encode / decode

    @SuppressWarnings("unused")
    public GuildBanner() {
        // Default constructor for Spring
    }

    public GuildBanner(GuildUUID guildUUID) {
//        this.guildUUID = guildUUID;
        this.banner = this.makeDefaultBanner(guildUUID);
    }

    public GuildBanner(GuildUUID guildUUID, ItemStack itemStack) {
//        this.guildUUID = guildUUID;

//        if (!itemStack.hasItemMeta() || !(itemStack.getItemMeta() instanceof BannerMeta)) {
//            this.banner = this.makeDefaultBanner(guildUUID);
//        } else {
//            this.banner = itemStack;
//        }
    }

    public ItemStack getBannerItem() {
        return this.banner;
    }

//    public GuildUUID guildUUID() {
//        return this.guildUUID;
//    }

    private ItemStack makeDefaultBanner(GuildUUID guildUUID) {
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

    public void setBanner(GuildUUID guildUUID, Material material, BannerMeta meta) {
        this.banner.setType(material);
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(guildUUID);
        if (guildInfo != null) {
            meta.setDisplayName(ColorUtil.format("&r&6" + ChatColor.stripColor(guildInfo.getName() + "'s Banner")));
        }
        this.banner.setItemMeta(meta);
    }
}