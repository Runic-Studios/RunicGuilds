package com.runicrealms.runicguilds.guilds;

import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.data.GuildUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ForceLoadBanners extends BukkitRunnable {
    @Override
    public void run() {
        for (PostedGuildBanner banner : Plugin.getPostedGuildBanners()) {
            banner.remove();
        }

        List<Guild> activeGuilds = GuildUtil.getAllGuilds();
        List<Guild> guilds = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            if (activeGuilds.size() - 1 < i) {
                continue;
            }
            guilds.add(activeGuilds.get(i));
        }

        this.makeBanners(guilds);
    }

    private void makeBanners(List<Guild> guilds) {
        Integer i = 1;
        for (Guild guild : guilds) {
            String path = "banners.number" + i.toString();
            FileConfiguration config = Plugin.getInstance().getConfig();
            World world = Bukkit.getWorld(config.getString(path + ".world"));
            double x = config.getDouble(path + ".x");
            double y = config.getDouble(path + ".y");
            double z = config.getDouble(path + ".z");
            Location location = new Location(world, x, y, z);
            PostedGuildBanner banner = new PostedGuildBanner(guild, location);
            Plugin.getPostedGuildBanners().add(banner);
            i++;
        }
    }
}