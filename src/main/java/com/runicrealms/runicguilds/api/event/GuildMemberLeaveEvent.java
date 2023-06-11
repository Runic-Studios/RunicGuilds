package com.runicrealms.runicguilds.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildMemberLeaveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final UUID guildUUID;
    private final UUID member;

    public GuildMemberLeaveEvent(UUID guildUUID, UUID member) {
        this.guildUUID = guildUUID;
        this.member = member;
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

    public UUID getMember() {
        return this.member;
    }

}
