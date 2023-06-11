package com.runicrealms.runicguilds.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildMemberKickedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final UUID guildUUID;
    private final UUID kicked;
    private final UUID kicker;
    private final boolean modKicked;

    public GuildMemberKickedEvent(UUID guildUUID, UUID kicked, UUID kicker, boolean modKicked) {
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

    public UUID getUUID() {
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
