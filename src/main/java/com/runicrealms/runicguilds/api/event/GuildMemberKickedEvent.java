package com.runicrealms.runicguilds.api.event;

import com.runicrealms.runicguilds.guild.Guild;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildMemberKickedEvent extends Event {

    private Guild guild;
    private UUID kicked;
    private UUID kicker;
    private boolean modKicked;

    private static final HandlerList handlers = new HandlerList();

    public GuildMemberKickedEvent(Guild guild, UUID kicked, UUID kicker, boolean modKicked) {
        this.guild = guild;
        this.kicked = kicked;
        this.kicker = kicker;
        this.modKicked = modKicked;
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

    public boolean didModKick() {
        return this.modKicked;
    }

}
