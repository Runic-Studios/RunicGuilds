package com.runicrealms.runicguilds.command;

import java.util.UUID;

import com.runicrealms.runicguilds.data.GuildData;
import com.runicrealms.runicguilds.data.PlayerGuildDataUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.api.GuildCreationEvent;
import com.runicrealms.runicguilds.api.GuildCreationResult;
import com.runicrealms.runicguilds.api.GuildDisbandEvent;
import com.runicrealms.runicguilds.api.GuildMemberKickedEvent;
import com.runicrealms.runicguilds.data.GuildUtil;
import com.runicrealms.runicguilds.gui.GuildBankUtil;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildMember;

public class OldGuildModCommand implements CommandExecutor {

	//REMOVE THIS HORRID THING xD -BoBo

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("create")) {
				if (sender.isOp()) {
					if (args.length == 4) {
						UUID uuid = GuildUtil.getOfflinePlayerUUID(args[1]);
						if (uuid != null) {
							GuildCreationResult result = GuildUtil.createGuild(uuid, args[2], args[3]);
							sendMessage(sender, "&e" + result.getMessage());
							if (result == GuildCreationResult.SUCCESSFUL) {
								PlayerGuildDataUtil.setGuildForPlayer(GuildUtil.getGuildData(uuid).getData().getGuildName(), uuid.toString());
								Bukkit.getServer().getPluginManager().callEvent(new GuildCreationEvent(GuildUtil.getGuildData(args[3]).getData(), true));
							}
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
						if (GuildUtil.getGuildDatas().containsKey(args[1])) {
							Guild guild = GuildUtil.getGuildData(args[1]).getData();
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
							GuildUtil.getGuildDatas().get(args[1]).deleteData();
							GuildUtil.removeGuildFromCache(GuildUtil.getGuildData(args[1]).getData());
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
						if (GuildUtil.getGuildData(GuildUtil.getOfflinePlayerUUID(args[1])).getData() != null) {
							GuildData guildData = GuildUtil.getGuildData(GuildUtil.getOfflinePlayerUUID(args[1]));
							Guild guild = guildData.getData();
							if (!guild.getOwner().getUUID().toString().equalsIgnoreCase(GuildUtil.getOfflinePlayerUUID(args[1]).toString())) {
								UUID uuid = GuildUtil.getOfflinePlayerUUID(args[1]);
								if (GuildBankUtil.isViewingBank(uuid)) {
									GuildBankUtil.close(Bukkit.getPlayer(args[1]));
								}
								if (GuildUtil.getPlayerCache().containsKey(uuid)) {
									GuildUtil.getPlayerCache().put(uuid, null);
								}
								PlayerGuildDataUtil.setGuildForPlayer("None", uuid.toString());
								guild.removeMember(uuid);
								guildData.queueToSave();
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
						UUID playerUUID = Bukkit.getPlayerExact(args[1]).getUniqueId();
						if (GuildUtil.getPlayerCache().get(playerUUID) != null) {
							GuildData guildData = GuildUtil.getGuildData(GuildUtil.getPlayerCache().get(playerUUID));
							Guild guild = guildData.getData();
							guild.setPlayerScore(GuildUtil.getOfflinePlayerUUID(args[1]), 0);
							guildData.queueToSave();
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
			} else if (args[0].equalsIgnoreCase("bank")){
				if (sender.hasPermission(Plugin.getInstance().getConfig().getString("permissions.guildmod-bank"))) {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						if (args.length == 2) {
							if (GuildUtil.getGuildData(args[1]) != null) {
								GuildBankUtil.open(player, 1, args[1]);
							} else {
								sendMessage(sender, "&eThat guild does not exist");
							}
						} else {
							sendHelpMessage(sender);
						}
					} else {
						sendMessage(sender, "&eYou must be a player to use this command!");
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
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	private static void sendHelpMessage(CommandSender sender) {
		sendMessage(sender, "&6Guild Moderator Commands:");
		sendMessage(sender, "&e/guildmod disband &6[prefix] &r- force disbands a guild.");
		sendMessage(sender, "&e/guildmod kick &6[player] &r- force kicks a player from their guild.");
		sendMessage(sender, "&e/guildmod reset &6[player] &r- resets a player's guild score.");
		sendMessage(sender, "&e/guildmod create &6[owner] [name] [prefix] &r- creates a guild. &cThis is only for operators.");
		sendMessage(sender, "&e/guildmod bank &6[prefi x] &r- views another guild's bank");
	}

}