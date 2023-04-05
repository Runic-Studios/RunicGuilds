package com.runicrealms.runicguilds.api.event;

import com.runicrealms.runicguilds.model.GuildUUID;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildMemberKickedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final GuildUUID guildUUID;
    private final UUID kicked;
    private final UUID kicker;
    private final boolean modKicked;

    public GuildMemberKickedEvent(GuildUUID guildUUID, UUID kicked, UUID kicker, boolean modKicked) {
        this.guildUUID = guildUUID;
        this.kicked = kicked;
        this.kicker = kicker;
        this.modKicked = modKicked;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean didModKick() {
        return this.modKicked;
    }

    public GuildUUID getGuildUUID() {
        return this.guildUUID;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public UUID getKicked() {
        return this.kicked;
    }

    public UUID getKicker() {
        return this.kicker;
    }

}
