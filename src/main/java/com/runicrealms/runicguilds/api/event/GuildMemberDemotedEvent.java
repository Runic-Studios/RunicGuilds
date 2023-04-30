package com.runicrealms.runicguilds.api.event;

import com.runicrealms.runicguilds.guild.GuildRank;
import com.runicrealms.runicguilds.model.GuildUUID;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildMemberDemotedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final GuildUUID guildUUID;
    private final UUID demoted;
    private final UUID demoter;
    private final GuildRank newRank;

    public GuildMemberDemotedEvent(GuildUUID guildUUID, UUID demoted, UUID demoter, GuildRank newRank) {
        this.guildUUID = guildUUID;
        this.demoted = demoted;
        this.demoter = demoter;
        this.newRank = newRank;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public UUID getDemoted() {
        return this.demoted;
    }

    public UUID getDemoter() {
        return this.demoter;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public GuildRank getNewRank() {
        return newRank;
    }

    public GuildUUID guildUUID() {
        return this.guildUUID;
    }

}
