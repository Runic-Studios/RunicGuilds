package com.runicrealms.runicguilds.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.runicrealms.runicguilds.config.GuildUtil;

public class GuildCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (GuildUtil.getPlayerCache().containsKey(player.getUniqueId())) {
				if (args.length == 0) {
					sendHelpMessage(player);
				} else {
					if (args[0].equalsIgnoreCase("invite")) {
						if (GuildUtil.getGuild(player.getUniqueId()).hasMinRank(player.getUniqueId(), 3)) {
							if (args.length == 2) {
								if (Bukkit.getPlayerExact(args[1]) != null) {
									if (GuildUtil.getPlayerCache().get(player.getUniqueId()) != null) {
										// TODO
									} else {
										sendMessage(player, "&eThat player is already in a guild.");
									}
								} else {
									sendMessage(player, "&eThat player is not online.");
								}
							}
						} else {
							sendMessage(player, "&eYou must be of rank recruiter or higher to invite other players.");
						}
					} else if (args[0].equalsIgnoreCase("kick")) {
						if (GuildUtil.getGuild(player.getUniqueId()).hasMinRank(player.getUniqueId(), 2)) {
							if (args.length == 2) {
								// TODO - prevent lower ranks from kicking higher ranks
								if (Bukkit.getPlayerExact(args[1]) != null) {
									if (GuildUtil.getPlayerCache().get(player.getUniqueId()).equalsIgnoreCase(GuildUtil.getPlayerCache().get(player.getUniqueId()))) {
										// TODO
									} else {
										sendMessage(player, "&eThat player is not in your guild.");
									}
								} else {
									sendMessage(player, "&eThat player is not online.");
								}
							}
						} else {
							sendMessage(player, "&eYou must be of rank officer or higher to kick other players!");
						}
					} else if (args[0].equalsIgnoreCase("promote")) {
						// TODO
					} else if (args[0].equalsIgnoreCase("transfer")) {
						// TODO
					} else {
						sendHelpMessage(player);
					}
				}
			} else {
				sendMessage(player, "&eYou must be in a guild to use this command!");
			}
		}
		return true;
	}

	private void sendHelpMessage(Player player) {
		// TODO
	}

	private void sendMessage(Player player, String message) {
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
	}

}