package com.runicrealms.runicguilds.guild;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class PostedGuildBanner {
    private final Guild guild;
    private final ArmorStand[] banner;
    private final Hologram hologram;

    public static final NamespacedKey KEY = new NamespacedKey(RunicGuilds.getInstance(), "bannerKey");

    public PostedGuildBanner(Guild guild, Location location) {
        this.guild = guild;
        this.banner = this.createShowcase(location);
        this.hologram = this.makeHologram(location);
    }

    public void remove() {
        this.hologram.delete();
        for (ArmorStand entity : this.banner) {
            entity.remove();
        }
        RunicGuilds.getPostedGuildBanners().remove(this);
    }

    public Guild getGuild() {
        return this.guild;
    }

    public ArmorStand[] getBanner() {
        return this.banner;
    }

    public Hologram getHologram() {
        return this.hologram;
    }

    private ArmorStand[] createShowcase(@NotNull Location location) {
        World world = location.getWorld();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        ArmorStand bannerBody = (ArmorStand) world.spawnEntity(new Location(world, x - .5, y - 1.45, z + .78), EntityType.ARMOR_STAND);
        this.armorStandSetup(bannerBody, this.guild.getGuildBanner().getBannerItem());

        ArmorStand logBody = (ArmorStand) location.getWorld().spawnEntity(new Location(world, x - .5, y - 1.6, z + .5), EntityType.ARMOR_STAND);
        this.armorStandSetup(logBody, new ItemStack(Material.OAK_LOG, 1));

        return new ArmorStand[]{bannerBody, logBody};
    }

    private void armorStandSetup(ArmorStand armorStand, ItemStack item) {
        armorStand.setInvulnerable(true);
        armorStand.setCanPickupItems(false);
        armorStand.setGravity(false);
        armorStand.setVisible(false);
        armorStand.setBasePlate(false);
        armorStand.setPersistent(false);
        armorStand.setCollidable(false);
        armorStand.setMarker(true);
        armorStand.getEquipment().setHelmet(item);
        armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.getPersistentDataContainer().set(KEY, PersistentDataType.STRING, this.guild.getGuildPrefix());
    }

    private Hologram makeHologram(Location location) {
        World world = location.getWorld();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        Hologram hologram = HologramsAPI.createHologram(RunicGuilds.getInstance(), new Location(world, x - .5, y + 2.85, z + .5));
        hologram.appendTextLine(ColorUtil.format("&r&6&l" + ChatColor.stripColor(this.guild.getGuildName())));
        hologram.appendTextLine(ColorUtil.format("&r&6&lScore: " + this.guild.getScore()));
        return hologram;
    }
}
