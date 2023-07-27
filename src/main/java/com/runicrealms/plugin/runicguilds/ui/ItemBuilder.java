package com.runicrealms.plugin.runicguilds.ui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private ItemStack item;

    public ItemBuilder(Material material, Integer count, String name, String... lore) {
        this.item = new ItemStack(material, count);
        if (name != null) {
            ItemMeta meta = this.item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            this.item.setItemMeta(meta);
        }
        if (lore != null) {
            ItemMeta meta = this.item.getItemMeta();
            List<String> lines = new ArrayList<String>();
            for (String line : lore) {
                lines.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(lines);
            this.item.setItemMeta(meta);
        }
    }

    public ItemStack getItem() {
        return this.item;
    }

}
