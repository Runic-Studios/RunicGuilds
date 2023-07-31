package com.runicrealms.plugin.runicguilds.model;

import com.runicrealms.plugin.common.api.guilds.GuildRank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.UUID;

public class MemberData {
    private String lastKnownName; // For when the player is offline
    private URL lastKnownSkin = null;
    private GuildRank rank;
    private Integer score = 0;
    private UUID uuid; // Of the PLAYER. Not a redundant field. Needed when we project member data

    @SuppressWarnings("unused")
    public MemberData() {
        // Default constructor for Spring
    }

    /**
     * Constructor to retrieve data from Redis
     *
     * @param uuid  of the player
     * @param name  of the player
     * @param rank  of the player
     * @param score of the player
     */
    public MemberData(@NotNull UUID uuid, @NotNull String name, @Nullable URL skin, @NotNull GuildRank rank, int score) {
        this.uuid = uuid;
        this.lastKnownName = name;
        this.lastKnownSkin = skin;
        this.rank = rank;
        this.score = score;
    }

    @NotNull
    public String getLastKnownName() {
        return lastKnownName;
    }

    public void setLastKnownName(@NotNull String lastKnownName) {
        this.lastKnownName = lastKnownName;
    }

    @Nullable
    public URL getLastKnownSkin() {
        return this.lastKnownSkin;
    }

    public void setLastKnownSkin(@Nullable URL lastKnownSkin) {
        this.lastKnownSkin = lastKnownSkin;
    }

    @NotNull
    public GuildRank getRank() {
        return rank;
    }

    public void setRank(@NotNull GuildRank rank) {
        this.rank = rank;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

}
