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
     * @param player to add score
     * @param score  int score to add
     * @return true if successful
     */
    boolean addPlayerScore(UUID player, Integer score);

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
     * @return a list of all guilds
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
     * @param player
     * @return
     */
    GuildData getGuildData(UUID player);

    /**
     * @param prefix
     * @return
     */
    GuildData getGuildData(String prefix);

    /**
     * @return
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
     * @param guild
     * @return
     */
    List<GuildMember> getOnlineMembersWithOwner(Guild guild);

    /**
     * @param uuid to check
     * @return true if player in guild
     */
    boolean isInGuild(UUID uuid);

    /**
     * @param guildData
     * @param name
     * @return
     */
    GuildRenameResult renameGuild(GuildData guildData, String name);
}
