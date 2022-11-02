package com.runicrealms.runicguilds.guild;

import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class GuildBanner {
    private final Guild guild;
    private final ItemStack banner;

    public GuildBanner(Guild guild) {
        this.guild = guild;
        this.banner = this.makeDefaultBanner();
    }

    public GuildBanner(Guild guild, ItemStack itemStack) {
        this.guild = guild;

        if (!itemStack.hasItemMeta() || !(itemStack.getItemMeta() instanceof BannerMeta)) {
            this.banner = this.makeDefaultBanner();
        } else {
            this.banner = itemStack;
        }
    }

    public Guild getGuild() {
        return this.guild;
    }

    public void setBanner(Material material, BannerMeta meta) {
        this.banner.setType(material);
        meta.setDisplayName(ColorUtil.format("&r&6" + ChatColor.stripColor(this.guild.getGuildName() + "'s Banner")));
        this.banner.setItemMeta(meta);
    }

    public ItemStack getBannerItem() {
        return this.banner;
    }

    private ItemStack makeDefaultBanner() {
        ItemStack item = new ItemStack(Material.WHITE_BANNER, 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ColorUtil.format("&r&6" + ChatColor.stripColor(this.guild.getGuildName() + "'s Banner")));
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

        item.setItemMeta(meta);
        return item;
    }
}