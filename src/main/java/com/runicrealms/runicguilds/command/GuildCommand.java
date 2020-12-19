package com.runicrealms.runicguilds.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.runicrealms.plugin.item.util.ItemRemover;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.api.*;
import com.runicrealms.runicguilds.data.GuildData;
import com.runicrealms.runicguilds.data.GuildUtil;
import com.runicrealms.runicguilds.data.PlayerGuildDataUtil;
import com.runicrealms.runicguilds.gui.GuildBankUtil;
import com.runicrealms.runicguilds.gui.GuildBannerUI;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildMember;
import com.runicrealms.runicguilds.guilds.GuildRank;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class GuildCommand extends BaseCommand {

    private final Map<UUID, UUID> transferOwnership = new HashMap<>();
    private final Map<UUID, UUID> invites = new HashMap<>();
    private final Set<UUID> disbanding = new HashSet<>();

    public GuildCommand() {
        //placeholder
    }

    @Default
    @CatchUnknown
    public void onGuildHelpCommand(CommandSender sender) {
        this.sendHelpMessage(sender);
    }

    @Subcommand("info")
    @Conditions("is-player")
    public void onGuildInfoCommand(Player player) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format("&eYou are not in a guild!"));
            return;
        }

        Guild guild = GuildUtil.getGuildData(player.getUniqueId()).getData();
        player.sendMessage(ColorUtil.format("&6[" + guild.getScore() + "]&r &e&l" + guild.getGuildName()));
        player.sendMessage(ColorUtil.format("&6Guild Owner: &7[" + guild.getOwner().getScore() + "] &e" + guild.getOwner().getLastKnownName()));

        HashMap<GuildRank, StringBuilder> members = new HashMap<>();
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
                player.sendMessage(ColorUtil.format("&6Guild " + rank.getPlural() + "s: &r" + members.get(rank).substring(0, members.get(rank).toString().length() - 2)));
            }
        }
    }

    @Subcommand("invite")
    @Syntax("<player>")
    @CommandCompletion("@guild-invite")
    @Conditions("is-player")
    public void onGuildInviteCommand(Player player, String[] args) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format("&eYou are not in a guild!"));
            return;
        }

        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();
        if (!guild.hasMinRank(player.getUniqueId(), GuildRank.RECRUITER)) {
            player.sendMessage(ColorUtil.format("&eYou must be of rank recruiter or higher to invite other players."));
            return;
        }

        if (args.length != 1) {
            this.sendHelpMessage(player);
            return;
        }

        if (Bukkit.getPlayerExact(args[0]) == null) {
            player.sendMessage(ColorUtil.format("&eThat player is not online."));
            return;
        }

        if (GuildUtil.getPlayerCache().get(Bukkit.getPlayer(args[0]).getUniqueId()) != null) {
            player.sendMessage(ColorUtil.format("&eThat player is already in a guild."));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        target.sendMessage(ColorUtil.format("&eYou have been invited to join the guild " + guild.getGuildName() + " by " + player.getName() + ". Type /guild accept to accept the invitation, or /guild decline to deny the invitation."));
        player.sendMessage(ColorUtil.format("&eYou have invited a player to the guild. An invitation has been sent."));
        this.invites.put(target.getUniqueId(), player.getUniqueId());
        guildData.queueToSave();
        Bukkit.getServer().getPluginManager().callEvent(new GuildMemberInvitedEvent(guild, target.getUniqueId(), player.getUniqueId()));
    }

    @Subcommand("settings bank")
    @Conditions("is-player")
    @CommandCompletion("Recruit|Member|Recruiter|Officer yes|no")
    public void onGuildSettingsBankCommand(Player player, String[] args) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format("&eYou are not in a guild!"));
            return;
        }

        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();

        if (args.length != 2) {
            player.sendMessage(ColorUtil.format("&eUse the command: /guild bank settings <rank> <yes|no> to allow or deny a specific rank access to your bank."));
            return;
        }

        GuildRank rank = GuildRank.getByIdentifier(args[0]);
        if (rank == null) {
            player.sendMessage(ColorUtil.format("&eThat isn't a valid guild rank!"));
            return;
        }

        if (rank == GuildRank.OWNER) {
            player.sendMessage(ColorUtil.format("&eYou cannot deny/allow bank access to the guild owner!"));
            return;
        }

        if (args[1].equalsIgnoreCase("yes") || args[1].equalsIgnoreCase("true")) {
            guild.setBankAccess(rank, true);
            guildData.queueToSave();
            player.sendMessage(ColorUtil.format("&eUpdated guild bank permissions."));
        } else if (args[1].equalsIgnoreCase("no") || args[1].equalsIgnoreCase("false")) {
            guild.setBankAccess(rank, false);
            guildData.queueToSave();
            player.sendMessage(ColorUtil.format("&eUpdated guild bank permissions."));
        } else {
            player.sendMessage(ColorUtil.format("&ePlease enter \"yes\" or \"no\"."));
        }
    }

    @Subcommand("bank")
    @Conditions("is-player|is-op")
    public void onGuildBankCommand(Player player) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format("&eYou are not in a guild!"));
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
            player.sendMessage(ColorUtil.format("&eYou are not in a guild!"));
            return;
        }

        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();

        if (!guild.hasMinRank(player.getUniqueId(), GuildRank.OFFICER)) {
            player.sendMessage(ColorUtil.format("&eYou must be of rank officer or higher to kick other players."));
            return;
        }

        if (args.length != 1) {
            this.sendHelpMessage(player);
            return;
        }

        UUID otherPlayer = GuildUtil.getOfflinePlayerUUID(args[0]);
        if (otherPlayer.toString().equalsIgnoreCase(player.getUniqueId().toString())) {
            player.sendMessage(ColorUtil.format("&eYou can't remove yourself from the guild. To leave, type /guild leave."));
            return;
        }

        if (!guild.isInGuild(args[0])) {
            player.sendMessage(ColorUtil.format("&eThat player is not in your guild."));
            return;
        }

        if (guild.getMember(player.getUniqueId()).getRank().getRankNumber() >= guild.getMember(otherPlayer).getRank().getRankNumber()) {
            player.sendMessage(ColorUtil.format("&eYou can only kick players that are of lower rank than you."));
            return;
        }

        guild.removeMember(otherPlayer);
        PlayerGuildDataUtil.setGuildForPlayer("None", otherPlayer.toString());
        player.sendMessage(ColorUtil.format("&eRemoved player from the guild!"));
        if (GuildUtil.getPlayerCache().containsKey(otherPlayer)) {
            GuildUtil.getPlayerCache().put(otherPlayer, null);
        }

        guildData.queueToSave();
        Player target = Bukkit.getPlayerExact(args[0]);
        Bukkit.getServer().getPluginManager().callEvent(new GuildMemberKickedEvent(guild, target.getUniqueId(), player.getUniqueId(), false));
        if (GuildBankUtil.isViewingBank(otherPlayer) && target != null) {
            GuildBankUtil.close(target);
        }
    }

    @Subcommand("promote")
    @Syntax("<player>")
    @CommandCompletion("@guild-promote")
    @Conditions("is-player")
    public void onGuildPromoteCommand(Player player, String[] args) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format("&eYou are not in a guild!"));
            return;
        }

        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();

        if (!guild.hasMinRank(player.getUniqueId(), GuildRank.OFFICER)) {
            player.sendMessage(ColorUtil.format("&eYou must be of rank officer or higher to promote other players."));
            return;
        }

        if (args.length != 1) {
            this.sendHelpMessage(player);
            return;
        }

        if (!guild.isInGuild(args[0])) {
            player.sendMessage(ColorUtil.format("&eThat player is not in your guild."));
            return;
        }

        GuildMember member = null;
        for (GuildMember target : guild.getMembers()) {
            if (target.getLastKnownName().equalsIgnoreCase(args[0])) {
                member = target;
            }
        }

        if (member == null) {
            player.sendMessage(ColorUtil.format("&eThat player is not in your guild!"));
            return;
        }

        if (member.getRank().getRankNumber() <= guild.getMember(player.getUniqueId()).getRank().getRankNumber() && member.getRank() != GuildRank.OFFICER) {
            if (member.getRank() == GuildRank.OFFICER) {
                player.sendMessage(ColorUtil.format("&eYou cannot promote another player to owner. To transfer guild ownership, use /guild transfer."));
            } else {
                player.sendMessage(ColorUtil.format("&eYou can only promote members that are under your rank."));
            }
            return;
        }

        member.setRank(GuildRank.getByNumber(member.getRank().getRankNumber() - 1));
        player.sendMessage(ColorUtil.format("&e" + member.getLastKnownName() + " has been promoted."));
        guildData.queueToSave();
        Bukkit.getServer().getPluginManager().callEvent(new GuildMemberPromotedEvent(guild, member.getUUID(), player.getUniqueId()));
    }

    @Subcommand("demote")
    @Syntax("<player>")
    @CommandCompletion("@guild-demote")
    @Conditions("is-player")
    public void onGuildDemoteCommand(Player player, String[] args) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format("&eYou are not in a guild!"));
            return;
        }

        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();

        if (!guild.hasMinRank(player.getUniqueId(), GuildRank.OFFICER)) {
            player.sendMessage(ColorUtil.format("&eYou must be of rank officer or higher to demote other players."));
            return;
        }

        if (args.length != 1) {
            this.sendHelpMessage(player);
            return;
        }

        if (!guild.isInGuild(args[0])) {
            player.sendMessage(ColorUtil.format("&eThat player is not in your guild."));
            return;
        }

        GuildMember member = null;
        for (GuildMember target : guild.getMembers()) {
            if (target.getLastKnownName().equalsIgnoreCase(args[0])) {
                member = target;
            }
        }

        if (member == null) {
            player.sendMessage(ColorUtil.format("&eThat player is not in your guild!"));
            return;
        }

        if (member.getRank().getRankNumber() <= guild.getMember(player.getUniqueId()).getRank().getRankNumber() &&
                member.getRank() != GuildRank.RECRUIT) {
            if (member.getRank() == GuildRank.RECRUIT) {
                player.sendMessage(ColorUtil.format("&eYou cannot demote players of the lowest guild rank."));
            } else {
                player.sendMessage(ColorUtil.format("&eYou can only demote players that are under your rank."));
            }
            return;
        }

        member.setRank(GuildRank.getByNumber(member.getRank().getRankNumber() + 1));
        player.sendMessage(ColorUtil.format("&e" + member.getLastKnownName() + " has been demoted."));
        guildData.queueToSave();
        Bukkit.getServer().getPluginManager().callEvent(new GuildMemberDemotedEvent(guild, member.getUUID(), player.getUniqueId()));
    }

    @Subcommand("transfer")
    @Syntax("<player>")
    @CommandCompletion("@guild-transfer")
    @Conditions("is-player")
    public void onGuildTransferCommand(Player player, String[] args) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format("&eYou are not in a guild!"));
            return;
        }

        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();

        if (guild.getMember(player.getUniqueId()).getRank() != GuildRank.OWNER) {
            player.sendMessage(ColorUtil.format("&eYou must be the guild owner to use this command."));
            return;
        }

        if (args.length != 1) {
            this.sendHelpMessage(player);
            return;
        }

        if (!guild.isInGuild(args[0])) {
            player.sendMessage(ColorUtil.format("&eThat player is not in your guild."));
            return;
        }

        if (args[0].equalsIgnoreCase(player.getName())) {
            player.sendMessage(ColorUtil.format("&eYou cannot transfer ownership to yourself."));
            return;
        }

        player.sendMessage(ColorUtil.format("&eType /guild confirm to confirm your actions, or /guild cancel to cancel. &cWARNING - You will be demoted to officer if you confirm!"));
        this.transferOwnership.put(player.getUniqueId(), GuildUtil.getOfflinePlayerUUID(args[0]));
        this.disbanding.remove(player.getUniqueId());
    }

    @Subcommand("set name")
    @Conditions("is-player")
    @Syntax("<name>")
    public void onGuildSetNameCommand(Player player, String[] args) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format("&eYou are not in a guild!"));
            return;
        }

        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();

        if (guild.getMember(player.getUniqueId()).getRank() != GuildRank.OWNER) {
            player.sendMessage(ColorUtil.format("&eYou must be guild owner to use that command!"));
            return;
        }

        if (args.length == 0) {
            player.sendMessage(ColorUtil.format("&eType &6/guild set name &e<name>."));
            return;
        }
        player.sendMessage(ColorUtil.format("&e" + GuildUtil.renameGuild(guildData, this.combineArgs(args, 0)).getMessage()));
    }

    @Subcommand("set prefix")
    @Conditions("is-player")
    @Syntax("<prefix>")
    public void onGuildSetPrefixCommand(Player player, String[] args) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format("&eYou are not in a guild!"));
            return;
        }

        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();

        if (guild.getMember(player.getUniqueId()).getRank() != GuildRank.OWNER) {
            player.sendMessage(ColorUtil.format("&eYou must be guild owner to use that command!"));
            return;
        }

        if (args.length != 1) {
            player.sendMessage(ColorUtil.format("&eType &6/guild set prefix &e<prefix>."));
            return;
        }

        GuildData finalGuildData = guildData;
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> player.sendMessage(ColorUtil.format("&e" + GuildUtil.reprefixGuild(finalGuildData, args[0]).getMessage())));
    }

    @Subcommand("leave")
    @Conditions("is-player")
    public void onGuildLeaveCommand(Player player) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format("&eYou are not in a guild!"));
            return;
        }

        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();

        if (guild.getMember(player.getUniqueId()).getRank() == GuildRank.OWNER) {
            player.sendMessage(ColorUtil.format("&eYou cannot leave the guild because you are the owner! To disband guild or transfer ownership, use those commands."));
            return;
        }

        PlayerGuildDataUtil.setGuildForPlayer("None", player.getUniqueId().toString());
        guild.removeMember(player.getUniqueId());
        player.sendMessage(ColorUtil.format("&eYou have left your guild."));

        GuildUtil.getPlayerCache().put(player.getUniqueId(), null);
        guildData.queueToSave();
        Bukkit.getServer().getPluginManager().callEvent(new GuildMemberLeaveEvent(guild, player.getUniqueId()));

        this.transferOwnership.remove(player.getUniqueId());

        this.disbanding.remove(player.getUniqueId());

        if (GuildBankUtil.isViewingBank(player.getUniqueId())) {
            GuildBankUtil.close(player);
        }
    }

    @Subcommand("confirm")
    @Conditions("is-player")
    public void onGuildConfirmCommand(Player player, String[] args) {
        String prefix;
        GuildData guildData = null;
        Guild guild = null;
        if (!(GuildUtil.getPlayerCache().get(player.getUniqueId()) == null && args.length > 0
                && (args[0].equalsIgnoreCase("confirm") || args[0].equalsIgnoreCase("cancel"))
                && Plugin.getPlayersCreatingGuild().contains(player.getUniqueId())
                && GuildUtil.getPlayerCache().get(player.getUniqueId()) == null)) {
            prefix = GuildUtil.getPlayerCache().get(player.getUniqueId());
            guildData = GuildUtil.getGuildData(prefix);
            guild = guildData.getData();
        }

        if (Plugin.getPlayersCreatingGuild().contains(player.getUniqueId())) {
            this.createGuild(player, args);
        } else if (this.transferOwnership.containsKey(player.getUniqueId())) {
            this.transferOwnership(player, guild, guildData);
        } else if (this.disbanding.contains(player.getUniqueId())) {
            this.disbandGuild(player, guild, guildData);
        } else {
            player.sendMessage(ColorUtil.format("&eYou have nothing to confirm."));
        }
    }

    @Subcommand("cancel")
    @Conditions("is-player")
    public void onGuildCancelCommand(Player player) {
        if (Plugin.getPlayersCreatingGuild().contains(player.getUniqueId())) {
            player.sendMessage(ColorUtil.format("&eCanceled creating guild."));
            Plugin.getPlayersCreatingGuild().remove(player.getUniqueId());
            return;
        }

        if (this.transferOwnership.containsKey(player.getUniqueId())) {
            player.sendMessage(ColorUtil.format("&eCanceled owner transfership."));
            this.transferOwnership.remove(player.getUniqueId());
            return;
        }

        if (this.disbanding.contains(player.getUniqueId())) {
            player.sendMessage(ColorUtil.format("&eCanceled this.disbanding of the guild."));
            this.disbanding.remove(player.getUniqueId());
        }
    }

    @Subcommand("disband")
    @Conditions("is-player")
    public void onGuildDisbandCommand(Player player) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format("&eYou are not in a guild!"));
            return;
        }

        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();

        if (guild.getMember(player.getUniqueId()).getRank() != GuildRank.OWNER) {
            player.sendMessage(ColorUtil.format("&eYou must be the guild owner to use this command."));
            return;
        }

        player.sendMessage(ColorUtil.format("&eType /guild confirm if you with to proceed with this.disbanding the guild, or /guild cancel to cancel this."));
        this.disbanding.add(player.getUniqueId());
        this.transferOwnership.remove(player.getUniqueId());
    }

    @Subcommand("accept")
    @Conditions("is-player")
    public void onGuildAcceptCommand(Player player) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) != null) {
            player.sendMessage(ColorUtil.format("&eYou cannot use this command since you are in a guild."));
            return;
        }

        if (!this.invites.containsKey(player.getUniqueId())) {
            player.sendMessage(ColorUtil.format("&eYou don't have any pending invitations."));
            return;
        }

        Plugin.getPlayersCreatingGuild().remove(player.getUniqueId());

        GuildData guildData = GuildUtil.getGuildData(this.invites.get(player.getUniqueId()));
        Guild guild = guildData.getData();
        guild.getMembers().add(new GuildMember(player.getUniqueId(), GuildRank.RECRUIT, 0, player.getName()));
        PlayerGuildDataUtil.setGuildForPlayer(guild.getGuildName(), player.getUniqueId().toString());
        player.sendMessage(ColorUtil.format("&eYou have accepted the guild invitation."));

        guildData.queueToSave();
        GuildUtil.getPlayerCache().put(player.getUniqueId(), guild.getGuildPrefix());
        Bukkit.getServer().getPluginManager().callEvent(new GuildInvitationAcceptedEvent(guild, player.getUniqueId(), this.invites.get(player.getUniqueId())));
        this.invites.remove(player.getUniqueId());
    }

    @Subcommand("decline")
    @Conditions("is-player")
    public void onGuildDeclineCommand(Player player) {
        if (!this.invites.containsKey(player.getUniqueId())) {
            player.sendMessage(ColorUtil.format("&eYou don't have any pending invitations."));
            return;
        }

        player.sendMessage(ColorUtil.format("&eYou have decline the guild invitation."));
        Guild guild = GuildUtil.getGuildData(this.invites.get(player.getUniqueId())).getData();
        Bukkit.getServer().getPluginManager().callEvent(new GuildInvitationDeclinedEvent(guild, player.getUniqueId(), this.invites.get(player.getUniqueId())));
        this.invites.remove(player.getUniqueId());
    }

    @Subcommand("banner")
    @Conditions("is-player")
    public void onGuildBannerCommand(Player player) {
        Guild guild = GuildUtil.getGuildData(player.getUniqueId()).getData();
        if (guild == null) {
            player.sendMessage(ColorUtil.format("&eYou are not in a guild!"));
            return;
        }

        if (guild.getMember(player.getUniqueId()).getRank() != GuildRank.OWNER) {
            player.sendMessage(ColorUtil.format("&6You must be the owner of your guild to execute this command!"));
            return;
        }

        GuildBannerUI ui = new GuildBannerUI(guild);
        player.sendMessage(ColorUtil.format("&6Initializing user interface..."));
        player.openInventory(ui.getInventory());
        ui.openColorMenu();
        //maybe we need to store something in cache???
    }

    private void sendHelpMessage(CommandSender sender) {
        String[] messages = new String[]{"&6Guild Commands:",
                "&e/guild info &r- gets guild members and score.",
                "&e/guild invite &6[player] &r- this.invites a player to the guild.",
                "&e/guild set name&6/&eprefix <text> &r- sets your guild name/prefix.",
                "&e/guild kick &6[player] &r- kicks a player from the guild.",
                "&e/guild promote&6/&edemote &6[player] &r- promotes/demotes a guild member.",
                "&e/guild disband &r- disbands your guild.",
                "&e/guild transfer &6[player] &r- transfers the guild ownership to another member.",
                "&e/guild leave &r- removes you from your guild.",
                "&e/guild accept&6/&edecline &r- accepts/declines an invitation to join a guild.",
                "&e/guild confirm&6/&ecancel &r- for confirming/canceling certain actions."};
        for (String message : messages) {
            sender.sendMessage(ColorUtil.format(message));
        }
    }

    private void createGuild(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ColorUtil.format("&eTo confirm creation of your guild, type &6/guild confirm <guild-prefix> <guild-name>&e. The prefix must be of 3-6 english letters."));
            return;
        }

        if (!player.getInventory().contains(Material.GOLD_NUGGET, Plugin.GUILD_COST)) {
            player.sendMessage(ColorUtil.format("&ePut " + Plugin.GUILD_COST + " coins in your inventory, and speak with the guild herald again."));
            Plugin.getPlayersCreatingGuild().remove(player.getUniqueId());
            return;
        }

        GuildCreationResult result = RunicGuildsAPI.createGuild(player.getUniqueId(), this.combineArgs(args, 1), args[0], false);
        if (result != GuildCreationResult.SUCCESSFUL) {
            player.sendMessage(ColorUtil.format("&e" + result.getMessage() + " Try again, or type &6/guild cancel&e."));
            return;
        }

        PlayerGuildDataUtil.setGuildForPlayer(GuildUtil.getGuildData(player.getUniqueId()).getData().getGuildName(), player.getUniqueId().toString());
        ItemRemover.takeItem(player, Material.GOLD_NUGGET, Plugin.GUILD_COST);
        Plugin.getPlayersCreatingGuild().remove(player.getUniqueId());
        player.sendMessage(ColorUtil.format("&e" + result.getMessage()));
    }

    private void transferOwnership(Player player, Guild guild, GuildData guildData) {
        this.transferOwnership.get(guild.getMember(this.transferOwnership.get(player.getUniqueId())));
        player.sendMessage(ColorUtil.format("&eSuccessfully transferred guild ownership. You have been demoted to officer."));

        guildData.queueToSave();
        Bukkit.getServer().getPluginManager().callEvent(new GuildOwnershipTransferedEvent(guild, this.transferOwnership.get(player.getUniqueId()), player.getUniqueId()));
        this.transferOwnership.remove(player.getUniqueId());
    }

    private void disbandGuild(Player player, Guild guild, GuildData guildData) {
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
        player.sendMessage(ColorUtil.format("&eSuccessfully disbanded guild."));
        this.disbanding.remove(player.getUniqueId());
    }

    public Map<UUID, UUID> getTransferOwnership() {
        return this.transferOwnership;
    }

    public Map<UUID, UUID> getInvites() {
        return this.invites;
    }

    public Set<UUID> getDisbanding() {
        return this.disbanding;
    }

    private String combineArgs(String[] args, int start) {
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
