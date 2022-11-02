package com.runicrealms.runicguilds.gui;

import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.GuildBanner;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class GuildBannerUI implements InventoryHolder {
    private final Inventory inventory;
    private final Guild guild;
    private final GuildBanner banner;
    private final ItemStack dummyBanner;

    private DyeColor selectedColor;
    private PatternType selectedPattern;
    private DyeColor chosenColor;
    private PatternType chosenPattern;
    private int page;

    private final ItemStack background = this.background();
    private final NamespacedKey key = new NamespacedKey(RunicGuilds.getInstance(), "itemKey");

    public GuildBannerUI(Guild guild) {
        this.inventory = Bukkit.createInventory(this, 54, ColorUtil.format("&r&6Guild Banner"));
        this.guild = guild;
        this.banner = guild.getGuildBanner();
        this.dummyBanner = this.banner();
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public Guild getGuild() {
        return this.guild;
    }

    public GuildBanner getBanner() {
        return this.banner;
    }

    public ItemStack getDummyBanner() {
        return this.dummyBanner;
    }

    public int getPage() {
        return this.page;
    }

    public NamespacedKey getKey() {
        return this.key;
    }

    public DyeColor getSelectedColor() {
        return this.selectedColor;
    }

    public PatternType getSelectedPattern() {
        return this.selectedPattern;
    }

    public DyeColor getChosenColor() {
        return this.chosenColor;
    }

    public PatternType getChosenPattern() {
        return this.chosenPattern;
    }

    public void setChosenColor(DyeColor chosenColor) {
        this.chosenColor = chosenColor;
    }

    public void setChosenPattern(PatternType chosenPattern) {
        this.chosenPattern = chosenPattern;
    }

    public void setSelectedColor(DyeColor selectedColor) {
        this.selectedColor = selectedColor;
    }

    public void setSelectedPattern(PatternType selectedPattern) {
        this.selectedPattern = selectedPattern;
    }

    public void openColorMenu() {
        this.page = 1;
        this.inventory.clear();
        this.inventory.setItem(4, this.dummyBanner);
        this.inventory.setItem(49, this.confirm());

        int[] slots = new int[]{13, 21, 22, 23, 28, 29, 30, 31, 32, 33, 34, 38, 39, 40, 41, 42};
        DyeColor[] colors = DyeColor.values();
        for (int i = 0; i < slots.length; i++) {
            this.inventory.setItem(slots[i], this.getConcrete(colors[i]));
        }


        int[] nextSlots = new int[]{0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 50, 51, 52, 53};
        for (int i = 0; i < nextSlots.length; i++) {
            this.inventory.setItem(nextSlots[i], this.background);
        }
    }

    public void openPatternMenu() {
        this.page = 2;
        this.inventory.clear();
        this.inventory.setItem(4, this.dummyBanner);
        this.inventory.setItem(49, this.confirm());

        int[] slots = new int[]{0, 1, 2, 3, 5, 6, 7, 8, 22, 31, 40};
        for (int i = 0; i < slots.length; i++) {
            this.inventory.setItem(slots[i], this.background);
        }

        List<Integer> emptySlots = new ArrayList<>();
        for (int i = 0; i <= 53; i++) {
            if (this.getInventory().getItem(i) == null) {
                emptySlots.add(i);
            }
        }

        PatternType[] types = PatternType.values();
        for (int i = 0; i < emptySlots.size(); i++) {
            PatternType pattern = types[i];
            ItemStack item;

            if (this.restrictedPattern(pattern) && this.guild.getGuildExp() < GuildStage.STAGE3.getExp()) {
                item = this.blocked(pattern);
            } else {
                item = this.getPattern(pattern);
            }

            this.getInventory().setItem(emptySlots.get(i), item);
        }
    }

    private ItemStack getConcrete(DyeColor color) {
        ItemStack item = new ItemStack(Material.getMaterial(color.name() + "_CONCRETE"), 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ColorUtil.format("&r"));

        if (this.getSelectedColor() == color) {
            meta.addEnchant(Enchantment.IMPALING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.getPersistentDataContainer().set(this.key, PersistentDataType.STRING, color.name());

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getPattern(PatternType type) {
        ItemStack item = new ItemStack(Material.PAINTING, 1);
        ItemMeta meta = item.getItemMeta();

        String name = type.name().toLowerCase() + "_pattern";
        String[] words = name.split("_");
        String neoName = "";
        for (String word : words) {
            neoName = neoName + " " + word.substring(0, 1).toUpperCase() + word.substring(1);
        }
        neoName = neoName.trim();
        meta.setDisplayName(ColorUtil.format("&r" + neoName));

        if (this.selectedPattern == type) {
            meta.addEnchant(Enchantment.IMPALING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.getPersistentDataContainer().set(this.key, PersistentDataType.STRING, type.name());

        item.setItemMeta(meta);
        return item;
    }

    private boolean restrictedPattern(PatternType pattern) {
        if (pattern == PatternType.SKULL || pattern == PatternType.CREEPER || pattern == PatternType.PIGLIN ||
                pattern == PatternType.MOJANG || pattern == PatternType.GLOBE) {
            return true;
        }
        return false;
    }

    private ItemStack confirm() {
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ColorUtil.format("&r&aConfirm"));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack background() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ColorUtil.format("&r"));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack blocked(PatternType patternType) {
        ItemStack item = new ItemStack(Material.BARRIER, 1);
        ItemMeta meta = item.getItemMeta();

        String name = patternType.name().toLowerCase() + "_pattern";
        String[] words = name.split("_");
        String neoName = "";
        for (String word : words) {
            neoName = neoName + " " + word.substring(0, 1).toUpperCase() + word.substring(1);
        }
        neoName = neoName.trim();
        meta.setDisplayName(ColorUtil.format("&r&c" + neoName));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack banner() {
        ItemStack item = new ItemStack(Material.WHITE_BANNER, 1);
        BannerMeta meta = (BannerMeta) item.getItemMeta();

        meta.setDisplayName(ColorUtil.format("&r&lClick to finish the banner"));

        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

        meta.getPersistentDataContainer().set(this.key, PersistentDataType.STRING, "banner");

        item.setItemMeta(meta);
        return item;
    }
}