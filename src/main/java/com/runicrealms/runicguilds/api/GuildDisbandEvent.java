package com.runicrealms.runicguilds.api;

import com.runicrealms.runicguilds.guilds.Guild;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildDisbandEvent extends Event {

    private Guild guild;
    private UUID disbander;
    private boolean modDisbanded;

    private static final HandlerList handlers = new HandlerList();

    public GuildDisbandEvent(Guild guild, UUID disbander, boolean modDisbanded) {
        this.guild = guild;
        this.disbander = disbander;
        this.modDisbanded = modDisbanded;
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

    public UUID getDisbander() {
        return this.disbander;
    }

    public boolean didModDisband() {
        return this.modDisbanded;
    }

}
