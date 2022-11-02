package com.runicrealms.runicguilds.api.event;

import com.runicrealms.runicguilds.guild.Guild;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildOwnershipTransferedEvent extends Event {

    private Guild guild;
    private UUID newOwner;
    private UUID oldOwner;

    private static final HandlerList handlers = new HandlerList();

    public GuildOwnershipTransferedEvent(Guild guild, UUID newOwner, UUID oldOwner) {
        this.guild = guild;
        this.newOwner = newOwner;
        this.oldOwner = oldOwner;
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

    public UUID getNewOwner() {
        return this.newOwner;
    }

    public UUID getOldOwner() {
        return this.oldOwner;
    }

}
