package com.runicrealms.plugin.runicguilds.api.event;

import com.runicrealms.plugin.runicguilds.model.GuildData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildMemberKickedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final GuildData guildData;
    private final UUID kicked;
    private final UUID kicker;
    private final boolean modKicked;

    public GuildMemberKickedEvent(GuildData guildData, UUID kicked, UUID kicker, boolean modKicked) {
        this.guildData = guildData;
        this.kicked = kicked;
        this.kicker = kicker;
        this.modKicked = modKicked;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean didModKick() {
        return this.modKicked;
    }

    public GuildData guildData() {
        return this.guildData;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public UUID getKicked() {
        return this.kicked;
    }

    public UUID getKicker() {
        return this.kicker;
    }

}
