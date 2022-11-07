package com.runicrealms.runicguilds.api.event;

import com.runicrealms.runicguilds.guild.Guild;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildMemberInvitedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final UUID invited;
    private final UUID inviter;

    public GuildMemberInvitedEvent(Guild guild, UUID invited, UUID inviter) {
        this.guild = guild;
        this.invited = invited;
        this.inviter = inviter;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Guild getGuild() {
        return this.guild;
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

}
