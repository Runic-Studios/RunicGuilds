package com.runicrealms.runicguilds.model;

import com.mongodb.client.FindIterable;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.database.GuildMongoData;
import com.runicrealms.plugin.redis.RedisUtil;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.api.RunicGuildsAPI;
import com.runicrealms.runicguilds.api.event.*;
import com.runicrealms.runicguilds.command.GuildCommandMapManager;
import com.runicrealms.runicguilds.guild.*;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.ui.GuildBankUtil;
import com.runicrealms.runicguilds.util.GuildUtil;
import com.runicrealms.runicrestart.api.RunicRestartApi;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manager for handling guild data and keeping it consistent across the network
 *
 * @author Skyfallin
 */
public class GuildDataManager implements Listener, RunicGuildsAPI {

    private final Map<String, GuildData> guildDataMap;

    /**
     * Initializes guilds and adds them to memory on plugin startup
     */
    public GuildDataManager() {
        this.guildDataMap = new HashMap<>();
        /*
        Load guilds into memory from mongo / jedis on startup
         */
        try (Jedis jedis = RunicCoreAPI.getNewJedisResource()) {
            Bukkit.getScheduler().runTaskAsynchronously(RunicGuilds.getInstance(), () -> loadGuildIntoMemory(jedis));
        }
        /*
        Tab update task
         */
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicGuilds.getInstance(), this::updateGuildTabs, 100L, 20L);
        Bukkit.getLogger().log(Level.INFO, "[RunicGuilds] All guilds have been loaded!");
    }

    @Override
    public boolean addGuildScore(UUID player, Integer score) {
        if (isInGuild(player)) {
            GuildData guildData = RunicGuilds.getRunicGuildsAPI().getGuildData(player);
            if (guildData != null) {
                guildData.getGuild().increasePlayerScore(player, score);
                guildData.getGuild().recalculateScore();
            }
            return true;
        }
        return false;
    }

    @Override
    public GuildCreationResult createGuild(Player owner, String name, String prefix, boolean modCreated) {
        GuildCreationResult result = createGuild(owner.getUniqueId(), name, prefix);
        if (result == GuildCreationResult.SUCCESSFUL) {
            owner.playSound(owner.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            GuildData.setGuildForPlayer(name, owner.toString());
            Bukkit.getServer().getPluginManager().callEvent(new GuildCreationEvent(RunicGuilds.getRunicGuildsAPI().getGuildData(prefix).getGuild(), modCreated));
        }
        return result;
    }

    @Override
    public List<Guild> getAllGuilds() {
        List<Guild> allGuilds = new ArrayList<>();
        for (String key : guildDataMap.keySet()) {
            allGuilds.add(guildDataMap.get(key).getGuild());
        }
        return allGuilds;
    }

    @Override
    public Guild getGuild(UUID uuid) {
        GuildData data = RunicGuilds.getRunicGuildsAPI().getGuildData(uuid);
        if (data != null) {
            return data.getGuild();
        }
        return null;
    }

    @Override
    public Guild getGuild(String prefix) {
        GuildData data = RunicGuilds.getRunicGuildsAPI().getGuildData(prefix);
        if (data != null) {
            return data.getGuild();
        }
        return null;
    }

    @Override
    public GuildData getGuildData(UUID playerUuid) {
        for (Map.Entry<String, GuildData> entry : guildDataMap.entrySet()) {
            if (entry.getValue().getGuild().getOwner().getUUID().toString().equalsIgnoreCase(playerUuid.toString())) {
                return entry.getValue();
            }
            for (GuildMember member : entry.getValue().getGuild().getMembers()) {
                if (member.getUUID().toString().equalsIgnoreCase(playerUuid.toString())) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public GuildData getGuildData(String prefix) {
        for (Map.Entry<String, GuildData> entry : guildDataMap.entrySet()) {
            if (entry.getValue().getGuild().getGuildPrefix().equalsIgnoreCase(prefix)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public Map<String, GuildData> getGuildDataMap() {
        return guildDataMap;
    }

    @Override
    public Set<UUID> getGuildRecipients(UUID player) {
        if (!isInGuild(player)) {
            return null;
        }
        Set<UUID> recipients = new HashSet<>();
        Guild guild = getGuild(player);
        if (!guild.getOwner().getUUID().toString().equalsIgnoreCase(player.toString())) {
            recipients.add(guild.getOwner().getUUID());
        }
        for (GuildMember member : guild.getMembers()) {
            if (!member.getUUID().toString().equalsIgnoreCase(player.toString())) {
                recipients.add(member.getUUID());
            }
        }
        return recipients;
    }

    @Override
    public GuildStage getGuildStage(UUID uuid) {
        GuildData data = RunicGuilds.getRunicGuildsAPI().getGuildData(uuid);
        if (data != null) {
            return data.getGuild().getGuildStage();
        }
        return null;
    }

    @Override
    public GuildStage getGuildStage(String prefix) {
        GuildData data = RunicGuilds.getRunicGuildsAPI().getGuildData(prefix);
        if (data != null) {
            return data.getGuild().getGuildStage();
        }
        return null;
    }

    @Override
    public List<GuildMember> getOnlineMembersWithOwner(Guild guild) {
        List<GuildMember> online = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (guild.getMember(player.getUniqueId()) != null) {
                online.add(guild.getMember(player.getUniqueId()));
            }
        }
        return online;
    }

    @Override
    public boolean isInGuild(UUID uuid) {
        return RunicGuilds.getRunicGuildsAPI().getGuildData(uuid) != null;
    }

    @Override
    public GuildRenameResult renameGuild(GuildData guildData, String name) {
        if (name.length() > 16) {
            return GuildRenameResult.NAME_TOO_LONG;
        }
        for (String otherGuildPrefix : guildDataMap.keySet()) {
            if (guildDataMap.get(otherGuildPrefix).getGuild().getGuildName().equalsIgnoreCase(name)) {
                return GuildRenameResult.NAME_NOT_UNIQUE;
            }
        }
        try {
            guildData.getGuild().setGuildName(name);
            for (GuildMember member : guildData.getGuild().getMembers()) {
                GuildData.setGuildForPlayer(name, member.getUUID().toString());
            }
            GuildData.setGuildForPlayer(name, guildData.getGuild().getOwner().toString());
            // guildData.queueToSave();
        } catch (Exception exception) {
            exception.printStackTrace();
            return GuildRenameResult.INTERNAL_ERROR;
        }
        return GuildRenameResult.SUCCESSFUL;
    }

    /**
     * Attempts to create a guild
     *
     * @param owner  of the guild
     * @param name   of the guild
     * @param prefix of the guild
     * @return the creation result
     */
    private GuildCreationResult createGuild(UUID owner, String name, String prefix) {
        if (prefix.length() < 3 || prefix.length() > 4 || prefix.equalsIgnoreCase("None")) {
            return GuildCreationResult.BAD_PREFIX;
        }
        Pattern pattern = Pattern.compile("[a-zA-Z]");
        Matcher matcher = pattern.matcher(prefix);
        if (!matcher.find()) {
            return GuildCreationResult.BAD_PREFIX;
        }
        if (name.length() > 16) {
            return GuildCreationResult.NAME_TOO_LONG;
        }
        for (String guildPrefix : guildDataMap.keySet()) {
            if (guildDataMap.get(guildPrefix).getGuild().getGuildName().equalsIgnoreCase(name)) {
                return GuildCreationResult.NAME_NOT_UNIQUE;
            }
            if (guildDataMap.get(guildPrefix).getGuild().getOwner().getUUID().toString().equalsIgnoreCase(owner.toString())) {
                return GuildCreationResult.CREATOR_IN_GUILD;
            }
            for (GuildMember member : guildDataMap.get(guildPrefix).getGuild().getMembers()) {
                if (member.getUUID().toString().equalsIgnoreCase(owner.toString())) {
                    return GuildCreationResult.CREATOR_IN_GUILD;
                }
            }
            if (guildPrefix.equalsIgnoreCase(prefix)) {
                return GuildCreationResult.PREFIX_NOT_UNIQUE;
            }
        }
        List<ItemStack> bank = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            bank.add(null);
        }
        Map<GuildRank, Boolean> bankPermissions = new HashMap<>();
        for (GuildRank rank : GuildRank.values()) {
            if (rank != GuildRank.OWNER && !bankPermissions.containsKey(rank)) {
                bankPermissions.put(rank, rank.canAccessBankByDefault());
            }
        }
        try (Jedis jedis = RunicCoreAPI.getNewJedisResource()) {
            Guild guild = new Guild(new HashSet<>(), new GuildMember(owner, GuildRank.OWNER, 0, GuildUtil.getOfflinePlayerName(owner)), name, prefix, bank, 45, bankPermissions, 0);
            GuildMongoData guildMongoData = new GuildMongoData(guild.getGuildPrefix());
            GuildData data = new GuildData(guild.getGuildPrefix(), guildMongoData, jedis);
            guildDataMap.put(prefix, data);
        }
        // data.queueToSave();
        return GuildCreationResult.SUCCESSFUL;
    }

    private void loadGuildIntoMemory(Jedis jedis) {
        FindIterable<Document> iterable = RunicCore.getDatabaseManager().getGuildData().find();
        for (Document guildDocument : iterable) {
            String prefix = guildDocument.getString("prefix");
            GuildMongoData guildMongoData = new GuildMongoData(prefix);
            guildDataMap.put(prefix,
                    new GuildData
                            (
                                    prefix,
                                    guildMongoData,
                                    jedis
                            ));
        }
        RunicRestartApi.markPluginLoaded("guilds");
    }

    @EventHandler
    public void onGuildCreation(GuildCreationEvent event) {
        Player owner = Bukkit.getPlayer(event.getGuild().getOwner().getUUID());
        syncDisplays(owner);
    }

    /**
     * Handles logic for removing guild data from memory on guild disband
     */
    @EventHandler
    public void onGuildDisband(GuildDisbandEvent event) {
        // set guild data to "None" in redis / mongo, close guild bank
        Player player = event.getWhoDisbanded();
        Guild guild = event.getGuild();
        GuildData guildData = RunicGuilds.getRunicGuildsAPI().getGuildData(guild.getGuildPrefix());
        for (GuildMember member : guild.getMembers()) {
            GuildData.setGuildForPlayer("None", member.getUUID().toString());
            Player playerMember = Bukkit.getPlayer(member.getUUID());
            if (playerMember == null) continue;
            if (GuildBankUtil.isViewingBank(member.getUUID())) {
                GuildBankUtil.close(playerMember);
            }
        }

        // remove guild for owner
        GuildData.setGuildForPlayer("None", guild.getOwner().getUUID().toString());
        if (GuildBankUtil.isViewingBank(guild.getOwner().getUUID())) {
            Player playerOwner = Bukkit.getPlayer(guild.getOwner().getUUID());
            if (playerOwner != null)
                GuildBankUtil.close(playerOwner);
        }

        // remove from jedis, mongo, and memory
        try (Jedis jedis = RunicCoreAPI.getNewJedisResource()) {
            guildData.delete(jedis);
        }
        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Successfully disbanded guild."));
        GuildCommandMapManager.getDisbanding().remove(player.getUniqueId());
        syncDisplays(event.getWhoDisbanded());
        for (GuildMember member : event.getGuild().getMembersWithOwner()) {
            Player playerMember = Bukkit.getPlayer(member.getUUID());
            if (playerMember == null) continue;
            syncDisplays(playerMember);
        }
    }

    @EventHandler
    public void onInvitationAccept(GuildInvitationAcceptedEvent event) {
        Player whoWasInvited = Bukkit.getPlayer(event.getInvited());
        syncDisplays(whoWasInvited);
        for (GuildMember member : event.getGuild().getMembersWithOwner()) {
            Player playerMember = Bukkit.getPlayer(member.getUUID());
            if (playerMember == null) continue;
            syncDisplays(playerMember);
        }
    }

    @EventHandler(priority = EventPriority.HIGH) // late
    public void onJoin(PlayerJoinEvent event) {
        syncDisplays(event.getPlayer());
    }

    @EventHandler
    public void onKick(GuildMemberKickedEvent event) {
        Player whoWasKicked = Bukkit.getPlayer(event.getKicked());
        if (whoWasKicked == null) return;
        whoWasKicked.sendMessage(ColorUtil.format(GuildUtil.PREFIX + ChatColor.RED + "You have been kicked from your guild!"));
        syncDisplays(whoWasKicked);
        for (GuildMember member : event.getGuild().getMembersWithOwner()) {
            Player playerMember = Bukkit.getPlayer(member.getUUID());
            if (playerMember == null) continue;
            syncDisplays(playerMember);
        }
    }

    @EventHandler
    public void onLeave(GuildMemberLeaveEvent event) {
        Player whoLeft = Bukkit.getPlayer(event.getMember());
        syncDisplays(whoLeft);
        for (GuildMember member : event.getGuild().getMembersWithOwner()) {
            Player playerMember = Bukkit.getPlayer(member.getUUID());
            if (playerMember == null) continue;
            syncDisplays(playerMember);
        }
    }

    /**
     * Clears player from in-memory command maps
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        GuildCommandMapManager.getTransferOwnership().remove(event.getPlayer().getUniqueId());
        GuildCommandMapManager.getDisbanding().remove(event.getPlayer().getUniqueId());
        RunicGuilds.getPlayersCreatingGuild().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onTransfer(GuildOwnershipTransferedEvent event) {
        Player oldOwner = Bukkit.getPlayer(event.getOldOwner());
        syncDisplays(oldOwner);
        for (GuildMember member : event.getGuild().getMembersWithOwner()) {
            Player playerMember = Bukkit.getPlayer(member.getUUID());
            if (playerMember == null) continue;
            syncDisplays(playerMember);
        }
    }

    /**
     * Sync displays ensures that redis and the player's session data properly reflect changes in the player's
     * guild during play time
     * <p>
     * Also syncs scoreboards and tab
     *
     * @param player to sync
     */
    private void syncDisplays(Player player) {
        if (player == null) return;
        Guild guild = this.getGuild(player.getUniqueId());
        try (Jedis jedis = RunicCoreAPI.getNewJedisResource()) {
            String key = player.getUniqueId() + ":guild";
            if (guild != null) {
                jedis.set(player.getUniqueId() + ":guild", guild.getGuildName());
                jedis.expire(key, RedisUtil.EXPIRE_TIME);
            } else {
                jedis.set(player.getUniqueId() + ":guild", "None");
                jedis.expire(key, RedisUtil.EXPIRE_TIME);
            }
        }
        RunicCoreAPI.updatePlayerScoreboard(player);
    }

    /**
     * Updates the guild section of tab for all online players
     */
    private void updateGuildTabs() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            GuildUtil.updateGuildTabColumn(player);
        }
    }
}
