package com.runicrealms.runicguilds.api;

import com.runicrealms.runicguilds.guilds.Guild;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildMemberPromotedEvent extends Event {

    private Guild guild;
    private UUID promoted;
    private UUID promoter;

    private static final HandlerList handlers = new HandlerList();

    public GuildMemberPromotedEvent(Guild guild, UUID promoted, UUID promoter) {
        this.guild = guild;
        this.promoted = promoted;
        this.promoter = promoter;
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

    public UUID getPromoted() {
        return this.promoted;
    }

    public UUID getPromoter() {
        return this.promoter;
    }

}
