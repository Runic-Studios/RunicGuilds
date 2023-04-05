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
     * Gets a list of online guild members, including the guild owner
     *
     * @param guild to check
     * @return a list of members
     */
    List<GuildMember> getOnlineMembersWithOwner(GuildData guild);

    /**
     * @param uuid to check
     * @return true if player in guild
     */
    boolean isInGuild(UUID uuid);

    /**
     * Attempts to rename the given guild
     *
     * @param guild the in-memory data of the guild
     * @param name  the intended new name
     * @return a rename result
     */
    GuildRenameResult renameGuild(GuildData guild, String name);

}
