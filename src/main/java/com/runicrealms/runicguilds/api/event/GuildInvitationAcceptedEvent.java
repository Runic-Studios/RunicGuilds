package com.runicrealms.runicguilds.api.event;

import com.runicrealms.runicguilds.model.GuildData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GuildInvitationAcceptedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final GuildData guildData;
    private final UUID invited;
    private final UUID inviter;

    /**
     * @param guildData
     * @param invited
     * @param inviter
     */
    public GuildInvitationAcceptedEvent(GuildData guildData, UUID invited, UUID inviter) {
        this.guildData = guildData;
        this.invited = invited;
        this.inviter = inviter;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public GuildData getGuildData() {
        return this.guildData;
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
