package com.runicrealms.runicguilds.api.event;

import com.runicrealms.runicguilds.model.GuildUUID;
import com.runicrealms.runicguilds.model.MemberData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This custom event is called when a player's guild score changes
 */
public class GuildScoreChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final GuildUUID guildUUID;
    private final MemberData memberData;
    private final int score;
    private final boolean negative;

    /**
     * @param guildUUID  of the guild
     * @param memberData of member who gained/loss guild score
     * @param score      gained or lost by the member
     * @param negative   true if we will subtract the score (for reset)
     */
    public GuildScoreChangeEvent(GuildUUID guildUUID, MemberData memberData, int score, boolean negative) {
        this.guildUUID = guildUUID;
        this.memberData = memberData;
        this.score = score;
        this.negative = negative;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public GuildUUID getGuildUUID() {
        return guildUUID;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public MemberData getMemberData() {
        return memberData;
    }

    public int getScore() {
        return score;
    }

    public boolean isNegative() {
        return negative;
    }
}
