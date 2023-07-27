package com.runicrealms.plugin.runicguilds.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildCreationEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final UUID guildUUID;
    private final UUID uuid;
    private final boolean modCreated;

    public GuildCreationEvent(UUID guildUUID, UUID uuid, boolean modCreated) {
        this.guildUUID = guildUUID;
        this.uuid = uuid;
        this.modCreated = modCreated;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean didModCreate() {
        return this.modCreated;
    }

    public UUID getUUID() {
        return guildUUID;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isModCreated() {
        return modCreated;
    }

}
