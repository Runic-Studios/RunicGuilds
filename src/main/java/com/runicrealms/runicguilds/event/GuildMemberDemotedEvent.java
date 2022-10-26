package com.runicrealms.runicguilds.event;

import com.runicrealms.runicguilds.guilds.Guild;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GuildMemberDemotedEvent extends Event {

    private Guild guild;
    private UUID demoted;
    private UUID demoter;

    private static final HandlerList handlers = new HandlerList();

    public GuildMemberDemotedEvent(Guild guild, UUID demoted, UUID demoter) {
        this.guild = guild;
        this.demoted = demoted;
        this.demoter = demoter;
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

    public UUID getDemoted() {
        return this.demoted;
    }

    public UUID getDemoter() {
        return this.demoter;
    }

}
