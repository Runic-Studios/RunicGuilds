package com.runicrealms.runicguilds.boss;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GuildBossSpawnEvent extends Event implements Cancellable {

    private LivingEntity guildBoss;
    private boolean isCancelled;

    /**
     * Called when a guild boss is spawned by the guild boss manager.
     * @param guildBoss the entity spawned from the activated spawner
     */
    public GuildBossSpawnEvent(LivingEntity guildBoss) {
        this.guildBoss = guildBoss;
        this.isCancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }

    public LivingEntity getGuildBoss() {
        return guildBoss;
    }

    public void setGuildBoss(LivingEntity guildBoss) {
        this.guildBoss = guildBoss;
    }

    private static final HandlerList handlers = new HandlerList();

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
