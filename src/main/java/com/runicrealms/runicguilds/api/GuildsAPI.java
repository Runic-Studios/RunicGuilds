package com.runicrealms.runicguilds.api;

import com.runicrealms.runicguilds.guild.GuildCreationResult;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.model.GuildUUID;
import com.runicrealms.runicguilds.model.MemberData;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface GuildsAPI {

    /**
     * Attempts to add guild score to the given player.
     *
     * @param guildUUID  uuid of the guild
     * @param memberData of member to add score
     * @param score      int score to add
     * @return true if successful
     */
    boolean addGuildScore(GuildUUID guildUUID, MemberData memberData, Integer score);

    /**
     * Attempts to create a guild
     *
     * @param owner      player to become owner
     * @param name       of the guild
     * @param prefix     of the guild
     * @param modCreated true if created by a mod command
     * @return the result of the creation
     */
    GuildCreationResult createGuild(Player owner, String name, String prefix, boolean modCreated);

    /**
     * @param guildUUID of the GUILD
     * @return the stage of the guild
     */
    GuildStage getGuildStage(GuildUUID guildUUID);

    /**
     * Gives the specified guild an amount of exp
     *
     * @param guildUUID of the guild
     * @param exp       an amount of guild experience
     */
    void giveExperience(GuildUUID guildUUID, int exp);

    /**
     * @param player to check
     * @return true if player in guild
     */
    boolean isInGuild(UUID player);

}
