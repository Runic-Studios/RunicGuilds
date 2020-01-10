package com.runicrealms.runicguilds.api;

import com.runicrealms.runicguilds.guilds.Guild;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildMemberLeaveEvent extends Event {

    private Guild guild;
    private UUID member;

    private static final HandlerList handlers = new HandlerList();

    public GuildMemberLeaveEvent(Guild guild, UUID member) {
        this.guild = guild;
        this.member = member;
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

    public UUID getMember() {
        return this.member;
    }

}
