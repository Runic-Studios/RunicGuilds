package com.runicrealms.runicguilds.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This custom event is called when a player attempts to disband their guild
 */
public class GuildDisbandEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final boolean modDisbanded;
    private final Player whoDisbanded;

    /**
     * @param guild        to be disbanded
     * @param whoDisbanded the player who initiated the action
     * @param modDisbanded true if a mod is force-disbanding the guild
     */
    public GuildDisbandEvent(Guild guild, Player whoDisbanded, boolean modDisbanded) {
        this.guild = guild;
        this.whoDisbanded = whoDisbanded;
        this.modDisbanded = modDisbanded;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean didModDisband() {
        return this.modDisbanded;
    }

    public Guild getGuild() {
        return this.guild;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getWhoDisbanded() {
        return this.whoDisbanded;
    }

}
