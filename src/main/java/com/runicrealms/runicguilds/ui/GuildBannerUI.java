package com.runicrealms.runicguilds.ui;

import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.GUIUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.GuildBanner;
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

/**
 *
 */
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

    private final NamespacedKey key = new NamespacedKey(RunicGuilds.getInstance(), "itemKey");

    public GuildBannerUI(Guild guild) {
        this.inventory = Bukkit.createInventory(this, 54, ColorUtil.format("&r&6Guild Banner"));
        this.guild = guild;
        this.banner = guild.getGuildBanner();
        this.dummyBanner = this.banner();
        setupColorMenu();
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

    /**
     * Sets up the first screen of the banner UI, which prompts the user to select a color for a given layer
     */
    public void setupColorMenu() {
        this.page = 1;
        this.inventory.clear();
        GUIUtil.fillInventoryBorders(this.inventory);
        this.inventory.setItem(0, GUIUtil.CLOSE_BUTTON);
        this.inventory.setItem(4, this.dummyBanner);
        this.inventory.setItem(49, CONFIRM_COLOR);
        int[] slots = new int[]{13, 21, 22, 23, 28, 29, 30, 31, 32, 33, 34, 38, 39, 40, 41, 42};
        DyeColor[] colors = DyeColor.values();
        for (int i = 0; i < slots.length; i++) {
            this.inventory.setItem(slots[i], this.getConcrete(colors[i]));
        }
    }

    /**
     * Sets up the second screen of the banner ui, prompting the user to select a pattern for the layer
     */
    public void openPatternMenu() {
        this.page = 2;
        this.inventory.clear();
        GUIUtil.fillInventoryBorders(this.inventory);
        this.inventory.setItem(0, GUIUtil.BACK_BUTTON);
        this.inventory.setItem(4, this.dummyBanner);
        this.inventory.setItem(49, CONFIRM_LAYER);

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
            item = this.getPattern(pattern);
            this.getInventory().setItem(emptySlots.get(i), item);
        }
    }

    private ItemStack getConcrete(DyeColor color) {
        ItemStack item = new ItemStack(Material.getMaterial(color.name() + "_CONCRETE"), 1);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColorUtil.format("&7Apply color to layer"));

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
        assert meta != null;
        String name = type.name().toLowerCase() + "_pattern";
        String[] words = name.split("_");
        StringBuilder neoName = new StringBuilder();
        for (String word : words) {
            neoName.append(" ").append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
        }
        neoName = new StringBuilder(neoName.toString().trim());
        meta.setDisplayName(ColorUtil.format("&r" + neoName));

        if (this.selectedPattern == type) {
            meta.addEnchant(Enchantment.IMPALING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.getPersistentDataContainer().set(this.key, PersistentDataType.STRING, type.name());

        item.setItemMeta(meta);
        return item;
    }

    private static final ItemStack CONFIRM_COLOR;
    private static final ItemStack CONFIRM_LAYER;

    static {
        CONFIRM_COLOR = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta confirmMeta = CONFIRM_COLOR.getItemMeta();
        assert confirmMeta != null;
        confirmMeta.setDisplayName(ColorUtil.format("&r&aConfirm color for layer"));
        CONFIRM_COLOR.setItemMeta(confirmMeta);

        CONFIRM_LAYER = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta layerMeta = CONFIRM_LAYER.getItemMeta();
        assert layerMeta != null;
        layerMeta.setDisplayName(ColorUtil.format("&r&aAdd Layer"));
        CONFIRM_LAYER.setItemMeta(layerMeta);
    }

    private ItemStack banner() {
        ItemStack item = new ItemStack(Material.WHITE_BANNER, 1);
        BannerMeta meta = (BannerMeta) item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColorUtil.format("&6&lClick &7to finish the banner"));
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        meta.getPersistentDataContainer().set(this.key, PersistentDataType.STRING, "banner");
        item.setItemMeta(meta);
        return item;
    }
}