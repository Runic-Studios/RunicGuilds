package com.runicrealms.runicguilds.api;

import com.runicrealms.runicguilds.guilds.Guild;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildMemberKickedEvent extends Event {

    private Guild guild;
    private UUID kicked;
    private UUID kicker;

    private static final HandlerList handlers = new HandlerList();

    public GuildMemberKickedEvent(Guild guild, UUID kicked, UUID kicker) {
        this.guild = guild;
        this.kicked = kicked;
        this.kicker = kicker;
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

    public UUID getKicked() {
        return this.kicked;
    }

    public UUID getKicker() {
        return this.kicker;
    }

}
