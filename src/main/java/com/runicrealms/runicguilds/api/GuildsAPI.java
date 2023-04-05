package com.runicrealms.runicguilds.api;

import com.runicrealms.runicguilds.guild.GuildCreationResult;
import com.runicrealms.runicguilds.guild.GuildRenameResult;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.model.GuildUUID;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public interface GuildsAPI {

    /**
     * Adds the player to the list of bank viewers. This is stored in Redis, ensuring data
     * remains consistent across the network
     *
     * @param guildUUID of the guild
     * @param uuid      of the player
     */
    void addBankViewer(GuildUUID guildUUID, UUID uuid);

    /**
     * Attempts to add guild score to the given player
     *
     * @param player to add score
     * @param score  int score to add
     * @return true if successful
     */
    boolean addGuildScore(UUID player, Integer score);

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
     * Used to determine which player will receive messages in guild chat
     *
     * @param player who sent message
     * @return a set of uuids of guild members
     */
    Set<UUID> getGuildRecipients(UUID player);

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
     * @param uuid to check
     * @return true if player in guild
     */
    boolean isInGuild(UUID uuid);

    /**
     * Removes the player from the list of bank viewers. This is stored in Redis, ensuring data
     * remains consistent across the network
     *
     * @param guildUUID of the guild
     * @param uuid      of the player
     */
    void removeBankViewer(GuildUUID guildUUID, UUID uuid);

    /**
     * Removes the specified player (by uuid) from the
     *
     * @param guildUUID of the guild
     * @param toRemove  uuid of player to remove
     */
    void removeGuildMember(GuildUUID guildUUID, UUID toRemove);

    /**
     * Attempts to rename the given guild
     *
     * @param guild the in-memory data of the guild
     * @param name  the intended new name
     * @return a rename result
     */
    GuildRenameResult renameGuild(GuildData guild, String name);

}
