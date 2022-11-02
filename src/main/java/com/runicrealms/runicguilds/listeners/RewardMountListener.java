package com.runicrealms.runicguilds.listeners;

import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.guild.stage.StageReward;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.util.GuildUtil;
import com.runicrealms.runicmounts.api.event.MountedEvent;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * This..
 */
public class RewardMountListener implements Listener {

    @EventHandler(priority = EventPriority.LOW) // early
    public void onMount(MountedEvent event) {
        AbstractHorse abstractHorse = event.getMount().getEntity();
        Player player = event.getPlayer();
        double speed = abstractHorse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
        double finalSpeed = guildMountedSpeed(player, speed);
        abstractHorse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(finalSpeed);
    }

    /**
     * Increases the player's movement speed while mounted if their guild has reached the appropriate stage
     *
     * @param player        who mounted
     * @param previousSpeed their speed before bonuses
     * @return their new correct speed
     */
    private double guildMountedSpeed(final Player player, double previousSpeed) {
        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        if (guildData == null) return previousSpeed;
        Guild guild = guildData.getData();
        StageReward mountSpeedReward = StageReward.MOUNT_SPEED_BONUS;
        GuildStage requiredStage = GuildStage.getFromReward(mountSpeedReward);
        if (requiredStage == null) return previousSpeed;
        if (guild.getGuildStage().getRank() < requiredStage.getRank()) return previousSpeed;
        double bonusSpeed = previousSpeed * mountSpeedReward.getBuffPercent();
        return previousSpeed + bonusSpeed;
    }
}
