package com.runicrealms.runicguilds.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.data.GuildUtil;
import com.runicrealms.runicguilds.gui.GuildBannerUI;
import com.runicrealms.runicguilds.guilds.Guild;
import org.bukkit.entity.Player;

public class GuildCommand extends BaseCommand {

    private final String prefix = ColorUtil.format("&r&6&lGuilds »&r ");

    public GuildCommand() {
        //placeholder
    }

    @Default
    @CatchUnknown
    public void onGuildHelpCommand(Player player) {
        String[] lines = new String[]{"&6Guild Commands:",
                "&e/guild info &r- gets guild members, score and guild experience",
                "&e/guild invite &6[player] &r- invites a player to the guild.",
                "&e/guild bank &r- opens your guild bank.",
                "&e/guild set name&6/&eprefix <text> &r- sets your guild name/prefix.",
                "&e/guild kick &6[player] &r- kicks a player from the guild.",
                "&e/guild promote&6/&edemote &6[player] &r- promotes/demotes a guild member.",
                "&e/guild disband &r- disbands your guild.",
                "&e/guild transfer &6[player] &r- transfers the guild ownership to another member.",
                "&e/guild leave &r- removes you from your guild.",
                "&e/guild accept&6/&edecline &r- accepts/declines an invitation to join a guild.",
                "&e/guild confirm&6/&ecancel &r- for confirming/canceling certain actions.",
                "&e/guild banner &r- for making a custom guild banner."};
        for (String line : lines) {
            player.sendMessage(ColorUtil.format(line));
        }
    }

    @Subcommand("info")
    @Conditions("is-player")
    public void onGuildInfoCommand(Player player) {
        //placeholder
    }

    @Subcommand("invite")
    @Syntax("<player>")
    @CommandCompletion("@guild-invite")
    @Conditions("is-player")
    public void onGuildInviteCommand(Player player, String[] args) {
        //placeholder
    }

    @Subcommand("bank")
    @Conditions("is-player")
    public void onGuildBankCommand(Player player) {
        //placeholder
    }
    
    /*
    @Syntax("<player>")
    @CommandCompletion("@guild-invite")
    
     */
    @Subcommand("set") //no clue has more then one arg
    @Conditions("is-player")
    public void onGuildSetNameCommand(Player player) {
        //placeholder
    }

    @Subcommand("kick")
    @Syntax("<player>")
    @CommandCompletion("@guild-kick")
    @Conditions("is-player")
    public void onGuildKickCommand(Player player, String[] args) {
        //placeholder
    }

    @Subcommand("promote")
    @Syntax("<player>")
    @CommandCompletion("@guild-promote")
    @Conditions("is-player")
    public void onGuildPromoteCommand(Player player, String[] args) {
        //placeholder
    }

    @Subcommand("demote")
    @Syntax("<player>")
    @CommandCompletion("@guild-demote")
    @Conditions("is-player")
    public void onGuildDemoteCommand(Player player, String[] args) {
        //placeholder
    }

    @Subcommand("disband")
    @Conditions("is-player")
    public void onGuildDisbandCommand(Player player) {
        //placeholder
    }

    @Subcommand("transfer")
    @Syntax("<player>")
    @CommandCompletion("@guild-transfer")
    @Conditions("is-player")
    public void onGuildTransferCommand(Player player, String[] args) {
        //placeholder
    }

    @Subcommand("leave")
    @Conditions("is-player")
    public void onGuildLeaveCommand(Player player) {
        //placeholder
    }

    @Subcommand("accept")
    @Conditions("is-player")
    public void onGuildAcceptCommand(Player player) {
        //placeholder
    }

    @Subcommand("decline")
    @Conditions("is-player")
    public void onGuildDeclineCommand(Player player) {
        //placeholder
    }

    @Subcommand("confirm")
    @Conditions("is-player")
    public void onGuildConfirmCommand(Player player) {
        //placeholder
    }

    @Subcommand("cancel")
    @Conditions("is-player")
    public void onGuildCancelCommand(Player player) {
        //placeholder
    }

    @Subcommand("banner")
    @Conditions("is-player")
    public void onGuildBannerCommand(Player player) {
        Guild guild = GuildUtil.getGuildData(player.getUniqueId()).getData();
        if (guild == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "&cYou must be in a guild to execute this command!"));
            return;
        }

        if (!player.getUniqueId().equals(guild.getOwner().getUUID())) {
            player.sendMessage(ColorUtil.format(this.prefix + "&cYou must be the owner of your guild to execute this command!"));
            return;
        }

        GuildBannerUI ui = new GuildBannerUI(guild);
        player.sendMessage(ColorUtil.format(this.prefix + "&aInitializing user interface..."));
        player.openInventory(ui.getInventory());
        ui.openColorMenu();
        //maybe we need to store something in cache???
    }
}
