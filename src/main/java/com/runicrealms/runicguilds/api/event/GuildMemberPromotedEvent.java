package com.runicrealms.runicguilds.api.event;

import com.runicrealms.runicguilds.guild.GuildRank;
import com.runicrealms.runicguilds.model.GuildUUID;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildMemberPromotedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final GuildUUID guildUUID;
    private final UUID promoted;
    private final UUID promoter;
    private final GuildRank newRank;

    public GuildMemberPromotedEvent(GuildUUID guildUUID, UUID promoted, UUID promoter, GuildRank newRank) {
        this.guildUUID = guildUUID;
        this.promoted = promoted;
        this.promoter = promoter;
        this.newRank = newRank;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public GuildRank getNewRank() {
        return newRank;
    }

    public UUID getPromoted() {
        return this.promoted;
    }

    public UUID getPromoter() {
        return this.promoter;
    }

    public GuildUUID guildUUID() {
        return this.guildUUID;
    }

}
