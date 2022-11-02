package com.runicrealms.runicguilds.listeners;

import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.guild.stage.StageReward;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.util.GuildUtil;
import com.runicrealms.runicmounts.event.DismountedEvent;
import com.runicrealms.runicmounts.event.MountedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * This..
 */
public class RewardMountListener implements Listener {

    @EventHandler
    public void onMount(MountedEvent e) {
        e.getPlayer().setWalkSpeed(guildMountedSpeed(e.getPlayer(), e.getPlayer().getWalkSpeed()));
    }

    @EventHandler
    public void onDismount(DismountedEvent e) {
        e.getPlayer().setWalkSpeed(guildMountedSpeed(e.getPlayer(), e.getPlayer().getWalkSpeed()));
    }

    /**
     * This..
     *
     * @param player
     * @param previousSpeed
     * @return
     */
    private float guildMountedSpeed(Player player, float previousSpeed) {
        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        if (guildData == null) return previousSpeed;
        Guild guild = guildData.getData();
        if (guild.getGuildLevel().getGuildStage().getExp() < GuildStage.STAGE5.getExp()) return previousSpeed;
        StageReward stageReward = StageReward.MOUNT_SPEED_BONUS;
        float bonusSpeed = (float) (previousSpeed * stageReward.getBuffPercent());
        return previousSpeed + bonusSpeed;
    }
}
