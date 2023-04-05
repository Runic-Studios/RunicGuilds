package com.runicrealms.runicguilds.api.event;

import com.runicrealms.runicguilds.model.GuildUUID;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildOwnershipTransferEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final UUID newOwner;
    private final UUID oldOwner;
    private final GuildUUID guildUUID;

    public GuildOwnershipTransferEvent(GuildUUID guildUUID, UUID newOwner, UUID oldOwner) {
        this.guildUUID = guildUUID;
        this.newOwner = newOwner;
        this.oldOwner = oldOwner;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public GuildUUID getGuildUUID() {
        return this.guildUUID;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public UUID getNewOwner() {
        return this.newOwner;
    }

    public UUID getOldOwner() {
        return this.oldOwner;
    }

}
