package com.runicrealms.runicguilds.api.event;

import com.runicrealms.runicguilds.model.GuildUUID;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This custom event is called when a player attempts to disband their guild
 */
public class GuildDisbandEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final GuildUUID guildUUID;
    private final boolean modDisbanded;
    private final Player whoDisbanded;

    /**
     * @param guildUUID    of guild to be disbanded
     * @param whoDisbanded the player who initiated the action
     * @param modDisbanded true if a mod is force-disbanding the guild
     */
    public GuildDisbandEvent(GuildUUID guildUUID, Player whoDisbanded, boolean modDisbanded) {
        this.guildUUID = guildUUID;
        this.whoDisbanded = whoDisbanded;
        this.modDisbanded = modDisbanded;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean didModDisband() {
        return this.modDisbanded;
    }

    public GuildUUID getGuildUUID() {
        return this.guildUUID;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getWhoDisbanded() {
        return this.whoDisbanded;
    }

}
