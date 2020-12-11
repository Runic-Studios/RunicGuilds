package com.runicrealms.plugin.runicguildslevelingtest.cmds;

import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.api.RunicGuildsAPI;
import com.runicrealms.runicguilds.gui.GuildBannerUI;
import com.runicrealms.runicguilds.guilds.Guild;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlaceholderMakeGuildBannerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.format("&r&cOnly players can execute this command!"));
            return false;
        }


        if (args.length != 0) {
            sender.sendMessage(ColorUtil.format("&r&cYou have used improper arguments to execute this command!"));
            sender.sendMessage(ColorUtil.format("&r&c/makeguildbanner"));
            return false;
        }

        Player player = (Player) sender;

        if (!RunicGuildsAPI.isInGuild(player.getUniqueId())) {
            player.sendMessage(ColorUtil.format("&r&cYou must be in a guild to execute this command!"));
            player.sendMessage(ColorUtil.format("&r&c/makeguildbanner"));
            return false;
        }

        Guild guild = RunicGuildsAPI.getGuild(player.getUniqueId());

        if (guild.getOwner().getUUID().toString().equals(player.getUniqueId())) {
            player.sendMessage(ColorUtil.format("&r&cYou must be the owner of the guild to execute this command!"));
            return false;
        }

        GuildBannerUI ui = new GuildBannerUI(guild);

        player.sendMessage(ColorUtil.format("&r&aLets make a banner!"));
        player.openInventory(ui.getInventory());
        ui.openColorMenu();
        return true;
    }

}
