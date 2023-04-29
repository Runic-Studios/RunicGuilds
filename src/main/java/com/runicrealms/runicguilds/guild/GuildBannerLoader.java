package com.runicrealms.runicguilds.guild;

import com.google.common.collect.Lists;
import com.runicrealms.libs.taskchain.TaskChain;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.model.ScoreContainer;
import com.runicrealms.runicguilds.util.TaskChainUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Loader that periodically retrieves the guild score for ALL guilds in redis (active guilds),
 * sorts by highest, then places that guild's banner at set locations in major cities
 *
 * @author BoBoBalloon, Skyfallin
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
     * @param guildScores a filtered list of the top ordered guilds (by guild score)
     */
    private void makeBanners(List<ScoreContainer> guildScores) {
        for (int i = 1; i <= guildScores.size(); i++) {
            String path = "banners.number" + i;
            Location location = BANNER_LOCATIONS.get(path);
            PostedGuildBanner banner = new PostedGuildBanner(guildScores.get(i - 1).guildUUID(), location);
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

        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> RunicGuilds.getDataAPI().loadAllGuildScores())
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicGuilds failed to load all guild scores!")
                .syncLast(guildScores -> {
                    List<ScoreContainer> guildsToDisplay = new ArrayList<>();
                    Comparator<ScoreContainer> comparator = Comparator.comparing(ScoreContainer::score).reversed();
                    guildScores.sort(comparator);

                    if (guildScores.size() < MAX_POSTED_BANNERS) {
                        guildsToDisplay.addAll(guildScores);
                    } else {
                        for (int i = 0; i < MAX_POSTED_BANNERS; i++) {
                            guildsToDisplay.add(guildScores.get(i));
                        }
                    }

                    Bukkit.getScheduler().runTask(RunicGuilds.getInstance(), () -> this.makeBanners(guildsToDisplay));
                })
                .execute();
    }
}
