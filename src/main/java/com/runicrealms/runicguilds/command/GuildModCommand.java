package com.runicrealms.runicguilds.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.api.GiveGuildEXPEvent;
import com.runicrealms.runicguilds.api.GuildCreationEvent;
import com.runicrealms.runicguilds.api.GuildCreationResult;
import com.runicrealms.runicguilds.api.GuildDisbandEvent;
import com.runicrealms.runicguilds.api.GuildMemberKickedEvent;
import com.runicrealms.runicguilds.data.GuildData;
import com.runicrealms.runicguilds.data.GuildUtil;
import com.runicrealms.runicguilds.data.PlayerGuildDataUtil;
import com.runicrealms.runicguilds.gui.GuildBankUtil;
import com.runicrealms.runicguilds.guilds.EXPSource;
import com.runicrealms.runicguilds.guilds.ForceLoadBanners;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildMember;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GuildModCommand extends BaseCommand {

    public GuildModCommand() {
        //placeholder
    }

    @Default
    @CatchUnknown
    @Conditions("is-op")
    @CommandPermission("runicguilds.help")
    public void onGuildModHelpCommand(CommandSender sender) {
        sendHelpMessage(sender);
    }

    @Subcommand("create")
    @Syntax("<owner> <name> <prefix>")
    @Conditions("is-op")
    @CommandCompletion("@players @nothing @nothing")
    public void onGuildModCreateCommand(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sendHelpMessage(sender);
            return;
        }
        UUID uuid = GuildUtil.getOfflinePlayerUUID(args[0]);
        if (uuid == null) {
            sendMessage(sender, "&eThat player is not online.");
            return;
        }
        GuildCreationResult result = GuildUtil.createGuild(uuid, args[1], args[2]);
        sendMessage(sender, "&e" + result.getMessage());
        if (result == GuildCreationResult.SUCCESSFUL) {
            PlayerGuildDataUtil.setGuildForPlayer(GuildUtil.getGuildData(uuid).getData().getGuildName(), uuid.toString());
            Bukkit.getServer().getPluginManager().callEvent(new GuildCreationEvent(GuildUtil.getGuildData(args[2]).getData(), true));
        }
    }

    @Subcommand("disband")
    @Syntax("<guild-prefix>")
    @CommandPermission("runicguilds.guildmod-disband")
    @CommandCompletion("@guilds")
    public void onGuildModDisbandCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sendHelpMessage(sender);
            return;
        }
        if (!GuildUtil.getGuildDatas().containsKey(args[0])) {
            sendMessage(sender, "&eThat guild does not exist.");
            return;
        }
        Guild guild = GuildUtil.getGuildData(args[0]).getData();
        if (OldGuildCommand.getTransferOwnership().containsKey(guild.getOwner().getUUID())) {
            OldGuildCommand.getTransferOwnership().remove(guild.getOwner().getUUID());
        }
        if (OldGuildCommand.getDisbanding().contains(guild.getOwner().getUUID())) {
            OldGuildCommand.getDisbanding().remove(guild.getOwner().getUUID());
        }
        for (GuildMember member : guild.getMembers()) {
            PlayerGuildDataUtil.setGuildForPlayer("None", member.getUUID().toString());
            if (GuildUtil.getPlayerCache().containsKey(member.getUUID())) {
                GuildUtil.getPlayerCache().put(member.getUUID(), null);
            }
            if (GuildBankUtil.isViewingBank(member.getUUID())) {
                GuildBankUtil.close(Bukkit.getPlayer(member.getUUID()));
            }
        }
        if (GuildUtil.getPlayerCache().containsKey(guild.getOwner().getUUID())) {
            GuildUtil.getPlayerCache().put(guild.getOwner().getUUID(), null);
        }
        Bukkit.getServer().getPluginManager().callEvent(new GuildDisbandEvent(guild, null, true));
        GuildUtil.getGuildDatas().get(args[0]).deleteData();
        GuildUtil.removeGuildFromCache(GuildUtil.getGuildData(args[0]).getData());
        sendMessage(sender, "&eSuccessfully disbanded guild.");
    }

    @Subcommand("kick")
    @Syntax("<player>")
    @CommandPermission("runicguilds.guildmod-kick")
    public void onGuildModKickCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sendHelpMessage(sender);
            return;
        }
        if (GuildUtil.getGuildData(GuildUtil.getOfflinePlayerUUID(args[0])).getData() == null) {
            sendMessage(sender, "&eThat player is not in a guild.");
            return;
        }
        GuildData guildData = GuildUtil.getGuildData(GuildUtil.getOfflinePlayerUUID(args[0]));
        Guild guild = guildData.getData();
        if (guild.getOwner().getUUID().toString().equalsIgnoreCase(GuildUtil.getOfflinePlayerUUID(args[0]).toString())) {
            sendMessage(sender, "&eThat user is the guild owner. To disband the guild, use /guildmod disband [prefix].");
            return;
        }
        UUID uuid = GuildUtil.getOfflinePlayerUUID(args[0]);
        if (GuildBankUtil.isViewingBank(uuid)) {
            GuildBankUtil.close(Bukkit.getPlayer(args[0]));
        }
        if (GuildUtil.getPlayerCache().containsKey(uuid)) {
            GuildUtil.getPlayerCache().put(uuid, null);
        }
        PlayerGuildDataUtil.setGuildForPlayer("None", uuid.toString());
        guild.removeMember(uuid);
        guildData.queueToSave();
        Bukkit.getServer().getPluginManager().callEvent(new GuildMemberKickedEvent(guild, GuildUtil.getOfflinePlayerUUID(args[0]), null, true));
        sendMessage(sender, "&eSuccessfully kicked guild member.");
    }

    @Subcommand("reset")
    @Syntax("<player>")
    @CommandPermission("runicguilds.guildmod-reset")
    @CommandCompletion("@guildmod-reset")
    public void onGuildModResetCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sendHelpMessage(sender);
            return;
        }
        UUID playerUUID = Bukkit.getPlayerExact(args[0]).getUniqueId();
        if (GuildUtil.getPlayerCache().get(playerUUID) == null) {
            sendMessage(sender, "&eThat player is not in a guild.");
            return;
        }
        GuildData guildData = GuildUtil.getGuildData(GuildUtil.getPlayerCache().get(playerUUID));
        Guild guild = guildData.getData();
        guild.setPlayerScore(GuildUtil.getOfflinePlayerUUID(args[0]), 0);
        guildData.queueToSave();
        sendMessage(sender, "&eSuccessfully reset guild member score.");

    }

    @Subcommand("bank")
    @Syntax("<prefix>")
    @CommandPermission("runicguilds.guildmod-bank")
    @CommandCompletion("@guildmod-bank")
    @Conditions("is-player")
    public void onGuildModBankCommand(Player player, String[] args) {
        if (args.length != 1) {
            sendHelpMessage(player);
            return;
        }
        if (GuildUtil.getGuildData(args[0]) != null) {
            GuildBankUtil.open(player, 1, args[0]);
        } else {
            sendMessage(player, "&eThat guild does not exist");
        }

    }

    @Subcommand("giveexp")
    @Syntax("<player> <reason> <amount>")
    @CommandPermission("runicguilds.guildmod-giveexp")
    @CommandCompletion("@guildmod-giveexp")
    public void onGuildModGiveEXPCommand(CommandSender sender, String[] args) { //made it CommandSender because it might be console (fix if wrong please)
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ColorUtil.format("&eYou must enter a valid player this command!"));
            return;
        }

        Guild guild = GuildUtil.getGuildData(target.getUniqueId()).getData();
        if (guild == null) {
            sender.sendMessage(ColorUtil.format("&eThe targeted player must be in a guild to execute this command!"));
            return;
        }

        EXPSource source;
        if (args[1].equalsIgnoreCase("Kill")) {
            source = EXPSource.KILL;
        } else if (args[1].equalsIgnoreCase("Quest")) {
            source = EXPSource.QUEST;
        } else if (args[1].equalsIgnoreCase("Brawl")) {
            source = EXPSource.BRAWL;
        } else if (args[1].equalsIgnoreCase("Other")) {
            source = EXPSource.OTHER;
        } else {
            sender.sendMessage(ColorUtil.format("&eYou have entered an invalid source, here is a list of sources that you can use to execute this command!"));
            sender.sendMessage(ColorUtil.format("&eHere are the valid sources: Kill, Quest, Brawl, Other"));
            sender.sendMessage(ColorUtil.format("&e/giveguildexp <player> <source> <amount>"));
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ColorUtil.format("&eYou have entered an invalid integer, the argument used must be an integer to execute this command!"));
            sender.sendMessage(ColorUtil.format("&e/giveguildexp <player> <source> <amount>"));
            return;
        }

        GiveGuildEXPEvent event = new GiveGuildEXPEvent(guild, amount, source);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        guild.setGuildEXP(guild.getGuildEXP() + amount);
        //save to cache somewhere maybe?
        target.sendMessage(ColorUtil.format("&eYou received " + amount + " guild experience!"));
    }

    @Subcommand("forceloadbanners")
    @Conditions("is-op")
    public void onGuildModGiveEXPCommand(CommandSender sender) { //made it CommandSender because it might be console (fix if wrong please)
        new ForceLoadBanners().run();
    }

    private static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private static void sendHelpMessage(CommandSender sender) {
        sendMessage(sender, "&6Guild Moderator Commands:");
        sendMessage(sender, "&e/guildmod disband &6[prefix] &r- force disbands a guild.");
        sendMessage(sender, "&e/guildmod kick &6[player] &r- force kicks a player from their guild.");
        sendMessage(sender, "&e/guildmod reset &6[player] &r- resets a player's guild score.");
        sendMessage(sender, "&e/guildmod create &6[owner] [name] [prefix] &r- creates a guild. &cThis is only for operators.");
        sendMessage(sender, "&e/guildmod bank &6[prefix] &r- views another guild's bank");
    }

}
