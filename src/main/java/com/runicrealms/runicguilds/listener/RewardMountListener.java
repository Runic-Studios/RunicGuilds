package com.runicrealms.runicguilds.listener;

import com.runicrealms.plugin.common.api.guilds.GuildStage;
import com.runicrealms.plugin.common.api.guilds.StageReward;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.model.GuildInfo;
import com.runicrealms.runicmounts.api.event.MountedEvent;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractHorse;
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
    private double guildMountedSpeed(final Player player, double previousSpeed) {
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (guildInfo == null) return previousSpeed;
        StageReward mountSpeedReward = StageReward.MOUNT_SPEED_BONUS;
        GuildStage requiredStage = GuildStage.getFromReward(mountSpeedReward);
        if (requiredStage == null) return previousSpeed;
        if (guildInfo.getExp() < requiredStage.getExp()) return previousSpeed;
        double bonusSpeed = previousSpeed * mountSpeedReward.getBuffPercent();
        return previousSpeed + bonusSpeed;
    }

    @EventHandler(priority = EventPriority.LOW) // early
    public void onMount(MountedEvent event) {
        AbstractHorse abstractHorse = event.getMount().getEntity();
        Player player = event.getPlayer();
        double speed = abstractHorse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
        double finalSpeed = guildMountedSpeed(player, speed);
        abstractHorse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(finalSpeed);
    }

}
