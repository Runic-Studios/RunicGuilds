package com.runicrealms.runicguilds.api;

import com.runicrealms.runicguilds.guilds.Guild;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ClickGuildBannerEvent extends Event implements Cancellable {
    private final Guild guild;
    private boolean cancelled;

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public ClickGuildBannerEvent(Guild guild) {
        this.guild = guild;
        this.cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return this.HANDLERS_LIST;
    }

    public Guild getGuild() {
        return this.guild;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
