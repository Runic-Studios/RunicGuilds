package com.runicrealms.runicguilds.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.runicrealms.runicguilds.guilds.Guild;

public class GuildCreationEvent extends Event {

    private Guild guild;
    private boolean modCreated;

    private static final HandlerList handlers = new HandlerList();

    public GuildCreationEvent(Guild guild, boolean modCreated) {
        this.guild = guild;
        this.modCreated = modCreated;
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
    
    public boolean didModCreate() {
    	return this.modCreated;
    }

}
