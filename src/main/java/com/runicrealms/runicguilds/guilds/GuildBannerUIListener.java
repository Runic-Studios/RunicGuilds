package com.runicrealms.runicguilds.guilds;

import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.gui.GuildBannerUI;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GuildBannerUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getClickedInventory().getHolder() instanceof GuildBannerUI)) {
            return;
        }

        if (event.getCurrentItem() == null) {
            return;
        }

        event.setCancelled(true);

        GuildBannerUI ui = (GuildBannerUI) event.getClickedInventory().getHolder();
        BannerMeta meta = (BannerMeta) ui.getDummyBanner().getItemMeta();
        ItemStack item = event.getCurrentItem();
        ItemMeta itemMeta = item.getItemMeta();
        Material material = item.getType();

        if (material == Material.GREEN_STAINED_GLASS_PANE) {
            if (ui.getPage() == 1 && ui.getSelectedColor() != null && ui.getChosenColor() == null && ui.getChosenPattern() == null) {
                ui.setSelectedColor(null);
                ui.openPatternMenu();
            } else if (ui.getPage() == 1 && ui.getSelectedColor() != null && ui.getChosenColor() == null && ui.getChosenPattern() != null) {
                ui.setChosenColor(ui.getSelectedColor());
                meta.removePattern(meta.getPatterns().size() - 1);
                meta.addPattern(new Pattern(ui.getChosenColor(), ui.getChosenPattern()));
                ui.setSelectedColor(null);
                ui.setChosenColor(null);
                ui.setChosenPattern(null);
                ui.openPatternMenu();
            } else if (ui.getPage() == 2 && ui.getSelectedColor() == null && ui.getSelectedPattern() != null && ui.getChosenPattern() == null) {
                ui.setChosenPattern(ui.getSelectedPattern());
                ui.setSelectedPattern(null);
                ui.openColorMenu();
            }
            return;
        }

        if (itemMeta.getPersistentDataContainer().has(ui.getKey(), PersistentDataType.STRING) && this.isConcrete(ui, item)) {
            DyeColor color = DyeColor.valueOf(itemMeta.getPersistentDataContainer().get(ui.getKey(), PersistentDataType.STRING));
            if (ui.getChosenPattern() == null) {
                ui.getDummyBanner().setType(Material.valueOf(color.name() + "_BANNER"));
            } else {
                meta.removePattern(meta.getPatterns().size() - 1);
                meta.addPattern(new Pattern(color, ui.getChosenPattern()));
                ui.getDummyBanner().setItemMeta(meta);
            }
            ui.setSelectedColor(color);
            ui.openColorMenu();
            return;
        }

        if (material == Material.PAINTING) {
            String name = item.getItemMeta().getDisplayName();
            String[] stitch = name.split(" ");
            if (stitch.length == 2) {
                name = (stitch[0]).toUpperCase();
            } else if (stitch.length == 3) {
                name = (stitch[0] + "_" + stitch[1]).toUpperCase();
            } else if (stitch.length == 4) {
                name = (stitch[0] + "_" + stitch[1] + "_" + stitch[2]).toUpperCase();
            }

            name = ChatColor.stripColor(name);

            if (name.equalsIgnoreCase("STRIPE_DOWN_RIGHT")) {
                name = "STRIPE_DOWNRIGHT";
            } else if (name.equals("STRIPE_DOWN_LEFT")) {
                name = "STRIPE_DOWNLEFT";
            }

            PatternType type = PatternType.valueOf(name);

            if (!meta.getPatterns().isEmpty() && ui.getSelectedPattern() != null) {
                meta.removePattern(meta.getPatterns().size() - 1);
            }

            if (ui.getDummyBanner().getType() == Material.BLACK_BANNER) {
                meta.addPattern(new Pattern(DyeColor.WHITE, type));
            } else {
                meta.addPattern(new Pattern(DyeColor.BLACK, type));
            }
            ui.getDummyBanner().setItemMeta(meta);
            ui.setSelectedPattern(type);
            ui.openPatternMenu();
            return;
        }

        if (itemMeta.getPersistentDataContainer().has(ui.getKey(), PersistentDataType.STRING) && this.isBanner(ui, item)) {
            ItemStack dummy = ui.getDummyBanner();
            if (ui.getSelectedPattern() != null) {
                meta.removePattern(meta.getPatterns().size() - 1);
                dummy.setItemMeta(meta);
            }
            if (ui.getSelectedColor() != null && ui.getChosenColor() == null && ui.getChosenPattern() == null) {
                dummy.setType(Material.WHITE_BANNER);
            }
            if (ui.getSelectedColor() != null && ui.getChosenColor() == null && ui.getChosenPattern() != null) {
                Pattern pattern = meta.getPattern(meta.getPatterns().size() - 1);
                PatternType patternType = pattern.getPattern();
                meta.removePattern(meta.getPatterns().size() - 1);
                DyeColor dyeColor = (ui.getDummyBanner().getType() == Material.BLACK_BANNER) ? DyeColor.WHITE : DyeColor.BLACK; //if banner is black, dyecolor = white
                meta.addPattern(new Pattern(dyeColor, patternType));
                dummy.setItemMeta(meta);
            }
            ui.getBanner().setBanner(dummy.getType(), (BannerMeta) dummy.getItemMeta());
            //Guild guild = ui.getGuild();
            //save new guild in cache somewhere no clue
            event.getWhoClicked().sendMessage(ColorUtil.format("&r&aYour guild's banner has been updated!"));
            event.getWhoClicked().closeInventory();
        }

    }

    private boolean isConcrete(GuildBannerUI ui, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        String name = meta.getPersistentDataContainer().get(ui.getKey(), PersistentDataType.STRING);
        for (DyeColor color : DyeColor.values()) {
            if (color.name().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBanner(GuildBannerUI ui, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        String name = meta.getPersistentDataContainer().get(ui.getKey(), PersistentDataType.STRING);
        if (name.equals("banner")) {
            return true;
        }
        return false;
    }
}