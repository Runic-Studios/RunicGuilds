package com.runicrealms.runicguilds.api.event;

import com.runicrealms.runicguilds.guild.GuildMember;
import com.runicrealms.runicguilds.model.GuildData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This custom event is called when a player's guild score changes
 */
public class GuildScoreChangeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final GuildData guildData;
    private final GuildMember guildMember;
    private final int score;
    private final boolean negative;

    /**
     * @param guildData   of the guild
     * @param guildMember who gained/loss guild score
     * @param score       gained or lost by the member
     * @param negative    true if we will subtract the score (for reset)
     */
    public GuildScoreChangeEvent(GuildData guildData, GuildMember guildMember, int score, boolean negative) {
        this.guildData = guildData;
        this.guildMember = guildMember;
        this.score = score;
        this.negative = negative;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public GuildData getGuildData() {
        return guildData;
    }

    public GuildMember getGuildMember() {
        return guildMember;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public int getScore() {
        return score;
    }

    public boolean isNegative() {
        return negative;
    }
}
