package com.runicrealms.runicguilds.guild;

import com.google.common.collect.Lists;
import com.runicrealms.runicguilds.RunicGuilds;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 *
 */
public class GuildBannerLoader extends BukkitRunnable {

    private static final int MAX_POSTED_BANNERS = 3; // how many banners display?
    private static final Map<String, Location> BANNER_LOCATIONS;

    static {
        BANNER_LOCATIONS = new HashMap<>();
        for (int i = 1; i <= MAX_POSTED_BANNERS; i++) {
            String path = "banners.number" + i;
            FileConfiguration config = RunicGuilds.getInstance().getConfig();
            World world = Bukkit.getWorld(config.getString(path + ".world"));
            double x = config.getDouble(path + ".x");
            double y = config.getDouble(path + ".y");
            double z = config.getDouble(path + ".z");
            float yaw = (float) config.getDouble(path + ".yaw");
            Location location = new Location(world, x, y, z);
            location.setYaw(yaw);
            location.getChunk().setForceLoaded(true);
            BANNER_LOCATIONS.put(path, location);
        }
    }

    /**
     * Creates guild banners for display in the guild quarter of 'hub' cities
     *
     * @param guilds a filtered list of the top ordered guilds (by guild score)
     */
    private void makeBanners(List<Guild> guilds) {
        for (int i = 1; i <= guilds.size(); i++) {
            String path = "banners.number" + i;
            Location location = BANNER_LOCATIONS.get(path);
            PostedGuildBanner banner = new PostedGuildBanner(guilds.get(i - 1), location);
            RunicGuilds.getPostedGuildBanners().add(banner);
        }
    }

    /**
     * A runnable (which can be called async) to sort the guilds, then calls a sync task to spawn
     * banners based on the top sorted guilds
     */
    @Override
    public void run() {
        List<PostedGuildBanner> posted = Lists.newArrayList(RunicGuilds.getPostedGuildBanners());
        posted.forEach(PostedGuildBanner::remove); // Remove existing banners

        List<Guild> ordering = new ArrayList<>(RunicGuilds.getGuildsAPI().getAllGuilds());
        List<Guild> guilds = new ArrayList<>();
        Comparator<Guild> comparator = Comparator.comparing(Guild::getScore).reversed();
        ordering.sort(comparator);

        if (ordering.size() < MAX_POSTED_BANNERS) {
            guilds.addAll(ordering);
        } else {
            for (int i = 0; i < MAX_POSTED_BANNERS; i++) {
                guilds.add(ordering.get(i));
            }
        }

        Bukkit.getScheduler().runTask(RunicGuilds.getInstance(), () -> this.makeBanners(guilds));
    }
}
