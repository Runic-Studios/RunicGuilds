package com.runicrealms.runicguilds.command.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.runicrealms.plugin.item.util.ItemRemover;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.CurrencyUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.api.RunicGuildsAPI;
import com.runicrealms.runicguilds.api.event.*;
import com.runicrealms.runicguilds.command.GuildCommandMapManager;
import com.runicrealms.runicguilds.gui.GuildBankUtil;
import com.runicrealms.runicguilds.gui.GuildBannerUI;
import com.runicrealms.runicguilds.gui.GuildInfoGUI;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.GuildCreationResult;
import com.runicrealms.runicguilds.guild.GuildMember;
import com.runicrealms.runicguilds.guild.GuildRank;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.util.GuildUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@CommandAlias("guild")
public class GuildCommand extends BaseCommand {

    private final String prefix = "&r&6&lGuilds »&r &e";

    @Default
    @CatchUnknown
    public void onGuildHelpCommand(CommandSender sender) {
        this.sendHelpMessage(sender);
    }

    @Subcommand("info")
    @Conditions("is-player")
    @CommandCompletion("@nothing")
    public void onGuildInfoCommand(Player player) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "You are not in a guild!"));
            return;
        }
        Guild guild = GuildUtil.getGuildData(player.getUniqueId()).getData();
        player.openInventory(new GuildInfoGUI(player, guild).getInventory());
    }

    @Subcommand("invite")
    @Syntax("<player>")
    @CommandCompletion("@players @nothing")
    @Conditions("is-player")
    public void onGuildInviteCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(player);
            return;
        }

        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "You are not in a guild!"));
            return;
        }

        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();
        if (!guild.hasMinRank(player.getUniqueId(), GuildRank.RECRUITER)) {
            player.sendMessage(ColorUtil.format(this.prefix + "You must be of rank recruiter or higher to invite other players."));
            return;
        }

        if (guild.getMembers().size() >= guild.getGuildStage().getMaxMembers()) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have reached your guild stages maximum amount of members."));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "That player is not online."));
            return;
        }

        if (GuildUtil.getPlayerCache().get(target.getUniqueId()) != null) {
            player.sendMessage(ColorUtil.format(this.prefix + "That player is already in a guild."));
            return;
        }

        target.sendMessage(ColorUtil.format(this.prefix + "You have been invited to join the guild " + guild.getGuildName() + " by " + player.getName() + ". Type /guild accept to accept the invitation, or /guild decline to deny the invitation."));
        player.sendMessage(ColorUtil.format(this.prefix + "You have invited a player to the guild. An invitation has been sent."));
        GuildCommandMapManager.getInvites().put(target.getUniqueId(), player.getUniqueId());
        // guildData.queueToSave();
        Bukkit.getServer().getPluginManager().callEvent(new GuildMemberInvitedEvent(guild, target.getUniqueId(), player.getUniqueId()));
    }

    @Subcommand("settings bank")
    @Conditions("is-player")
    @CommandCompletion("Recruit|Member|Recruiter|Officer yes|no")
    public void onGuildSettingsBankCommand(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(player);
            return;
        }

        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "You are not in a guild!"));
            return;
        }

        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();

        GuildRank rank = GuildRank.getByIdentifier(args[0]);
        if (rank == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "That isn't a valid guild rank!"));
            return;
        }

        if (rank == GuildRank.OWNER) {
            player.sendMessage(ColorUtil.format(this.prefix + "You cannot deny/allow bank access to the guild owner!"));
            return;
        }

        if (args[1].equalsIgnoreCase("yes") || args[1].equalsIgnoreCase("true")) {
            guild.setBankAccess(rank, true);
            // guildData.queueToSave();
            player.sendMessage(ColorUtil.format(this.prefix + "Updated guild bank permissions."));
        } else if (args[1].equalsIgnoreCase("no") || args[1].equalsIgnoreCase("false")) {
            guild.setBankAccess(rank, false);
            // guildData.queueToSave();
            player.sendMessage(ColorUtil.format(this.prefix + "Updated guild bank permissions."));
        } else {
            player.sendMessage(ColorUtil.format(this.prefix + "Please enter \"yes\" or \"no\"."));
        }
    }

    @Subcommand("bank")
    @Conditions("is-player|is-op")
    public void onGuildBankCommand(Player player) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "You are not in a guild!"));
            return;
        }
        GuildBankUtil.open(player, 1);
    }

    @Subcommand("kick")
    @Syntax("<player>")
    @CommandCompletion("@players @nothing")
    @Conditions("is-player")
    public void onGuildKickCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(player);
            return;
        }

        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "You are not in a guild!"));
            return;
        }

        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();

        if (!guild.hasMinRank(player.getUniqueId(), GuildRank.OFFICER)) {
            player.sendMessage(ColorUtil.format(this.prefix + "You must be of rank officer or higher to kick other players."));
            return;
        }

        UUID otherPlayer = GuildUtil.getOfflinePlayerUUID(args[0]);
        if (otherPlayer.toString().equalsIgnoreCase(player.getUniqueId().toString())) {
            player.sendMessage(ColorUtil.format(this.prefix + "You can't remove yourself from the guild. To leave, type /guild leave."));
            return;
        }

        if (!guild.isInGuild(args[0])) {
            player.sendMessage(ColorUtil.format(this.prefix + "That player is not in your guild."));
            return;
        }

        if (guild.getMember(player.getUniqueId()).getRank().getRankNumber() >= guild.getMember(otherPlayer).getRank().getRankNumber()) {
            player.sendMessage(ColorUtil.format(this.prefix + "You can only kick players that are of lower rank than you."));
            return;
        }

        guild.removeMember(otherPlayer);
        GuildData.setGuildForPlayer("None", otherPlayer.toString());
        player.sendMessage(ColorUtil.format(this.prefix + "Removed player from the guild!"));
        if (GuildUtil.getPlayerCache().containsKey(otherPlayer)) {
            GuildUtil.getPlayerCache().put(otherPlayer, null);
        }

        // guildData.queueToSave();
        Player target = Bukkit.getPlayerExact(args[0]);
        Bukkit.getServer().getPluginManager().callEvent(new GuildMemberKickedEvent(guild, target.getUniqueId(), player.getUniqueId(), false));
        if (GuildBankUtil.isViewingBank(otherPlayer) && target != null) {
            GuildBankUtil.close(target);
        }
    }

    @Subcommand("promote")
    @Syntax("<player>")
    @CommandCompletion("@players @nothing")
    @Conditions("is-player")
    public void onGuildPromoteCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(player);
            return;
        }

        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "You are not in a guild!"));
            return;
        }

        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();

        if (!guild.hasMinRank(player.getUniqueId(), GuildRank.OFFICER)) {
            player.sendMessage(ColorUtil.format(this.prefix + "You must be of rank officer or higher to promote other players."));
            return;
        }

        if (!guild.isInGuild(args[0])) {
            player.sendMessage(ColorUtil.format(this.prefix + "That player is not in your guild."));
            return;
        }

        GuildMember member = null;
        for (GuildMember target : guild.getMembers()) {
            if (target.getLastKnownName().equalsIgnoreCase(args[0])) {
                member = target;
            }
        }

        if (member == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "That player is not in your guild!"));
            return;
        }

        if (member.getRank().getRankNumber() <= guild.getMember(player.getUniqueId()).getRank().getRankNumber() && member.getRank() != GuildRank.OFFICER) {
            if (member.getRank() == GuildRank.OFFICER) {
                player.sendMessage(ColorUtil.format(this.prefix + "You cannot promote another player to owner. To transfer guild ownership, use /guild transfer."));
            } else {
                player.sendMessage(ColorUtil.format(this.prefix + "You can only promote members that are under your rank."));
            }
            return;
        }

        member.setRank(GuildRank.getByNumber(member.getRank().getRankNumber() - 1));
        player.sendMessage(ColorUtil.format(this.prefix + member.getLastKnownName() + " has been promoted."));
        // guildData.queueToSave();
        Bukkit.getServer().getPluginManager().callEvent(new GuildMemberPromotedEvent(guild, member.getUUID(), player.getUniqueId()));
    }

    @Subcommand("demote")
    @Syntax("<player>")
    @CommandCompletion("@players @nothing")
    @Conditions("is-player")
    public void onGuildDemoteCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(player);
            return;
        }

        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "You are not in a guild!"));
            return;
        }

        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();

        if (!guild.hasMinRank(player.getUniqueId(), GuildRank.OFFICER)) {
            player.sendMessage(ColorUtil.format(this.prefix + "You must be of rank officer or higher to demote other players."));
            return;
        }

        if (!guild.isInGuild(args[0])) {
            player.sendMessage(ColorUtil.format(this.prefix + "That player is not in your guild."));
            return;
        }

        GuildMember member = null;
        for (GuildMember target : guild.getMembers()) {
            if (target.getLastKnownName().equalsIgnoreCase(args[0])) {
                member = target;
            }
        }

        if (member == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "That player is not in your guild!"));
            return;
        }

        if (member.getRank().getRankNumber() <= guild.getMember(player.getUniqueId()).getRank().getRankNumber() &&
                member.getRank() != GuildRank.RECRUIT) {
            if (member.getRank() == GuildRank.RECRUIT) {
                player.sendMessage(ColorUtil.format(this.prefix + "You cannot demote players of the lowest guild rank."));
            } else {
                player.sendMessage(ColorUtil.format(this.prefix + "You can only demote players that are under your rank."));
            }
            return;
        }

        member.setRank(GuildRank.getByNumber(member.getRank().getRankNumber() + 1));
        player.sendMessage(ColorUtil.format(this.prefix + member.getLastKnownName() + " has been demoted."));
        // guildData.queueToSave();
        Bukkit.getServer().getPluginManager().callEvent(new GuildMemberDemotedEvent(guild, member.getUUID(), player.getUniqueId()));
    }

    @Subcommand("transfer")
    @Syntax("<player>")
    @CommandCompletion("@players @nothing")
    @Conditions("is-player")
    public void onGuildTransferCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(player);
            return;
        }

        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "You are not in a guild!"));
            return;
        }

        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();

        if (guild.getMember(player.getUniqueId()).getRank() != GuildRank.OWNER) {
            player.sendMessage(ColorUtil.format(this.prefix + "You must be the guild owner to use this command."));
            return;
        }

        if (!guild.isInGuild(args[0])) {
            player.sendMessage(ColorUtil.format(this.prefix + "That player is not in your guild."));
            return;
        }

        if (args[0].equalsIgnoreCase(player.getName())) {
            player.sendMessage(ColorUtil.format(this.prefix + "You cannot transfer ownership to yourself."));
            return;
        }

        player.sendMessage(ColorUtil.format(this.prefix + "Type /guild confirm to confirm your actions, or /guild cancel to cancel. &cWARNING - You will be demoted to officer if you confirm!"));
        GuildCommandMapManager.getTransferOwnership().put(player.getUniqueId(), GuildUtil.getOfflinePlayerUUID(args[0]));
        GuildCommandMapManager.getDisbanding().remove(player.getUniqueId());
    }

    @Subcommand("leave")
    @Conditions("is-player")
    @CommandCompletion("@nothing")
    public void onGuildLeaveCommand(Player player) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "You are not in a guild!"));
            return;
        }

        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();

        if (guild.getMember(player.getUniqueId()).getRank() == GuildRank.OWNER) {
            player.sendMessage(ColorUtil.format(this.prefix + "You cannot leave the guild because you are the owner! To disband guild or transfer ownership, use those commands."));
            return;
        }

        GuildData.setGuildForPlayer("None", player.getUniqueId().toString());
        guild.removeMember(player.getUniqueId());
        player.sendMessage(ColorUtil.format(this.prefix + "You have left your guild."));

        GuildUtil.getPlayerCache().put(player.getUniqueId(), null);
        // guildData.queueToSave();
        Bukkit.getServer().getPluginManager().callEvent(new GuildMemberLeaveEvent(guild, player.getUniqueId()));

        GuildCommandMapManager.getTransferOwnership().remove(player.getUniqueId());

        GuildCommandMapManager.getDisbanding().remove(player.getUniqueId());

        if (GuildBankUtil.isViewingBank(player.getUniqueId())) {
            GuildBankUtil.close(player);
        }
    }

    @Subcommand("confirm")
    @Conditions("is-player")
    @CommandCompletion("@nothing")
    public void onGuildConfirmCommand(Player player, String[] args) {

        if (GuildCommandMapManager.getDisbanding().contains(player.getUniqueId())) {
            // Disbanding
            String prefix = GuildUtil.getPlayerCache().get(player.getUniqueId());
            GuildData guildData = GuildUtil.getGuildData(prefix);
            Guild guild = guildData.getData();
            this.disbandGuild(player, guild, guildData);
        } else if (GuildCommandMapManager.getTransferOwnership().containsKey(player.getUniqueId())) {
            // Transferring ownership
            String prefix = GuildUtil.getPlayerCache().get(player.getUniqueId());
            GuildData guildData = GuildUtil.getGuildData(prefix);
            Guild guild = guildData.getData();
            this.transferOwnership(player, guild, guildData);
        } else if (RunicGuilds.getPlayersCreatingGuild().contains(player.getUniqueId())) {
            // Creating guild
            this.createGuild(player, args);
        } else {
            // Not confirming
            player.sendMessage(ColorUtil.format(this.prefix + "You have nothing to confirm."));
        }
    }

    @Subcommand("cancel")
    @Conditions("is-player")
    @CommandCompletion("@nothing")
    public void onGuildCancelCommand(Player player) {
        if (RunicGuilds.getPlayersCreatingGuild().contains(player.getUniqueId())) {
            player.sendMessage(ColorUtil.format(this.prefix + "Canceled creating guild."));
            RunicGuilds.getPlayersCreatingGuild().remove(player.getUniqueId());
            return;
        }

        if (GuildCommandMapManager.getTransferOwnership().containsKey(player.getUniqueId())) {
            player.sendMessage(ColorUtil.format(this.prefix + "Canceled owner transfership."));
            GuildCommandMapManager.getTransferOwnership().remove(player.getUniqueId());
            return;
        }

        if (GuildCommandMapManager.getDisbanding().contains(player.getUniqueId())) {
            player.sendMessage(ColorUtil.format(this.prefix + "Canceled disbanding of the guild."));
            GuildCommandMapManager.getDisbanding().remove(player.getUniqueId());
            return;
        }
        player.sendMessage(ColorUtil.format(this.prefix + "You have nothing to cancel."));
    }

    @Subcommand("disband")
    @Conditions("is-player")
    @CommandCompletion("@nothing")
    public void onGuildDisbandCommand(Player player) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "You are not in a guild!"));
            return;
        }

        GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
        Guild guild = guildData.getData();

        if (guild.getMember(player.getUniqueId()).getRank() != GuildRank.OWNER) {
            player.sendMessage(ColorUtil.format(this.prefix + "You must be the guild owner to use this command."));
            return;
        }

        player.sendMessage(ColorUtil.format(this.prefix + "Type /guild confirm if you with to proceed with disbanding the guild, or /guild cancel to cancel this."));
        GuildCommandMapManager.getDisbanding().add(player.getUniqueId());
        GuildCommandMapManager.getTransferOwnership().remove(player.getUniqueId());
    }

    @Subcommand("accept")
    @Conditions("is-player")
    @CommandCompletion("@nothing")
    public void onGuildAcceptCommand(Player player) {
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) != null) {
            player.sendMessage(ColorUtil.format(this.prefix + "You cannot use this command since you are in a guild."));
            return;
        }

        if (!GuildCommandMapManager.getInvites().containsKey(player.getUniqueId())) {
            player.sendMessage(ColorUtil.format(this.prefix + "You don't have any pending invitations."));
            return;
        }

        GuildData guildData = GuildUtil.getGuildData(GuildCommandMapManager.getInvites().get(player.getUniqueId()));
        Guild guild = guildData.getData();

        RunicGuilds.getPlayersCreatingGuild().remove(player.getUniqueId());

        if (guild.getMembers().size() >= guild.getGuildStage().getMaxMembers()) {
            player.sendMessage(ColorUtil.format(this.prefix + "The guild has reached the maximum amount of members for their guild stage."));
            return;
        }

        guild.getMembers().add(new GuildMember(player.getUniqueId(), GuildRank.RECRUIT, 0, player.getName()));
        GuildData.setGuildForPlayer(guild.getGuildName(), player.getUniqueId().toString());
        player.sendMessage(ColorUtil.format(this.prefix + "You have accepted the guild invitation."));

        // guildData.queueToSave();
        GuildUtil.getPlayerCache().put(player.getUniqueId(), guild.getGuildPrefix());
        Bukkit.getServer().getPluginManager().callEvent(new GuildInvitationAcceptedEvent(guild, player.getUniqueId(), GuildCommandMapManager.getInvites().get(player.getUniqueId())));
        GuildCommandMapManager.getInvites().remove(player.getUniqueId());
    }

    @Subcommand("decline")
    @Conditions("is-player")
    @CommandCompletion("@nothing")
    public void onGuildDeclineCommand(Player player) {
        if (!GuildCommandMapManager.getInvites().containsKey(player.getUniqueId())) {
            player.sendMessage(ColorUtil.format(this.prefix + "You don't have any pending invitations."));
            return;
        }

        player.sendMessage(ColorUtil.format(this.prefix + "You have decline the guild invitation."));
        Guild guild = GuildUtil.getGuildData(GuildCommandMapManager.getInvites().get(player.getUniqueId())).getData();
        Bukkit.getServer().getPluginManager().callEvent(new GuildInvitationDeclinedEvent(guild, player.getUniqueId(), GuildCommandMapManager.getInvites().get(player.getUniqueId())));
        GuildCommandMapManager.getInvites().remove(player.getUniqueId());
    }

    @Subcommand("banner")
    @Conditions("is-player")
    @CommandCompletion("@nothing")
    public void onGuildBannerCommand(Player player) {
        Guild guild = GuildUtil.getGuildData(player.getUniqueId()).getData();
        if (guild == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "You are not in a guild!"));
            return;
        }

        if (guild.getMember(player.getUniqueId()).getRank() != GuildRank.OWNER) {
            player.sendMessage(ColorUtil.format(this.prefix + "You must be the owner of your guild to execute this command!"));
            return;
        }

        if (guild.getGuildExp() < GuildStage.STAGE2.getExp()) {
            player.sendMessage(ColorUtil.format(this.prefix + "You must be at least guild stage two to execute this command!"));
            return;
        }

        GuildBannerUI ui = new GuildBannerUI(guild);
        player.sendMessage(ColorUtil.format(this.prefix + "Initializing user interface..."));
        player.openInventory(ui.getInventory());
        ui.openColorMenu();
    }

    private void sendHelpMessage(CommandSender sender) {
        String[] messages = new String[]{"&6Guild Commands:",
                "&e/guild info &r- gets guild members, score and experience.",
                "&e/guild invite &6<player> &r- invites a player to the guild.",
                "&e/guild kick &6<player> &r- kicks a player from the guild.",
                "&e/guild promote&6/&edemote &6<player> &r- promotes/demotes a guild member.",
                "&e/guild disband &r- disbands your guild.",
                "&e/guild transfer &6<player> &r- transfers the guild ownership to another member.",
                "&e/guild leave &r- removes you from your guild.",
                "&e/guild accept&6/&edecline &r- accepts/declines an invitation to join a guild.",
                "&e/guild confirm&6/&ecancel &r- for confirming/canceling certain actions.",
                "&e/guild banner &r- to make a custom guild banner."};
        for (String message : messages) {
            sender.sendMessage(ColorUtil.format(message));
        }
    }

    private void createGuild(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ColorUtil.format(this.prefix + "To confirm creation of your guild, type &6/guild confirm <guild-prefix> <guild-name>&e. The prefix must be of 3-6 english letters."));
            return;
        }

        if (!player.getInventory().contains(Material.GOLD_NUGGET, RunicGuilds.GUILD_COST)) {
            player.sendMessage(ColorUtil.format(this.prefix + "Put " + RunicGuilds.GUILD_COST + " coins in your inventory, and speak with the guild herald again."));
            RunicGuilds.getPlayersCreatingGuild().remove(player.getUniqueId());
            return;
        }

        GuildCreationResult result = RunicGuildsAPI.createGuild(player.getUniqueId(), this.combineArgs(args, 1), args[0], false);
        if (result != GuildCreationResult.SUCCESSFUL) {
            player.sendMessage(ColorUtil.format(this.prefix + result.getMessage() + " Try again, or type &6/guild cancel&e."));
            return;
        }

        GuildData.setGuildForPlayer(GuildUtil.getGuildData(player.getUniqueId()).getData().getGuildName(), player.getUniqueId().toString());
        ItemRemover.takeItem(player, CurrencyUtil.goldCoin(), RunicGuilds.GUILD_COST);
        RunicGuilds.getPlayersCreatingGuild().remove(player.getUniqueId());
        player.sendMessage(ColorUtil.format(this.prefix + result.getMessage()));
    }

    private void transferOwnership(Player player, Guild guild, GuildData guildData) {
        GuildCommandMapManager.getTransferOwnership().get(guild.getMember(GuildCommandMapManager.getTransferOwnership().get(player.getUniqueId())).getUUID());
        player.sendMessage(ColorUtil.format(this.prefix + "Successfully transferred guild ownership. You have been demoted to officer."));

        // guildData.queueToSave();
        Bukkit.getServer().getPluginManager().callEvent(new GuildOwnershipTransferedEvent(guild, GuildCommandMapManager.getTransferOwnership().get(player.getUniqueId()), player.getUniqueId()));
        GuildCommandMapManager.getTransferOwnership().remove(player.getUniqueId());
    }

    private void disbandGuild(Player player, Guild guild, GuildData guildData) {
        for (GuildMember member : guild.getMembers()) {
            GuildData.setGuildForPlayer("None", member.getUUID().toString());
            if (GuildUtil.getPlayerCache().containsKey(member.getUUID())) {
                GuildUtil.getPlayerCache().put(member.getUUID(), null);
            }
            if (GuildBankUtil.isViewingBank(member.getUUID())) {
                GuildBankUtil.close(Bukkit.getPlayer(member.getUUID()));
            }
        }

        GuildUtil.getPlayerCache().put(guild.getOwner().getUUID(), null);
        GuildData.setGuildForPlayer("None", guild.getOwner().getUUID().toString());
        if (GuildBankUtil.isViewingBank(guild.getOwner().getUUID())) {
            GuildBankUtil.close(Bukkit.getPlayer(guild.getOwner().getUUID()));
        }

        Bukkit.getServer().getPluginManager().callEvent(new GuildDisbandEvent(guild, player.getUniqueId(), false));
        guildData.deleteData();
        GuildUtil.getGuildDatas().remove(guild.getGuildPrefix());
        player.sendMessage(ColorUtil.format(this.prefix + "Successfully disbanded guild."));
        GuildCommandMapManager.getDisbanding().remove(player.getUniqueId());
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
