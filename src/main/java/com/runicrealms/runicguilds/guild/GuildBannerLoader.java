package com.runicrealms.runicguilds.guild;

import com.google.common.collect.Lists;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.util.GuildUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GuildBannerLoader extends BukkitRunnable {
    @Override
    public void run() {
        List<PostedGuildBanner> posted = Lists.newArrayList(Plugin.getPostedGuildBanners());
        posted.forEach(PostedGuildBanner::remove);

        List<Guild> ordering = new ArrayList<>(GuildUtil.getAllGuilds());
        List<Guild> guilds = new ArrayList<>();

        Comparator<Guild> comparator = Comparator.comparing(Guild::getScore).reversed();

        ordering.sort(comparator);

        if (ordering.size() < 3) {
            guilds.addAll(ordering);
        } else {
            for (int i = 0; i < 3; i++) {
                guilds.add(ordering.get(i));
            }
        }

        Bukkit.getScheduler().runTask(Plugin.getInstance(), () -> this.makeBanners(guilds));
    }

    private void makeBanners(List<Guild> guilds) {
        for (int i = 1; i <= guilds.size(); i++) {
            String path = "banners.number" + i;
            FileConfiguration config = Plugin.getInstance().getConfig();
            World world = Bukkit.getWorld(config.getString(path + ".world"));
            double x = config.getDouble(path + ".x");
            double y = config.getDouble(path + ".y");
            double z = config.getDouble(path + ".z");
            float yaw = (float) config.getDouble(path + ".yaw");
            Location location = new Location(world, x, y, z);
            location.setYaw(yaw);
            location.getChunk().setForceLoaded(true);
            PostedGuildBanner banner = new PostedGuildBanner(guilds.get(i - 1), location);
            Plugin.getPostedGuildBanners().add(banner);
        }
    }
}
