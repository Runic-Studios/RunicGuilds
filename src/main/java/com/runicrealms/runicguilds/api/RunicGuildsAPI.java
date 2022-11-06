package com.runicrealms.runicguilds.api;

import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.GuildCreationResult;
import com.runicrealms.runicguilds.guild.GuildMember;
import com.runicrealms.runicguilds.guild.GuildRenameResult;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.model.GuildData;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface RunicGuildsAPI {

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
     * @return a list of all guilds in the guildDataMap
     */
    List<Guild> getAllGuilds();

    /**
     * @param uuid of the player
     * @return the guild of the player (or null)
     */
    Guild getGuild(UUID uuid);

    /**
     * @param prefix of the guild
     * @return the guild (or null) matching prefix
     */
    Guild getGuild(String prefix);

    /**
     * @param playerUuid uuid of player to lookup
     * @return the GuildData object (or null if not found)
     */
    GuildData getGuildData(UUID playerUuid);

    /**
     * @param prefix of the GUILD
     * @return the GuildData object (or null if not found)
     */
    GuildData getGuildData(String prefix);

    /**
     * @return a map of guild redis/mongo data, keyed by prefix
     */
    Map<String, GuildData> getGuildDataMap();

    /**
     * Used to determine which player will receive messages in guild chat
     *
     * @param player who sent message
     * @return a set of uuids of guild members
     */
    Set<UUID> getGuildRecipients(UUID player);

    /**
     * @param uuid of the GUILD
     * @return the stage of the guild
     */
    GuildStage getGuildStage(UUID uuid);

    /**
     * @param prefix of the GUILD
     * @return the stage of the guild
     */
    GuildStage getGuildStage(String prefix);

    /**
     * Gets a list of online guild members, including the guild owner
     *
     * @param guild to check
     * @return a list of members
     */
    List<GuildMember> getOnlineMembersWithOwner(Guild guild);

    /**
     * @param uuid to check
     * @return true if player in guild
     */
    boolean isInGuild(UUID uuid);

    /**
     * Attempts to rename the given guild
     *
     * @param guildData the in-memory data of the guild
     * @param name      the intended new name
     * @return a rename result
     */
    GuildRenameResult renameGuild(GuildData guildData, String name);
}
