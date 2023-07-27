package com.runicrealms.plugin.runicguilds.api.event;

import com.runicrealms.plugin.runicguilds.model.MemberData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This custom event is called when a player's guild score changes
 */
public class GuildScoreChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final UUID guildUUID;
    private final MemberData memberData;
    private final int score;

    /**
     * @param guildUUID  of the guild
     * @param memberData of member who gained/loss guild score
     * @param score      gained or lost by the member
     */
    public GuildScoreChangeEvent(UUID guildUUID, MemberData memberData, int score) {
        this.guildUUID = guildUUID;
        this.memberData = memberData;
        this.score = score;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public UUID getUUID() {
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

}
