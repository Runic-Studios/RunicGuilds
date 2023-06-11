package com.runicrealms.runicguilds.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildInvitationDeclinedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final UUID invited;
    private final UUID inviter;
    private final UUID guildUUID;

    public GuildInvitationDeclinedEvent(UUID guildUUID, UUID invited, UUID inviter) {
        this.guildUUID = guildUUID;
        this.invited = invited;
        this.inviter = inviter;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public UUID getInvited() {
        return this.invited;
    }

    public UUID getInviter() {
        return this.inviter;
    }

    public UUID guildUUID() {
        return this.guildUUID;
    }

}
