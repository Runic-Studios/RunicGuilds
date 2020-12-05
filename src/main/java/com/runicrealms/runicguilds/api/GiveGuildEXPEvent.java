package com.runicrealms.runicguilds.api;

import com.runicrealms.runicguilds.guilds.EXPSource;
import com.runicrealms.runicguilds.guilds.Guild;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GiveGuildEXPEvent extends Event implements Cancellable {
    private final Guild guild;
    private final int amount;
    private final EXPSource source;
    private boolean cancelled;
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public GiveGuildEXPEvent(Guild guild, int amount, EXPSource source) {
        this.guild = guild;
        this.amount = amount;
        this.source = source;
        this.cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return this.HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public Guild getGuildWrapper() {
        return this.guild;
    }

    public int getAmount() {
        return this.amount;
    }

    public EXPSource getSource() {
        return this.source;
    }
}
