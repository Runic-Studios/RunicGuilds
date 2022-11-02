package com.runicrealms.runicguilds.util;

import com.mongodb.client.FindIterable;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.gui.GuildBankUtil;
import com.runicrealms.runicguilds.guild.*;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicrestart.api.RunicRestartApi;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuildUtil {

    private static final Map<String, GuildData> guilds = new HashMap<>();
    private static final Map<UUID, String> players = new HashMap<>();

    public static List<Guild> getAllGuilds() {
        List<Guild> allGuilds = new ArrayList<>();
        for (String key : guilds.keySet()) {
            allGuilds.add(guilds.get(key).getData());
        }
        return allGuilds;
    }

    public static void loadGuilds() {
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> {
            FindIterable<Document> iterable = RunicCore.getDatabaseManager().getGuildData().find();
            for (Document guildData : iterable) {
                guilds.put(guildData.getString("prefix"), new GuildData(guildData.getString("prefix")));
            }
            RunicRestartApi.markPluginLoaded("guilds");
        });
    }

    public static GuildData getGuildData(UUID player) {
        for (Map.Entry<String, GuildData> entry : guilds.entrySet()) {
            if (entry.getValue().getData().getOwner().getUUID().toString().equalsIgnoreCase(player.toString())) {
                return entry.getValue();
            }
            for (GuildMember member : entry.getValue().getData().getMembers()) {
                if (member.getUUID().toString().equalsIgnoreCase(player.toString())) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public static GuildData getGuildData(String prefix) {
        for (Map.Entry<String, GuildData> entry : guilds.entrySet()) {
            if (entry.getValue().getData().getGuildPrefix().equalsIgnoreCase(prefix)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static List<GuildMember> getOnlineMembersWithOwner(Guild guild) {
        List<GuildMember> online = new ArrayList<>();
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (guild.getMember(pl.getUniqueId()) != null) {
                online.add(guild.getMember(pl.getUniqueId()));
            }
        }
        return online;
    }

    public static GuildCreationResult createGuild(UUID owner, String name, String prefix) {
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
        for (String guild : guilds.keySet()) {
            if (guilds.get(guild).getData().getGuildName().equalsIgnoreCase(name)) {
                return GuildCreationResult.NAME_NOT_UNIQUE;
            }
            if (guilds.get(guild).getData().getOwner().getUUID().toString().equalsIgnoreCase(owner.toString())) {
                return GuildCreationResult.CREATOR_IN_GUILD;
            }
            for (GuildMember member : guilds.get(guild).getData().getMembers()) {
                if (member.getUUID().toString().equalsIgnoreCase(owner.toString())) {
                    return GuildCreationResult.CREATOR_IN_GUILD;
                }
            }
            if (guild.equalsIgnoreCase(prefix)) {
                return GuildCreationResult.PREFIX_NOT_UNIQUE;
            }
        }
        List<ItemStack> bank = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            bank.add(null);
        }
        Map<GuildRank, Boolean> bankPermissions = new HashMap<GuildRank, Boolean>();
        for (GuildRank rank : GuildRank.values()) {
            if (rank != GuildRank.OWNER && !bankPermissions.containsKey(rank)) {
                bankPermissions.put(rank, rank.canAccessBankByDefault());
            }
        }
        GuildData data = new GuildData(new Guild(new HashSet<>(), new GuildMember(owner, GuildRank.OWNER, 0, GuildUtil.getOfflinePlayerName(owner)), name, prefix, bank, 45, bankPermissions, 0));
        guilds.put(prefix, data);
        players.put(owner, prefix);
        // data.queueToSave();
        return GuildCreationResult.SUCCESSFUL;
    }

    public static Map<UUID, String> getPlayerCache() {
        return players;
    }

    public static Map<String, GuildData> getGuildDatas() {
        return guilds;
    }

    public static UUID getOfflinePlayerUUID(String playerName) {
        @SuppressWarnings("deprecation")
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player.hasPlayedBefore()) {
            return player.getUniqueId();
        }
        return null;
    }

    public static String getOfflinePlayerName(UUID uuid) {
        @SuppressWarnings("deprecation")
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.hasPlayedBefore()) {
            return player.getName();
        }
        return null;
    }

    public static void removeGuildFromCache(Guild guild) {
        guilds.remove(guild.getGuildPrefix());
    }

    public static GuildRenameResult renameGuild(GuildData guildData, String name) {
        if (name.length() > 16) {
            return GuildRenameResult.NAME_TOO_LONG;
        }
        for (String otherGuild : guilds.keySet()) {
            if (guilds.get(otherGuild).getData().getGuildName().equalsIgnoreCase(name)) {
                return GuildRenameResult.NAME_NOT_UNIQUE;
            }
        }
        try {
            guildData.getData().setGuildName(name);
            for (GuildMember member : guildData.getData().getMembers()) {
                GuildData.setGuildForPlayer(name, member.getUUID().toString());
            }
            GuildData.setGuildForPlayer(name, guildData.getData().getOwner().toString());
            // guildData.queueToSave();
        } catch (Exception exception) {
            exception.printStackTrace();
            return GuildRenameResult.INTERNAL_ERROR;
        }
        return GuildRenameResult.SUCCESSFUL;
    }

    public static GuildReprefixResult reprefixGuild(GuildData guildData, String prefix) { // Must be called async
        Pattern pattern = Pattern.compile("[a-zA-Z]");
        Matcher matcher = pattern.matcher(prefix);
        if (!matcher.find() || (prefix.length() > 6 || prefix.length() < 3)) {
            return GuildReprefixResult.BAD_PREFIX;
        }
        for (String otherGuild : guilds.keySet()) {
            if (otherGuild.equalsIgnoreCase(prefix)) {
                if (!guilds.get(otherGuild).getData().getGuildName().equalsIgnoreCase(guildData.getData().getGuildName())) {
                    return GuildReprefixResult.PREFIX_NOT_UNIQUE;
                }
            }
        }
        try {
            for (GuildMember member : guildData.getData().getMembersWithOwner()) {
                if (GuildBankUtil.isViewingBank(member.getUUID())) {
                    GuildBankUtil.close(Bukkit.getPlayer(member.getUUID()));
                }
                if (players.containsKey(member.getUUID())) {
                    players.put(member.getUUID(), prefix);
                }
            }
            guilds.remove(guildData.getData().getGuildPrefix());
            Guild guild = guildData.getData();
            guild.setGuildPrefix(prefix);
            guildData.getMongoData().set("prefix", prefix);
            guildData.getMongoData().save();
            GuildData newGuildData = new GuildData(guild, false);
            guilds.put(prefix, newGuildData);
        } catch (Exception exception) {
            exception.printStackTrace();
            return GuildReprefixResult.INTERNAL_ERROR;
        }
        return GuildReprefixResult.SUCCESSFUL;
    }

}
