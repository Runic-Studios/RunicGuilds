package com.runicrealms.runicguilds.command;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.runicrealms.runicguilds.config.GuildUtil;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildRank;

public class GuildCommand implements CommandExecutor {
	
	private static Map<UUID, UUID> transferOwnership = new HashMap<UUID, UUID>();
	private static Map<String, GuildInvite> invites = new HashMap<String, GuildInvite>();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (GuildUtil.getPlayerCache().get(player.getUniqueId()) != null) {
				String prefix = GuildUtil.getPlayerCache().get(player.getUniqueId());
				Guild guild = GuildUtil.getGuild(prefix);
				if (args.length == 0) {
					sendHelpMessage(player);
				} else {
					if (args[0].equalsIgnoreCase("invite")) {
						if (guild.hasMinRank(player.getUniqueId(), 3)) {
							if (args.length == 2) {
								if (Bukkit.getPlayerExact(args[1]) != null) {
									if (GuildUtil.getPlayerCache().get(Bukkit.getPlayer(args[1]).getUniqueId()) != null) {
										// TODO
									} else {
										sendMessage(player, "&eThat player is already in a guild.");
									}
								} else {
									sendMessage(player, "&eThat player is not online.");
								}
							} else {
								sendHelpMessage(player);
							}
						} else {
							sendMessage(player, "&eYou must be of rank recruiter or higher to invite other players.");
						}
					} else if (args[0].equalsIgnoreCase("kick")) {
						if (guild.hasMinRank(player.getUniqueId(), 2)) {
							if (args.length == 2) {
								Player otherPlayer = Bukkit.getPlayerExact(args[1]);
								if (guild.isInGuild(args[1])) {
									if (guild.getMember(player.getUniqueId()).getRank().getRankNumber() > guild.getMember(otherPlayer.getUniqueId()).getRank().getRankNumber()) {
										// TODO
									} else {
										sendMessage(player, "&eYou can only kick players that are of lower rank than you.");
									}
								} else {
									sendMessage(player, "&eThat player is not in your guild.");
								}
							} else {
								sendHelpMessage(player);
							}
						} else {
							sendMessage(player, "&eYou must be of rank officer or higher to kick other players!");
						}
					} else if (args[0].equalsIgnoreCase("promote")) {
						if (guild.hasMinRank(player.getUniqueId(), 2)) {
							if (args.length == 2) {
								if (guild.isInGuild(args[1])) {
									if (guild.getMember(Bukkit.getPlayerExact(args[1]).getUniqueId()).getRank().getRankNumber() >= guild.getMember(player.getUniqueId()).getRank().getRankNumber()) {
										// TODO
									} else {
										if (guild.getMember(player.getUniqueId()).getRank() == GuildRank.OWNER) {
											sendMessage(player, "&eYou cannot promote another player to owner. To transfer guild ownership, use /guild transfer.");
										} else {
											sendMessage(player, "&eYou can only promote members that are under your rank.");
										}
									}
								} else {
									sendMessage(player, "&eThat player is not in your guild.");
								}
							} else {
								sendHelpMessage(player);
							}
						} else {
							sendMessage(player, "&eYou must be of rank officer or higher to promote other players.");
						}

					} else if (args[0].equalsIgnoreCase("transfer")) {
						if (guild.getMember(player.getUniqueId()).getRank() == GuildRank.OWNER) {
							if (args.length == 2) {
								if (guild.isInGuild(args[1])) {
									sendMessage(player, "&eType /guild confirm to confirm your actions.");
									transferOwnership.put(player.getUniqueId(), GuildUtil.getOfflinePlayerUUID(args[1]));
								} else {
									sendMessage(player, "&eThat player is not in your guild.");
								}
							} else {
								
							}
						} else {
							sendMessage(player, "&eYou must be the guild owner to use this command.");
						}
					} else if (args[0].equalsIgnoreCase("leave")) {
						// TODO - also remove player from any maps
					} else if (args[0].equalsIgnoreCase("confirm")) {
						if (transferOwnership.containsKey(player.getUniqueId())) {
							guild.transferOwnership(guild.getMember(transferOwnership.get(player.getUniqueId())));
							transferOwnership.remove(player.getUniqueId());
						} else {
							sendMessage(player, "&eYou have nothing to confirm.");
						}
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
		sendMessage(player, "&6Guild Commands:");
		sendMessage(player, "&e/guild invite [player] - invites a player to the guild.");
		sendMessage(player, "&e/guild kick [player] - kicks a player from the guild.");
		sendMessage(player, "&e/guild promote [player] - promotes a guild member.");
		sendMessage(player, "&e/guild transfer [player] - transfers the guild ownership to another member.");
		sendMessage(player, "&e/guild leave - removes you from your guild.");
		sendMessage(player, "&e/guild confirm - for confirming certain actions.");
	}

	private void sendMessage(Player player, String message) {
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
	}
	
	public static Map<UUID, UUID> getTransferOwnership() {
		return transferOwnership;
	}
	
	private static class GuildInvite {
		
		private UUID invited;
		private UUID inviter;
		
		public GuildInvite(UUID invited, UUID inviter) {
			this.invited = invited;
			this.inviter = inviter;
		}
		
		public UUID getInviter() {
			return this.inviter;
		}
		
		public UUID getInvited() {
			return this.invited;
		}
		
	}

}