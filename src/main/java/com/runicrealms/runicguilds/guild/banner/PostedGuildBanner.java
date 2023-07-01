package com.runicrealms.runicguilds.guild.banner;

import com.runicrealms.plugin.common.api.guilds.GuildStage;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.model.GuildInfo;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Class that represents the spawned guild banners in hub cities for display
 */
public class PostedGuildBanner {
    public static final NamespacedKey KEY = new NamespacedKey(RunicGuilds.getInstance(), "bannerKey");
    // constants to adjust the location based on the yaw
    final double OFFSET_X_YAW_90 = 0.78;
    final double OFFSET_Z_YAW_90 = 0.5;
    private final UUID guildUUID;
    private final ArmorStand[] banner;
    private final Hologram hologram;

    public PostedGuildBanner(UUID guildUUID, Location location) {
        this.guildUUID = guildUUID;
        this.banner = this.createShowcase(location);
        this.hologram = this.makeHologram(location);
    }

    private void armorStandSetup(@NotNull ArmorStand armorStand, ItemStack item) {
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
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(this.guildUUID);
        if (guildInfo != null) {
            armorStand.getPersistentDataContainer().set(KEY, PersistentDataType.STRING, guildInfo.getUUID().toString());
        }
    }

    private ArmorStand[] createShowcase(@NotNull Location location) {
        World world = location.getWorld();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        assert world != null;
        double adjustedX = x - OFFSET_Z_YAW_90;
        double adjustedZ = z + OFFSET_X_YAW_90;
        if (location.getYaw() == 90) {
            adjustedX += OFFSET_Z_YAW_90;
            adjustedX -= OFFSET_X_YAW_90;
            adjustedZ -= OFFSET_X_YAW_90;
            adjustedZ += OFFSET_Z_YAW_90;
        }
        ArmorStand bannerBody = (ArmorStand) world.spawnEntity(new Location
                (
                        world, adjustedX, y - 1.45, adjustedZ, location.getYaw(), 0
                ), EntityType.ARMOR_STAND);
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(this.guildUUID);
        if (guildInfo != null) {
            String serializedBanner = guildInfo.getSerializedBanner();
            if (serializedBanner != null && !serializedBanner.equalsIgnoreCase("")) {
                this.armorStandSetup(bannerBody, BannerUtil.deserializeItemStack(serializedBanner));
            } else {
                // Banner wasn't loaded for some reason, use default banner and update
                ItemStack banner = BannerUtil.makeDefaultBanner(guildUUID);
                guildInfo.setSerializedBanner(BannerUtil.serializeItemStack(banner));
                this.armorStandSetup(bannerBody, BannerUtil.deserializeItemStack(guildInfo.getSerializedBanner()));
            }
        }

        ArmorStand logBody = (ArmorStand) world.spawnEntity(new Location(world, x - .5, y - 1.6, z + .5), EntityType.ARMOR_STAND);
        this.armorStandSetup(logBody, new ItemStack(Material.OAK_LOG, 1));

        return new ArmorStand[]{bannerBody, logBody};
    }

    public ArmorStand[] getBanner() {
        return this.banner;
    }

    public Hologram getHologram() {
        return this.hologram;
    }

    public UUID guildUUID() {
        return this.guildUUID;
    }

    private Hologram makeHologram(Location location) {
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(this.guildUUID);
        World world = location.getWorld();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        Hologram hologram = HolographicDisplaysAPI.get(RunicGuilds.getInstance()).createHologram(new Location(world, x - .5, y + 2.85, z + .5));
        if (guildInfo != null) {
            GuildStage guildStage = GuildStage.getFromExp(guildInfo.getExp());
            hologram.getLines().appendText(ColorUtil.format("&r&6&l" + ChatColor.stripColor(guildInfo.getName())));
            String color = guildStage.getRank() == GuildStage.getMaxStage().getRank() ? "&a&l" : "&f&l";
            hologram.getLines().appendText(ColorUtil.format("&r&6&lStage: [" + color + guildStage.getRank() + "&6&l/5]"));
            hologram.getLines().appendText(ColorUtil.format("&r&6&lScore: " + guildInfo.getScore()));
        }
        return hologram;
    }

    public void remove() {
        this.hologram.delete();
        for (ArmorStand entity : this.banner) {
            entity.remove();
        }
        RunicGuilds.getPostedGuildBanners().remove(this);
    }
}
