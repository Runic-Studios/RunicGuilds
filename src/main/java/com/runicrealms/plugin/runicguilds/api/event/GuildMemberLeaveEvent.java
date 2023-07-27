package com.runicrealms.plugin.runicguilds.api.event;

import com.runicrealms.plugin.runicguilds.model.GuildData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildMemberLeaveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final UUID guildUUID;
    private final Player whoLeft;
    private final GuildData guildData;

    public GuildMemberLeaveEvent(UUID guildUUID, Player whoLeft, GuildData guildData) {
        this.guildUUID = guildUUID;
        this.whoLeft = whoLeft;
        this.guildData = guildData;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public UUID getUUID() {
        return this.guildUUID;
    }

    public GuildData getGuildData() {
        return guildData;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getWhoLeft() {
        return this.whoLeft;
    }

}
