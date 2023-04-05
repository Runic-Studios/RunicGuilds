package com.runicrealms.runicguilds.api.event;

import com.runicrealms.runicguilds.model.GuildUUID;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildCreationEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final GuildUUID guildUUID;
    private final UUID uuid;
    private final boolean modCreated;

    public GuildCreationEvent(GuildUUID guildUUID, UUID uuid, boolean modCreated) {
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

    public GuildUUID getGuildUUID() {
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
