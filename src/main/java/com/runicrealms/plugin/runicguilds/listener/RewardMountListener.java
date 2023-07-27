package com.runicrealms.plugin.runicguilds.listener;

import com.runicrealms.plugin.common.api.guilds.GuildStage;
import com.runicrealms.plugin.common.api.guilds.StageReward;
import com.runicrealms.plugin.runicguilds.RunicGuilds;
import com.runicrealms.plugin.runicguilds.model.GuildInfo;
import com.runicrealms.plugin.runicmounts.api.event.MountedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class RewardMountListener implements Listener {

    /**
     * Increases the player's movement speed while mounted if their guild has reached the appropriate stage
     *
     * @param player        who mounted
     * @param previousSpeed their speed before bonuses
     * @return their new correct speed
     */
    private float guildMountedSpeed(final Player player, float previousSpeed) {
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (guildInfo == null) return previousSpeed;
        StageReward mountSpeedReward = StageReward.MOUNT_SPEED_BONUS;
        GuildStage requiredStage = GuildStage.getFromReward(mountSpeedReward);
        if (requiredStage == null) return previousSpeed;
        if (guildInfo.getExp() < requiredStage.getExp()) return previousSpeed;
        float bonusSpeed = previousSpeed * (float) mountSpeedReward.getBuffPercent();
        return previousSpeed + bonusSpeed;
    }

    @EventHandler(priority = EventPriority.LOW) // early
    public void onMount(MountedEvent event) {
        Player player = event.getPlayer();
        float finalSpeed = guildMountedSpeed(player, event.getMount().getSpeed());
        event.getMount().setSpeed(finalSpeed);
    }

}
