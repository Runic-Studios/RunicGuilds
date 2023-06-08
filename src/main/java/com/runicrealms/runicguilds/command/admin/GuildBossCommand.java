package com.runicrealms.runicguilds.command.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.runicrealms.runicguilds.RunicGuilds;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

@CommandAlias("guildboss")
@Conditions("is-op")
@CommandPermission("runic.op")
public class GuildBossCommand extends BaseCommand {

    private static boolean isInt(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private static UUID uuidOrNull(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    @Default
    @CatchUnknown
    public void onCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Syntax is /guildboss distributescore <score-int> <boss-uuid>");
    }

    @Subcommand("distributescore")
    public void onDistributeScore(CommandSender sender, String[] args) {
        UUID bossUUID = args.length == 2 ? uuidOrNull(args[1]) : null;
        if (args.length != 2 || !isInt(args[0]) || bossUUID == null) {
            sender.sendMessage(ChatColor.RED + "Syntax is /guildboss distributescore <score-int> <boss-uuid>");
            return;
        }
        RunicGuilds.getBossManager().handleBossDeath(bossUUID, Integer.parseInt(args[0]));
    }

}
