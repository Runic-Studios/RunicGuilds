package com.runicrealms.runicguilds.api;

import com.runicrealms.runicguilds.guilds.Guild;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildMemberInvitedEvent extends Event {

    private Guild guild;
    private UUID invited;
    private UUID inviter;

    private static final HandlerList handlers = new HandlerList();

    public GuildMemberInvitedEvent(Guild guild, UUID invited, UUID inviter) {
        this.guild = guild;
        this.invited = invited;
        this.inviter = inviter;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Guild getGuild() {
        return this.guild;
    }

    public UUID getInvited() {
        return this.invited;
    }

    public UUID getInviter() {
        return this.inviter;
    }

}
