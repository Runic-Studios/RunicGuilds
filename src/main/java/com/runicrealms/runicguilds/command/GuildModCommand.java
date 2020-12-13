package com.runicrealms.runicguilds.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.api.GiveGuildEXPEvent;
import com.runicrealms.runicguilds.data.GuildUtil;
import com.runicrealms.runicguilds.guilds.GuildEXPSource;
import com.runicrealms.runicguilds.guilds.ForceLoadBanners;
import com.runicrealms.runicguilds.guilds.Guild;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuildModCommand extends BaseCommand {

    private final String prefix = ColorUtil.format("&r&6&lGuilds »&r ");

    public GuildModCommand() {
        //placeholder
    }

    @Default
    @CatchUnknown
    public void onGuildHelpCommand(Player player) {
        String[] lines = new String[]{"&6Guild Moderator Commands:",
                "&e/guildmod disband &6[prefix] &r- force disbands a guild.",
                "&e/guildmod kick &6[player] &r- force kicks a player from their guild.",
                "&e/guildmod reset &6[player] &r- resets a player's guild score and guild experience.",
                "&e/guildmod create &6[owner] [name] [prefix] &r- creates a guild. &cThis is only for operators.",
                "&e/guildmod bank &6[prefix] &r- views another guild's bank",
                "&e/guildmod giveexp &6[player] [reason] [amount] &r- give a player guild experience"};
        for (String line : lines) {
            player.sendMessage(ColorUtil.format(line));
        }
    }

    @Subcommand("disband")
    @Syntax("<player>")
    @CommandPermission("runicadmin.guilds.disband")
    @CommandCompletion("@guildmod-disband")
    public void onGuildModDisbandCommand(Player player, String[] args) {
        //placeholder
    }

    @Subcommand("kick")
    @Syntax("<player>")
    @CommandPermission("runicadmin.guilds.kick")
    @CommandCompletion("@guildmod-kick")
    public void onGuildModKickCommand(Player player, String[] args) {
        //placeholder
    }

    @Subcommand("reset")
    @Syntax("<player>")
    @CommandPermission("runicadmin.guilds.reset")
    @CommandCompletion("@guildmod-reset")
    public void onGuildModResetCommand(Player player, String[] args) {
        //placeholder
    }

    //no clue what im doing, has more then one arg
    @Subcommand("create")
    @Syntax("<player>")
    //check if op here
    @CommandCompletion("@guildmod-create")
    public void onGuildModCreateCommand(Player player, String[] args) {
        //placeholder
    }

    @Subcommand("bank")
    @Syntax("<prefix>")
    @CommandPermission("runicadmin.guilds.bank")
    @CommandCompletion("@guildmod-bank")
    @Conditions("is-player")
    public void onGuildModBankCommand(Player player, String[] args) {
        //placeholder
    }

    @Subcommand("giveexp")
    @Syntax("<player> <reason> <amount>")
    @CommandPermission("runicadmin.guilds.giveexp")
    @CommandCompletion("@guildmod-giveexp")
    public void onGuildModGiveEXPCommand(CommandSender sender, String[] args) { //made it CommandSender because it might be console (fix if wrong please)
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ColorUtil.format(this.prefix + "&cYou must enter a valid player this command!"));
            return;
        }

        Guild guild = GuildUtil.getGuildData(target.getUniqueId()).getData();
        if (guild == null) {
            sender.sendMessage(ColorUtil.format(this.prefix + "&cThe targeted player must be in a guild to execute this command!"));
            return;
        }

        GuildEXPSource source;
        if (args[1].equalsIgnoreCase("Kill")) {
            source = GuildEXPSource.KILL;
        } else if (args[1].equalsIgnoreCase("Quest")) {
            source = GuildEXPSource.QUEST;
        } else if (args[1].equalsIgnoreCase("Brawl")) {
            source = GuildEXPSource.BRAWL;
        } else if (args[1].equalsIgnoreCase("Other")) {
            source = GuildEXPSource.OTHER;
        } else {
            sender.sendMessage(ColorUtil.format(this.prefix + "&cYou have entered an invalid source, here is a list of sources that you can use to execute this command!"));
            sender.sendMessage(ColorUtil.format(this.prefix + "&cHere are the valid sources: Kill, Quest, Brawl, Other"));
            sender.sendMessage(ColorUtil.format(this.prefix + "&c/giveguildexp <player> <source> <amount>"));
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ColorUtil.format(this.prefix + "&cYou have entered an invalid integer, the argument used must be an integer to execute this command!"));
            sender.sendMessage(ColorUtil.format(this.prefix + "&c/giveguildexp <player> <source> <amount>"));
            return;
        }

        GiveGuildEXPEvent event = new GiveGuildEXPEvent(guild, amount, source);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        guild.setGuildEXP(guild.getGuildEXP() + amount);
        //save to cache somewhere maybe?
        target.sendMessage(ColorUtil.format("&r&aYou received " + amount + " guild experience!"));
    }

    @Subcommand("forceloadbanners")
    @CommandPermission("runicadmin.guilds.forceloadbanners")
    public void onGuildModGiveEXPCommand(CommandSender sender) { //made it CommandSender because it might be console (fix if wrong please)
        new ForceLoadBanners().run();
    }
}
