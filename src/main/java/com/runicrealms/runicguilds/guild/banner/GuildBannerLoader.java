package com.runicrealms.runicguilds.guild.banner;

import co.aikar.taskchain.TaskChain;
import com.google.common.collect.Lists;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.model.ScoreContainer;
import com.runicrealms.runicguilds.util.TaskChainUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loader that periodically retrieves the guild score for ALL guilds in redis (active guilds),
 * sorts by highest, then places that guild's banner at set locations in major cities
 *
 * @author BoBoBalloon, Skyfallin
 */
public class GuildBannerLoader extends BukkitRunnable {
    private static final int MAX_POSTED_BANNERS = 3; // How many banners to display
    private static final Map<String, List<Location>> BANNER_LOCATIONS;

    static {
        BANNER_LOCATIONS = new HashMap<>();
        try {
            FileConfiguration config = RunicGuilds.getInstance().getConfig();
            ConfigurationSection configSection = config.getConfigurationSection("banners");
            for (String key : configSection.getKeys(false)) {
                Bukkit.getLogger().severe(key);
                ConfigurationSection bannerSection = configSection.getConfigurationSection(key);
                List<Map<?, ?>> locationMaps = bannerSection.getMapList("locations");
                List<Location> locations = new ArrayList<>();
                for (Map<?, ?> locMap : locationMaps) {
                    World world = Bukkit.getWorld((String) locMap.get("world"));
                    double x = ((Number) locMap.get("x")).doubleValue();
                    double y = ((Number) locMap.get("y")).doubleValue();
                    double z = ((Number) locMap.get("z")).doubleValue();
                    float yaw = ((Number) locMap.get("yaw")).floatValue();
                    Location location = new Location(world, x, y, z);
                    location.setYaw(yaw);
                    location.getChunk().setForceLoaded(true);
                    locations.add(location);
                }
                BANNER_LOCATIONS.put(key, locations);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates guild banners for display in the guild quarter of 'hub' cities
     *
     * @param guildScores a filtered list of the top ordered guilds (by guild score)
     */
    private void makeBanners(List<ScoreContainer> guildScores) {
        for (int i = 1; i <= guildScores.size(); i++) {
            String path = switch (i) {
                case 2 -> "second";
                case 3 -> "third";
                default -> "first";
            };
            List<Location> locations = BANNER_LOCATIONS.get(path);
            for (Location location : locations) {
                PostedGuildBanner banner = new PostedGuildBanner(guildScores.get(i - 1).guildUUID(), location);
                RunicGuilds.getPostedGuildBanners().add(banner);
            }
        }
    }

    /**
     * A runnable (should be SYNC) to sort the guilds, then calls a sync task to spawn
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

                    this.makeBanners(guildsToDisplay);
                })
                .execute();
    }
}
