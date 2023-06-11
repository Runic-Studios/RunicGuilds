package com.runicrealms.runicguilds.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildOwnershipTransferEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player newOwner;
    private final Player oldOwner;
    private final UUID guildUUID;

    public GuildOwnershipTransferEvent(UUID guildUUID, Player newOwner, Player oldOwner) {
        this.guildUUID = guildUUID;
        this.newOwner = newOwner;
        this.oldOwner = oldOwner;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public UUID getUUID() {
        return this.guildUUID;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getNewOwner() {
        return this.newOwner;
    }

    public Player getOldOwner() {
        return this.oldOwner;
    }

}
