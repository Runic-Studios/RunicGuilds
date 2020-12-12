package com.runicrealms.runicguilds.guilds;

import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.data.GuildUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ForceLoadBanners extends BukkitRunnable {
    @Override
    public void run() {
        for (PostedGuildBanner banner : new ArrayList<>(Plugin.getPostedGuildBanners())) {
            banner.remove();
            Plugin.getPostedGuildBanners().remove(banner);
        }

        List<Guild> ordering = new ArrayList<>(GuildUtil.getAllGuilds());
        List<Guild> guilds = new ArrayList<>();
        Comparator<Guild> comparator = Comparator.comparing(Guild::getScore).reversed();

        ordering.sort(comparator);

        for (int i = 0; i < 3; i++) {
            guilds.add(ordering.get(i));
        }

        this.makeBanners(guilds);
    }

    private void makeBanners(List<Guild> guilds) {
        for (int i = 1; i <= 3; i++) {
            String path = "banners.number" + i;
            FileConfiguration config = Plugin.getInstance().getConfig();
            World world = Bukkit.getWorld(config.getString(path + ".world"));
            double x = config.getDouble(path + ".x");
            double y = config.getDouble(path + ".y");
            double z = config.getDouble(path + ".z");
            Location location = new Location(world, x, y, z);
            if(config.contains(path + ".pitch")&&config.contains(path + ".yaw")) {
                int pitch = config.getInt(path + ".pitch");
                int yaw = config.getInt(path + ".yaw");
                location.setPitch(pitch);
                location.setYaw(yaw);
            }
            PostedGuildBanner banner = new PostedGuildBanner(guilds.get(i - 1), location);
            Plugin.getPostedGuildBanners().add(banner);
        }
    }
}
