package com.runicrealms.runicguilds.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.api.GuildDisbandEvent;
import com.runicrealms.runicguilds.api.GuildMemberKickedEvent;
import com.runicrealms.runicguilds.config.GuildUtil;
import com.runicrealms.runicguilds.guilds.Guild;

public class GuildModCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("create")) {
				if (sender.isOp()) {
					if (args.length == 4) {
						if (GuildUtil.getOfflinePlayerUUID(args[1]) != null) {
							sendMessage(sender, "&e" + GuildUtil.createGuild(GuildUtil.getOfflinePlayerUUID(args[1]), args[2], args[3]).getMessage());
						} else {
							sendMessage(sender, "&eThat player is not online.");
						}
					} else {
						sendHelpMessage(sender);
					}
				} else {
					sendMessage(sender, "&eYou do not have permission to do this.");
				}
			} else if (args[0].equalsIgnoreCase("disband")) {
				if (sender.hasPermission(Plugin.getInstance().getConfig().getString("permissions.guildmod-disband"))) {
					if (args.length == 2) {
						if (GuildUtil.getGuildFiles().containsKey(args[1])) {
							Guild guild = GuildUtil.getGuild(args[1]);
							if (GuildCommand.getTransferOwnership().containsKey(guild.getOwner().getUUID())) {
								GuildCommand.getTransferOwnership().remove(guild.getOwner().getUUID());
							}
							if (GuildCommand.getDisbanding().contains(guild.getOwner().getUUID())) {
								GuildCommand.getDisbanding().remove(guild.getOwner().getUUID());
							}
							Bukkit.getServer().getPluginManager().callEvent(new GuildDisbandEvent(guild, null, true));
							GuildUtil.getGuildFiles().get(args[1]).deleteFile();
							GuildUtil.removeGuild(GuildUtil.getGuild(args[1]));
							sendMessage(sender, "&eSuccessfully disbanded guild.");
						} else {
							sendMessage(sender, "&eThat guild does not exist.");
						}
					} else {
						sendHelpMessage(sender);
					}
				} else {
					sendMessage(sender, "&eYou do not have permission to do this.");
				}
			} else if (args[0].equalsIgnoreCase("kick")) {
				if (sender.hasPermission(Plugin.getInstance().getConfig().getString("permissions.guildmod-kick"))) {
					if (args.length == 2) {
						if (GuildUtil.getGuild(args[1]) != null) {
							Guild guild = GuildUtil.getGuild(args[1]);
							if (!guild.getOwner().getUUID().toString().equalsIgnoreCase(GuildUtil.getOfflinePlayerUUID(args[1]).toString())) {
								guild.removeMember(GuildUtil.getOfflinePlayerUUID(args[1]));
								GuildUtil.saveGuild(guild);
								Bukkit.getServer().getPluginManager().callEvent(new GuildMemberKickedEvent(guild, GuildUtil.getOfflinePlayerUUID(args[1]), null, true));
								sendMessage(sender, "&eSuccessfully kicked guild member.");
							} else {
								sendMessage(sender, "&eThat user is the guild owner. To disband the guild, use /guildmod disband [prefix].");
							}
							sendMessage(sender, "&ePlayer has been kicked.");
						} else {
							sendMessage(sender, "&eThat player is not in a guild.");
						}
					} else {
						sendHelpMessage(sender);
					}
				} else {
					sendMessage(sender, "&eYou do not have permission to do this.");
				}
			} else if (args[0].equalsIgnoreCase("reset")) {
				if (sender.hasPermission(Plugin.getInstance().getConfig().getString("permissions.guildmod-reset"))) {
					if (args.length == 2) {
						if (GuildUtil.getGuild(args[1]) != null) {
							Guild guild = GuildUtil.getGuild(args[1]);
							guild.setPlayerScore(GuildUtil.getOfflinePlayerUUID(args[1]), 0);
							GuildUtil.saveGuild(guild);
							sendMessage(sender, "&eSuccessfully reset guild member score.");
						} else {
							sendMessage(sender, "&eThat player is not in a guild.");
						}
					} else {
						sendHelpMessage(sender);
					}
				} else {
					sendMessage(sender, "&eYou do not have permission to do this.");
				}
			} else {
				sendHelpMessage(sender);
			}
		} else if (sender.hasPermission(Plugin.getInstance().getConfig().getString("permissions.guildmod-disband")) ||
				sender.hasPermission(Plugin.getInstance().getConfig().getString("permissions.guildmod-kick")) ||
				sender.hasPermission(Plugin.getInstance().getConfig().getString("permissions.guildmod-reset"))) {
			sendHelpMessage(sender);
		} else {
			sendMessage(sender, "&eYou do not have permission to do this.");
		}
		return true;
	}

	private static void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
	}

	private static void sendHelpMessage(CommandSender sender) {
		sendMessage(sender, "&6Guild Commands:");
		sendMessage(sender, "&e/guildmod disband &6[prefix] &r- force disbands a guild.");
		sendMessage(sender, "&e/guildmod kick &6[player] &r- force kicks a player from their guild.");
		sendMessage(sender, "&e/guildmod reset &6[player] &r- resets a player's guild score.");
		sendMessage(sender, "&e/guildmod create &6[owner] [name] [prefix] &r- creates a guild. &cThis is only for operators.");
	}

}
