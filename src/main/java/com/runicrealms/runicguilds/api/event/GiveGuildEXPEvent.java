package com.runicrealms.runicguilds.api.event;

import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.stage.GuildEXPSource;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GiveGuildEXPEvent extends Event implements Cancellable {
    private final Guild guild;
    private final int amount;
    private final GuildEXPSource source;
    private boolean cancelled;

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public GiveGuildEXPEvent(Guild guild, int amount, GuildEXPSource source) {
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

    public GuildEXPSource getSource() {
        return this.source;
    }
}