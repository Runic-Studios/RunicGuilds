package com.runicrealms.runicguilds.api.event;

import com.runicrealms.runicguilds.guild.stage.GuildEXPSource;
import com.runicrealms.runicguilds.model.GuildUUID;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GiveGuildEXPEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final GuildUUID guildUUID;
    private final int amount;
    private final GuildEXPSource source;
    private boolean cancelled;

    public GiveGuildEXPEvent(GuildUUID guildUUID, int amount, GuildEXPSource source) {
        this.guildUUID = guildUUID;
        this.amount = amount;
        this.source = source;
        this.cancelled = false;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public int getAmount() {
        return this.amount;
    }

    public GuildUUID getGuildUUID() {
        return this.guildUUID;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public GuildEXPSource getSource() {
        return this.source;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
