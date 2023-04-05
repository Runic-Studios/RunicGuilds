package com.runicrealms.runicguilds.api.event;

import com.runicrealms.runicguilds.model.GuildUUID;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GuildInvitationAcceptedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final GuildUUID guildUUID;
    private final UUID invited;
    private final UUID inviter;

    public GuildInvitationAcceptedEvent(GuildUUID guildUUID, UUID invited, UUID inviter) {
        this.guildUUID = guildUUID;
        this.invited = invited;
        this.inviter = inviter;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public GuildUUID getGuildUUID() {
        return this.guildUUID;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public UUID getInvited() {
        return this.invited;
    }

    public UUID getInviter() {
        return this.inviter;
    }

}
