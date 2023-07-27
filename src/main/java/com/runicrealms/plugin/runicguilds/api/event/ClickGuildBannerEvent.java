package com.runicrealms.plugin.runicguilds.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class ClickGuildBannerEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final UUID guildUUID;
    private boolean cancelled;

    public ClickGuildBannerEvent(UUID guildUUID) {
        this.guildUUID = guildUUID;
        this.cancelled = false;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return this.HANDLERS_LIST;
    }

    public UUID guildUUID() {
        return this.guildUUID;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
