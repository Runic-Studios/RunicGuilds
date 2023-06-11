package com.runicrealms.runicguilds.guild.banner;

import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.GUIUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.model.GuildInfo;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GuildBannerUIListener implements Listener {

    /**
     * @param ui
     * @param meta
     */
    private void confirm(GuildBannerUI ui, BannerMeta meta) {
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
            ui.setupColorMenu();
        } else {
            selectColor(ui, meta);
            ui.setSelectedColor(null);
            ui.openPatternMenu();
        }
    }

    /**
     * Completes the guild banner, updating the color and patterns
     */
    private void finish(GuildBannerUI bannerUI, HumanEntity humanEntity, ItemStack dummy, BannerMeta meta) {
        if (!(humanEntity instanceof Player player)) return;
        if (bannerUI.getSelectedPattern() != null) {
            meta.removePattern(meta.getPatterns().size() - 1);
            dummy.setItemMeta(meta);
        }
        if (bannerUI.getSelectedColor() != null && bannerUI.getChosenColor() == null && bannerUI.getChosenPattern() == null) {
            dummy.setType(Material.WHITE_BANNER);
        }
        if (bannerUI.getSelectedColor() != null && bannerUI.getChosenColor() == null && bannerUI.getChosenPattern() != null) {
            Pattern pattern = meta.getPattern(meta.getPatterns().size() - 1);
            PatternType patternType = pattern.getPattern();
            meta.removePattern(meta.getPatterns().size() - 1);
            DyeColor dyeColor = (bannerUI.getDummyBanner().getType() == Material.BLACK_BANNER) ? DyeColor.WHITE : DyeColor.BLACK; // If banner is black, dye color = white
            meta.addPattern(new Pattern(dyeColor, patternType));
            dummy.setItemMeta(meta);
        }

        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (guildInfo != null) {
            bannerUI.getBanner().setBanner(bannerUI.getUUID(), dummy.getType(), (BannerMeta) dummy.getItemMeta());
            guildInfo.setGuildBanner(bannerUI.getBanner());
            humanEntity.sendMessage(ColorUtil.format("&r&6&lGuilds »&r &aYour guild's banner has been updated!"));
        } else {
            humanEntity.sendMessage(ColorUtil.format("&r&6&lGuilds »&r &cAn internal error has occurred, please try again..."));
        }
        humanEntity.closeInventory();
    }

    /**
     * Used for listening to the InventoryClickEvent
     *
     * @param ui   that is open
     * @param item item that was clicked
     * @return true if clicked item is a banner
     */
    private boolean isBanner(GuildBannerUI ui, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        String name = meta.getPersistentDataContainer().get(ui.getKey(), PersistentDataType.STRING);
        if (name == null) return false;
        return name.equals("banner");
    }

    /**
     * Used for listening to the InventoryClickEvent
     *
     * @param ui   that is open
     * @param item item that was clicked
     * @return true if clicked item is concrete
     */
    private boolean isConcrete(GuildBannerUI ui, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        String name = meta.getPersistentDataContainer().get(ui.getKey(), PersistentDataType.STRING);
        for (DyeColor color : DyeColor.values()) {
            if (color.name().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null) return;
        if (!(inventory.getHolder() instanceof GuildBannerUI guildBannerUI)) return;
        if (event.getCurrentItem() == null) return;

        event.setCancelled(true);

        BannerMeta meta = (BannerMeta) guildBannerUI.getDummyBanner().getItemMeta();
        assert meta != null;
        ItemStack item = event.getCurrentItem();
        ItemMeta itemMeta = item.getItemMeta();
        Material material = item.getType();

        if (material == GUIUtil.CLOSE_BUTTON.getType()) {
            event.getWhoClicked().closeInventory();
            return;
        }

        if (material == GUIUtil.BACK_BUTTON.getType()) {
            guildBannerUI.setupColorMenu();
            return;
        }

        if (meta.getPatterns().size() >= 6) {
            this.finish(guildBannerUI, event.getWhoClicked(), guildBannerUI.getDummyBanner(), meta);
            return;
        }

        if (material == Material.SLIME_BALL) {
            this.confirm(guildBannerUI, meta);
            return;
        }

        if (itemMeta.getPersistentDataContainer().has(guildBannerUI.getKey(), PersistentDataType.STRING) && this.isConcrete(guildBannerUI, item)) {
            this.selectColor(guildBannerUI, meta, itemMeta);
            return;
        }

        if (material == Material.PAINTING) {
            this.selectPattern(guildBannerUI, item, meta);
            return;
        }

        if (itemMeta.getPersistentDataContainer().has(guildBannerUI.getKey(), PersistentDataType.STRING) && this.isBanner(guildBannerUI, item)) {
            this.finish(guildBannerUI, event.getWhoClicked(), guildBannerUI.getDummyBanner(), meta);
        }
    }

    /**
     * Sets the color of this banner layer / pattern
     */
    private void selectColor(GuildBannerUI ui, BannerMeta meta) {
        DyeColor color = DyeColor.WHITE;
        if (ui.getChosenPattern() == null) {
            ui.getDummyBanner().setType(Material.valueOf(color.name() + "_BANNER"));
        } else {
            meta.removePattern(meta.getPatterns().size() - 1);
            meta.addPattern(new Pattern(color, ui.getChosenPattern()));
            ui.getDummyBanner().setItemMeta(meta);
        }
        ui.setSelectedColor(color);
        ui.setupColorMenu();
    }

    /**
     * @param ui
     * @param meta
     * @param itemMeta
     */
    private void selectColor(GuildBannerUI ui, BannerMeta meta, ItemMeta itemMeta) {
        DyeColor color = DyeColor.valueOf(itemMeta.getPersistentDataContainer().get(ui.getKey(), PersistentDataType.STRING));
        if (ui.getChosenPattern() == null) {
            ui.getDummyBanner().setType(Material.valueOf(color.name() + "_BANNER"));
        } else {
            meta.removePattern(meta.getPatterns().size() - 1);
            meta.addPattern(new Pattern(color, ui.getChosenPattern()));
            ui.getDummyBanner().setItemMeta(meta);
        }
        ui.setSelectedColor(color);
        ui.setupColorMenu();
    }

    /**
     * @param ui
     * @param item
     * @param meta
     */
    private void selectPattern(GuildBannerUI ui, ItemStack item, BannerMeta meta) {
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
    }
}
