package com.runicrealms.runicguilds.command;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.runicrealms.runicguilds.guilds.GuildMember;
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
	private static Map<UUID, UUID> invites = new HashMap<UUID, UUID>();

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
										sendMessage(Bukkit.getPlayerExact(args[1]), "&eYou have been invited to join the guild " + guild.getGuildName() + " by " + player.getName() + ". Type /guild accept to accept the invitation");
										sendMessage(player, "&eYou have invited a player to the guild. An invitation has been sent.");
										invites.put(Bukkit.getPlayerExact(args[1]).getUniqueId(), player.getUniqueId());
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
								if (!otherPlayer.getUniqueId().toString().equalsIgnoreCase(player.getUniqueId().toString())) {
									if (guild.isInGuild(args[1])) {
										if (guild.getMember(player.getUniqueId()).getRank().getRankNumber() > guild.getMember(otherPlayer.getUniqueId()).getRank().getRankNumber()) {
											guild.removeMember(otherPlayer.getUniqueId());
											sendMessage(player, "&eRemoved player from the guild!");
										} else {
											sendMessage(player, "&eYou can only kick players that are of lower rank than you.");
										}
									} else {
										sendMessage(player, "&eThat player is not in your guild.");
									}
								} else {
									sendMessage(player, "&eYou can't remove yourself from the guild. To leave, type /guild leave.");
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
									GuildMember member = guild.getMember(Bukkit.getPlayerExact(args[1]).getUniqueId());
									if (member.getRank().getRankNumber() >= guild.getMember(player.getUniqueId()).getRank().getRankNumber()) {
										member.setRank(GuildRank.getByNumber(member.getRank().getRankNumber()));
										sendMessage(player, "&eMember has been promoted!");
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
									sendMessage(player, "&eType /guild confirm to confirm your actions. &cWARNING - You will be kicked from the guild if you confirm.");
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
						guild.removeMember(player.getUniqueId());
						sendMessage(player, "&eYou have left your guild.");
						if (transferOwnership.containsKey(player.getUniqueId())) {
							transferOwnership.remove(player.getUniqueId());
						}
					} else if (args[0].equalsIgnoreCase("confirm")) {
						if (transferOwnership.containsKey(player.getUniqueId())) {
							guild.transferOwnership(guild.getMember(transferOwnership.get(player.getUniqueId())));
							transferOwnership.remove(player.getUniqueId());
							sendMessage(player, "&eSuccessfully transferred guild ownership. You have been removed from your guild.");
							GuildUtil.getGuildFiles().get(guild.getGuildPrefix()).save(guild);
						} else {
							sendMessage(player, "&eYou have nothing to confirm.");
						}
					} else if (args[0].equalsIgnoreCase("accept")) {
						sendMessage(player, "&eYou cannot use this command since you are in a guild.");
					} else {
						sendHelpMessage(player);
					}
				}
			} else if (args[0].equalsIgnoreCase("accept")) {
				if (invites.containsKey(player.getUniqueId())) {
					Guild guild = GuildUtil.getGuild(invites.get(player.getUniqueId()));
					guild.getMembers().add(new GuildMember(player.getUniqueId(), GuildRank.MEMBER, 0));
					sendMessage(player, "&eYou have accepted the guild invitation.");
				} else {
					sendMessage(player, "&eYou don't have any pending invitations.");
				}
			} else if (args[0].equalsIgnoreCase("decline")) {
				if (invites.containsKey(player.getUniqueId())) {
					invites.remove(player.getUniqueId());
					sendMessage(player, "&eYou have decline the guild invitation.");
				} else {
					sendMessage(player, "&eYou don't have any pending invitations.");
				}
			} else {
				sendMessage(player, "&eYou must be in a guild to use this command.");
			}
		}
		return true;
	}

	private static void sendHelpMessage(Player player) {
		sendMessage(player, "&6Guild Commands:");
		sendMessage(player, "&e/guild invite &6[player] &r- invites a player to the guild.");
		sendMessage(player, "&e/guild kick [player] &r- kicks a player from the guild.");
		sendMessage(player, "&e/guild promote [player] &r- promotes a guild member.");
		sendMessage(player, "&e/guild transfer [player] &r- transfers the guild ownership to another member.");
		sendMessage(player, "&e/guild leave &r- removes you from your guild.");
		sendMessage(player, "&e/guild accept&6/&edecline &r- accepts/declines an invitation to join a guild.");
		sendMessage(player, "&e/guild confirm &r- for confirming certain actions.");
	}

	private static void sendMessage(Player player, String message) {
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
	}
	
	public static Map<UUID, UUID> getTransferOwnership() {
		return transferOwnership;
	}

	public static Map<UUID, UUID> getInvites() {
		return invites;
	}

}