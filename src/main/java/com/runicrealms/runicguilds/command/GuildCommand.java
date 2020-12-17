package com.runicrealms.runicguilds.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.runicrealms.plugin.item.util.ItemRemover;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.api.GuildCreationResult;
import com.runicrealms.runicguilds.api.GuildDisbandEvent;
import com.runicrealms.runicguilds.api.GuildInvitationAcceptedEvent;
import com.runicrealms.runicguilds.api.GuildInvitationDeclinedEvent;
import com.runicrealms.runicguilds.api.GuildMemberDemotedEvent;
import com.runicrealms.runicguilds.api.GuildMemberInvitedEvent;
import com.runicrealms.runicguilds.api.GuildMemberKickedEvent;
import com.runicrealms.runicguilds.api.GuildMemberLeaveEvent;
import com.runicrealms.runicguilds.api.GuildMemberPromotedEvent;
import com.runicrealms.runicguilds.api.GuildOwnershipTransferedEvent;
import com.runicrealms.runicguilds.api.RunicGuildsAPI;
import com.runicrealms.runicguilds.data.GuildData;
import com.runicrealms.runicguilds.data.GuildUtil;
import com.runicrealms.runicguilds.data.PlayerGuildDataUtil;
import com.runicrealms.runicguilds.gui.GuildBankUtil;
import com.runicrealms.runicguilds.gui.GuildBannerUI;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildMember;
import com.runicrealms.runicguilds.guilds.GuildRank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GuildCommand extends BaseCommand {

    private static Map<UUID, UUID> transferOwnership = new HashMap<UUID, UUID>();
    private static Map<UUID, UUID> invites = new HashMap<UUID, UUID>();
    private static Set<UUID> disbanding = new HashSet<UUID>();

    public GuildCommand() {
        //placeholder
    }

    @Default
    @CatchUnknown
    public void onGuildHelpCommand(CommandSender sender) {
        sendHelpMessage(sender);
    }

    @Subcommand("info")
    @Conditions("is-player")
    public void onGuildInfoCommand(Player player) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) != null) {
            Guild guild = GuildUtil.getGuildData(player.getUniqueId()).getData();
            sendMessage(player, "&6[" + guild.getScore() + "]&r &e&l" + guild.getGuildName());
            sendMessage(player, "&6Guild Owner: &7[" + guild.getOwner().getScore() + "] &e" + guild.getOwner().getLastKnownName());
            HashMap<GuildRank, StringBuilder> members = new HashMap<GuildRank, StringBuilder>();
            for (GuildMember member : guild.getMembers()) {
                if (!members.containsKey(member.getRank())) {
                    members.put(member.getRank(), new StringBuilder());
                }
                members.get(member.getRank())
                        .append("&7[")
                        .append(member.getScore())
                        .append("] &e")
                        .append(member.getLastKnownName())
                        .append("&r, ");
            }
            for (GuildRank rank : GuildRank.values()) {
                if (members.containsKey(rank)) {
                    sendMessage(player, "&6Guild " + rank.getPlural() + "s: &r" + members.get(rank).toString().substring(0, members.get(rank).toString().length() - 2));
                }
            }
        } else {
            sendMessage(player, "&eYou are not in a guild!");
        }
    }

    @Subcommand("invite")
    @Syntax("<player>")
    @CommandCompletion("@guild-invite")
    @Conditions("is-player")
    public void onGuildInviteCommand(Player player, String[] args) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            sendMessage(player, "&eYou are not in a guild!");
            return;
        }
        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();
        if (!guild.hasMinRank(player.getUniqueId(), GuildRank.RECRUITER)) {
            sendMessage(player, "&eYou must be of rank recruiter or higher to invite other players.");
            return;
        }
        if (args.length != 1) {
            sendHelpMessage(player);
            return;
        }
        if (Bukkit.getPlayerExact(args[0]) == null) {
            sendMessage(player, "&eThat player is not online.");
            return;
        }
        if (GuildUtil.getPlayerCache().get(Bukkit.getPlayer(args[0]).getUniqueId()) != null) {
            sendMessage(player, "&eThat player is already in a guild.");
            return;
        }
        sendMessage(Bukkit.getPlayerExact(args[0]), "&eYou have been invited to join the guild " + guild.getGuildName() + " by " + player.getName() + ". Type /guild accept to accept the invitation, or /guild decline to deny the invitation.");
        sendMessage(player, "&eYou have invited a player to the guild. An invitation has been sent.");
        invites.put(Bukkit.getPlayerExact(args[0]).getUniqueId(), player.getUniqueId());
        guildData.queueToSave();
        Bukkit.getServer().getPluginManager().callEvent(new GuildMemberInvitedEvent(guild, Bukkit.getPlayerExact(args[0]).getUniqueId(), player.getUniqueId()));
    }

    @Subcommand("settings bank")
    @Conditions("is-player")
    @CommandCompletion("Recruit|Member|Recruiter|Officer yes|no")
    public void onGuildSettingsBankCommand(Player player, String[] args) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            sendMessage(player, "&eYou are not in a guild!");
            return;
        }
        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();
        if (args.length != 2) {
            sendMessage(player, "&eUse the command: /guild bank settings <rank> <yes|no> to allow or deny a specific rank access to your bank.");
            return;
        }
        GuildRank rank = GuildRank.getByIdentifier(args[0]);
        if (rank == null) {
            sendMessage(player, "&eThat isn't a valid guild rank!");
            return;
        }
        if (rank == GuildRank.OWNER) {
            sendMessage(player, "&eYou cannot deny/allow bank access to the guild owner!");
            return;
        }
        if (args[1].equalsIgnoreCase("yes") || args[1].equalsIgnoreCase("true")) {
            guild.setBankAccess(rank, true);
            guildData.queueToSave();
            sendMessage(player, "&eUpdated guild bank permissions.");
        } else if (args[1].equalsIgnoreCase("no") || args[1].equalsIgnoreCase("false")) {
            guild.setBankAccess(rank, false);
            guildData.queueToSave();
            sendMessage(player, "&eUpdated guild bank permissions.");
        } else {
            sendMessage(player, "&ePlease enter \"yes\" or \"no\".");
        }
    }

    @Subcommand("bank")
    @Conditions("is-player|is-op")
    public void onGuildBankCommand(Player player) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            sendMessage(player, "&eYou are not in a guild!");
            return;
        }
        GuildBankUtil.open(player, 1);
    }

    @Subcommand("kick")
    @Syntax("<player>")
    @CommandCompletion("@guild-kick")
    @Conditions("is-player")
    public void onGuildKickCommand(Player player, String[] args) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            sendMessage(player, "&eYou are not in a guild!");
            return;
        }
        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();
        if (!guild.hasMinRank(player.getUniqueId(), GuildRank.OFFICER)) {
            sendMessage(player, "&eYou must be of rank officer or higher to kick other players.");
            return;
        }
        if (args.length != 1) {
            sendHelpMessage(player);
            return;
        }
        UUID otherPlayer = GuildUtil.getOfflinePlayerUUID(args[0]);
        if (otherPlayer.toString().equalsIgnoreCase(player.getUniqueId().toString())) {
            sendMessage(player, "&eYou can't remove yourself from the guild. To leave, type /guild leave.");
            return;
        }
        if (!guild.isInGuild(args[0])) {
            sendMessage(player, "&eThat player is not in your guild.");
            return;
        }
        if (guild.getMember(player.getUniqueId()).getRank().getRankNumber() >= guild.getMember(otherPlayer).getRank().getRankNumber()) {
            sendMessage(player, "&eYou can only kick players that are of lower rank than you.");
            return;
        }
        guild.removeMember(otherPlayer);
        PlayerGuildDataUtil.setGuildForPlayer("None", otherPlayer.toString());
        sendMessage(player, "&eRemoved player from the guild!");
        if (GuildUtil.getPlayerCache().containsKey(otherPlayer)) {
            GuildUtil.getPlayerCache().put(otherPlayer, null);
        }
        guildData.queueToSave();
        Bukkit.getServer().getPluginManager().callEvent(new GuildMemberKickedEvent(guild, Bukkit.getPlayerExact(args[0]).getUniqueId(), player.getUniqueId(), false));
        if (GuildBankUtil.isViewingBank(otherPlayer) && Bukkit.getPlayerExact(args[0]) != null) {
            GuildBankUtil.close(Bukkit.getPlayerExact(args[0]));
        }
    }

    @Subcommand("promote")
    @Syntax("<player>")
    @CommandCompletion("@guild-promote")
    @Conditions("is-player")
    public void onGuildPromoteCommand(Player player, String[] args) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            sendMessage(player, "&eYou are not in a guild!");
            return;
        }
        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();
        if (!guild.hasMinRank(player.getUniqueId(), GuildRank.OFFICER)) {
            sendMessage(player, "&eYou must be of rank officer or higher to promote other players.");
            return;
        }
        if (args.length != 1) {
            sendHelpMessage(player);
            return;
        }
        if (!guild.isInGuild(args[0])) {
            sendMessage(player, "&eThat player is not in your guild.");
            return;
        }
        GuildMember member = null;
        for (GuildMember target : guild.getMembers()) {
            if (target.getLastKnownName().equalsIgnoreCase(args[0])) {
                member = target;
            }
        }
        if (member == null) {
            sendMessage(player, "&eThat player is not in your guild!");
            return;
        }
        if (member.getRank().getRankNumber() <= guild.getMember(player.getUniqueId()).getRank().getRankNumber() && member.getRank() != GuildRank.OFFICER) {
            if (member.getRank() == GuildRank.OFFICER) {
                sendMessage(player, "&eYou cannot promote another player to owner. To transfer guild ownership, use /guild transfer.");
            } else {
                sendMessage(player, "&eYou can only promote members that are under your rank.");
            }
            return;
        }
        member.setRank(GuildRank.getByNumber(member.getRank().getRankNumber() - 1));
        sendMessage(player, "&eMember has been promoted.");
        guildData.queueToSave();
        Bukkit.getServer().getPluginManager().callEvent(new GuildMemberPromotedEvent(guild, member.getUUID(), player.getUniqueId()));
    }

    @Subcommand("demote")
    @Syntax("<player>")
    @CommandCompletion("@guild-demote")
    @Conditions("is-player")
    public void onGuildDemoteCommand(Player player, String[] args) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            sendMessage(player, "&eYou are not in a guild!");
            return;
        }
        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();
        if (!guild.hasMinRank(player.getUniqueId(), GuildRank.OFFICER)) {
            sendMessage(player, "&eYou must be of rank officer or higher to demote other players.");
            return;
        }
        if (args.length != 1) {
            sendHelpMessage(player);
            return;
        }
        if (!guild.isInGuild(args[0])) {
            sendMessage(player, "&eThat player is not in your guild.");
            return;
        }
        GuildMember member = null;
        for (GuildMember target : guild.getMembers()) {
            if (target.getLastKnownName().equalsIgnoreCase(args[0])) {
                member = target;
            }
        }
        if (member == null) {
            sendMessage(player, "&eThat player is not in your guild!");
            return;
        }
        if (member.getRank().getRankNumber() <= guild.getMember(player.getUniqueId()).getRank().getRankNumber() &&
                member.getRank() != GuildRank.RECRUIT) {
            if (member.getRank() == GuildRank.RECRUIT) {
                sendMessage(player, "&eYou cannot demote players of the lowest guild rank.");
            } else {
                sendMessage(player, "&eYou can only demote players that are under your rank.");
            }
            return;
        }
        member.setRank(GuildRank.getByNumber(member.getRank().getRankNumber() + 1));
        sendMessage(player, "&eMember has been demoted.");
        guildData.queueToSave();
        Bukkit.getServer().getPluginManager().callEvent(new GuildMemberDemotedEvent(guild, member.getUUID(), player.getUniqueId()));
    }

    @Subcommand("transfer")
    @Syntax("<player>")
    @CommandCompletion("@guild-transfer")
    @Conditions("is-player")
    public void onGuildTransferCommand(Player player, String[] args) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            sendMessage(player, "&eYou are not in a guild!");
            return;
        }
        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();
        if (guild.getMember(player.getUniqueId()).getRank() != GuildRank.OWNER) {
            sendMessage(player, "&eYou must be the guild owner to use this command.");
            return;
        }
        if (args.length != 1) {
            sendHelpMessage(player);
            return;
        }
        if (!guild.isInGuild(args[0])) {
            sendMessage(player, "&eThat player is not in your guild.");
            return;
        }
        if (args[0].equalsIgnoreCase(player.getName())) {
            sendMessage(player, "&eYou cannot transfer ownership to yourself.");
            return;
        }
        sendMessage(player, "&eType /guild confirm to confirm your actions, or /guild cancel to cancel. &cWARNING - You will be demoted to officer if you confirm!");
        transferOwnership.put(player.getUniqueId(), GuildUtil.getOfflinePlayerUUID(args[0]));
        if (disbanding.contains(player.getUniqueId())) {
            disbanding.remove(player.getUniqueId());
        }
    }

    @Subcommand("set name")
    @Conditions("is-player")
    @Syntax("<name>")
    public void onGuildSetNameCommand(Player player, String[] args) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            sendMessage(player, "&eYou are not in a guild!");
            return;
        }
        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();
        if (guild.getMember(player.getUniqueId()).getRank() != GuildRank.OWNER) {
            sendMessage(player, "&eYou must be guild owner to use that command!");
            return;
        }
        if (args.length == 0) {
            sendMessage(player, "&eType &6/guild set name &e<name>.");
            return;
        }
        sendMessage(player, "&e" + GuildUtil.renameGuild(guildData, combineArgs(args, 0)).getMessage());
    }

    @Subcommand("set prefix")
    @Conditions("is-player")
    @Syntax("<prefix>")
    public void onGuildSetPrefixCommand(Player player, String[] args) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            sendMessage(player, "&eYou are not in a guild!");
            return;
        }
        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();
        if (guild.getMember(player.getUniqueId()).getRank() != GuildRank.OWNER) {
            sendMessage(player, "&eYou must be guild owner to use that command!");
            return;
        }
        if (args.length != 1) {
            sendMessage(player, "&eType &6/guild set prefix &e<prefix>.");
            return;
        }
        GuildData finalGuildData = guildData;
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> {
            sendMessage(player, "&e" + GuildUtil.reprefixGuild(finalGuildData, args[0]).getMessage());
        });
    }

    @Subcommand("leave")
    @Conditions("is-player")
    public void onGuildLeaveCommand(Player player) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            sendMessage(player, "&eYou are not in a guild!");
            return;
        }
        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();
        if (guild.getMember(player.getUniqueId()).getRank() == GuildRank.OWNER) {
            sendMessage(player, "&eYou cannot leave the guild because you are the owner! To disband guild or transfer ownership, use those commands.");
            return;
        }
        PlayerGuildDataUtil.setGuildForPlayer("None", player.getUniqueId().toString());
        guild.removeMember(player.getUniqueId());
        sendMessage(player, "&eYou have left your guild.");
        GuildUtil.getPlayerCache().put(player.getUniqueId(), null);
        guildData.queueToSave();
        Bukkit.getServer().getPluginManager().callEvent(new GuildMemberLeaveEvent(guild, player.getUniqueId()));
        if (transferOwnership.containsKey(player.getUniqueId())) {
            transferOwnership.remove(player.getUniqueId());
        }
        if (disbanding.contains(player.getUniqueId())) {
            disbanding.remove(player.getUniqueId());
        }
        if (GuildBankUtil.isViewingBank(player.getUniqueId())) {
            GuildBankUtil.close(player);
        }
    }

    @Subcommand("confirm")
    @Conditions("is-player")
    public void onGuildConfirmCommand(Player player, String[] args) {
        String prefix = null;
        GuildData guildData = null;
        Guild guild = null;
        if (!(GuildUtil.getPlayerCache().get(player.getUniqueId()) == null
                && args.length > 0
                && (args[0].equalsIgnoreCase("confirm") || args[0].equalsIgnoreCase("cancel"))
                && Plugin.getPlayersCreatingGuild().contains(player.getUniqueId())
                && GuildUtil.getPlayerCache().get(player.getUniqueId()) == null)) {
            prefix = GuildUtil.getPlayerCache().get(player.getUniqueId());
            guildData = GuildUtil.getGuildData(prefix);
            guild = guildData.getData();
        }
        if (Plugin.getPlayersCreatingGuild().contains(player.getUniqueId())) {
            if (args.length < 2) {
                sendMessage(player, "&eTo confirm creation of your guild, type &6/guild confirm <guild-prefix> <guild-name>&e. The prefix must be of 3-6 english letters.");
                return;
            }
            if (!player.getInventory().contains(Material.GOLD_NUGGET, Plugin.GUILD_COST)) {
                sendMessage(player, "&ePut " + Plugin.GUILD_COST + " coins in your inventory, and speak with the guild herald again.");
                Plugin.getPlayersCreatingGuild().remove(player.getUniqueId());
                return;
            }
            GuildCreationResult result = RunicGuildsAPI.createGuild(player.getUniqueId(), combineArgs(args, 1), args[0], false);
            if (result != GuildCreationResult.SUCCESSFUL) {
                sendMessage(player, "&e" + result.getMessage() + " Try again, or type &6/guild cancel&e.");
                return;
            }
            PlayerGuildDataUtil.setGuildForPlayer(GuildUtil.getGuildData(player.getUniqueId()).getData().getGuildName(), player.getUniqueId().toString());
            ItemRemover.takeItem(player, Material.GOLD_NUGGET, Plugin.GUILD_COST);
            Plugin.getPlayersCreatingGuild().remove(player.getUniqueId());
            sendMessage(player, "&e" + result.getMessage());
        } else if (transferOwnership.containsKey(player.getUniqueId())) {
            guild.transferOwnership(guild.getMember(transferOwnership.get(player.getUniqueId())));
            sendMessage(player, "&eSuccessfully transferred guild ownership. You have been demoted to officer.");
            guildData.queueToSave();
            Bukkit.getServer().getPluginManager().callEvent(new GuildOwnershipTransferedEvent(guild, transferOwnership.get(player.getUniqueId()), player.getUniqueId()));
            transferOwnership.remove(player.getUniqueId());
        } else if (disbanding.contains(player.getUniqueId())) {
            for (GuildMember member : guild.getMembers()) {
                PlayerGuildDataUtil.setGuildForPlayer("None", member.getUUID().toString());
                if (GuildUtil.getPlayerCache().containsKey(member.getUUID())) {
                    GuildUtil.getPlayerCache().put(member.getUUID(), null);
                }
                if (GuildBankUtil.isViewingBank(member.getUUID())) {
                    GuildBankUtil.close(Bukkit.getPlayer(member.getUUID()));
                }
            }
            GuildUtil.getPlayerCache().put(guild.getOwner().getUUID(), null);
            PlayerGuildDataUtil.setGuildForPlayer("None", guild.getOwner().getUUID().toString());
            if (GuildBankUtil.isViewingBank(guild.getOwner().getUUID())) {
                GuildBankUtil.close(Bukkit.getPlayer(guild.getOwner().getUUID()));
            }
            Bukkit.getServer().getPluginManager().callEvent(new GuildDisbandEvent(guild, player.getUniqueId(), false));
            guildData.deleteData();
            GuildUtil.getGuildDatas().remove(guild.getGuildPrefix());
            sendMessage(player, "&eSuccessfully disbanded guild.");
            disbanding.remove(player.getUniqueId());
        } else {
            sendMessage(player, "&eYou have nothing to confirm.");
        }
    }

    @Subcommand("cancel")
    @Conditions("is-player")
    public void onGuildCancelCommand(Player player) {
        if (Plugin.getPlayersCreatingGuild().contains(player.getUniqueId())) {
            sendMessage(player, "&eCanceled creating guild.");
            Plugin.getPlayersCreatingGuild().remove(player.getUniqueId());
            return;
        }
        if (transferOwnership.containsKey(player.getUniqueId())) {
            sendMessage(player, "&eCanceled owner transfership.");
            transferOwnership.remove(player.getUniqueId());
            return;
        }
        if (disbanding.contains(player.getUniqueId())) {
            sendMessage(player, "&eCanceled disbanding of the guild.");
            disbanding.remove(player.getUniqueId());
        }
    }

    @Subcommand("disband")
    @Conditions("is-player")
    public void onGuildDisbandCommand(Player player) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            sendMessage(player, "&eYou are not in a guild!");
            return;
        }
        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();
        if (guild.getMember(player.getUniqueId()).getRank() != GuildRank.OWNER) {
            sendMessage(player, "&eYou must be the guild owner to use this command.");
            return;
        }
        sendMessage(player, "&eType /guild confirm if you with to proceed with disbanding the guild, or /guild cancel to cancel this.");
        disbanding.add(player.getUniqueId());
        if (transferOwnership.containsKey(player.getUniqueId())) {
            transferOwnership.remove(player.getUniqueId());
        }
    }

    @Subcommand("accept")
    @Conditions("is-player")
    public void onGuildAcceptCommand(Player player) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) != null) {
            sendMessage(player, "&eYou cannot use this command since you are in a guild.");
            return;
        }
        if (!invites.containsKey(player.getUniqueId())) {
            sendMessage(player, "&eYou don't have any pending invitations.");
            return;
        }
        if (Plugin.getPlayersCreatingGuild().contains(player.getUniqueId())) {
            Plugin.getPlayersCreatingGuild().remove(player.getUniqueId());
        }
        GuildData guildData = GuildUtil.getGuildData(invites.get(player.getUniqueId()));
        Guild guild = guildData.getData();
        guild.getMembers().add(new GuildMember(player.getUniqueId(), GuildRank.RECRUIT, 0, player.getName()));
        PlayerGuildDataUtil.setGuildForPlayer(guild.getGuildName(), player.getUniqueId().toString());
        sendMessage(player, "&eYou have accepted the guild invitation.");
        guildData.queueToSave();
        GuildUtil.getPlayerCache().put(player.getUniqueId(), guild.getGuildPrefix());
        Bukkit.getServer().getPluginManager().callEvent(new GuildInvitationAcceptedEvent(guild, player.getUniqueId(), invites.get(player.getUniqueId())));
        invites.remove(player.getUniqueId());
    }

    @Subcommand("decline")
    @Conditions("is-player")
    public void onGuildDeclineCommand(Player player) {
        if (!invites.containsKey(player.getUniqueId())) {
            sendMessage(player, "&eYou don't have any pending invitations.");
            return;
        }
        sendMessage(player, "&eYou have decline the guild invitation.");
        Guild guild = GuildUtil.getGuildData(invites.get(player.getUniqueId())).getData();
        Bukkit.getServer().getPluginManager().callEvent(new GuildInvitationDeclinedEvent(guild, player.getUniqueId(), invites.get(player.getUniqueId())));
        invites.remove(player.getUniqueId());
    }

    @Subcommand("banner")
    @Conditions("is-player")
    public void onGuildBannerCommand(Player player) {
        Guild guild = GuildUtil.getGuildData(player.getUniqueId()).getData();
        if (guild == null) {
            player.sendMessage(ColorUtil.format("&eYou are not in a guild!"));
            return;
        }
        if (!player.getUniqueId().equals(guild.getOwner().getUUID())) {
            player.sendMessage(ColorUtil.format("&6You must be the owner of your guild to execute this command!"));
            return;
        }
        GuildBannerUI ui = new GuildBannerUI(guild);
        player.sendMessage(ColorUtil.format("&6Initializing user interface..."));
        player.openInventory(ui.getInventory());
        ui.openColorMenu();
        //maybe we need to store something in cache???
    }

    private static void sendHelpMessage(CommandSender sender) {
        sendMessage(sender, "&6Guild Commands:");
        sendMessage(sender, "&e/guild info &r- gets guild members and score.");
        sendMessage(sender, "&e/guild invite &6[player] &r- invites a player to the guild.");
        sendMessage(sender, "&e/guild bank &r- opens your guild bank.");
        sendMessage(sender, "&e/guild set name&6/&eprefix <text> &r- sets your guild name/prefix.");
        sendMessage(sender, "&e/guild kick &6[player] &r- kicks a player from the guild.");
        sendMessage(sender, "&e/guild promote&6/&edemote &6[player] &r- promotes/demotes a guild member.");
        sendMessage(sender, "&e/guild disband &r- disbands your guild.");
        sendMessage(sender, "&e/guild transfer &6[player] &r- transfers the guild ownership to another member.");
        sendMessage(sender, "&e/guild leave &r- removes you from your guild.");
        sendMessage(sender, "&e/guild accept&6/&edecline &r- accepts/declines an invitation to join a guild.");
        sendMessage(sender, "&e/guild confirm&6/&ecancel &r- for confirming/canceling certain actions.");
    }

    private static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static Map<UUID, UUID> getTransferOwnership() {
        return transferOwnership;
    }

    public static Map<UUID, UUID> getInvites() {
        return invites;
    }

    public static Set<UUID> getDisbanding() {
        return disbanding;
    }

    private static String combineArgs(String[] args, int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            builder.append(args[i]);
            if (i != args.length - 1) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }

}
