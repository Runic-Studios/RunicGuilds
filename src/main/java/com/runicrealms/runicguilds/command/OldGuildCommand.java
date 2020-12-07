package com.runicrealms.runicguilds.command;

import java.util.*;

import com.runicrealms.plugin.item.util.ItemRemover;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.api.*;
import com.runicrealms.runicguilds.data.GuildData;
import com.runicrealms.runicguilds.data.PlayerGuildDataUtil;
import com.runicrealms.runicguilds.guilds.GuildMember;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.runicrealms.runicguilds.data.GuildUtil;
import com.runicrealms.runicguilds.gui.GuildBankUtil;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildRank;

public class OldGuildCommand implements CommandExecutor {

	//REMOVE THIS HORRID THING xD -BoBo

	private static Map<UUID, UUID> transferOwnership = new HashMap<UUID, UUID>();
	private static Map<UUID, UUID> invites = new HashMap<UUID, UUID>();
	private static Set<UUID> disbanding = new HashSet<UUID>();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			boolean isConfirming = false;
			if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
				if (args.length > 0) {
					if (args[0].equalsIgnoreCase("confirm") || args[0].equalsIgnoreCase("cancel")) {
						if (Plugin.getPlayersCreatingGuild().contains(player.getUniqueId())) {
							if (GuildUtil.getPlayerCache().get(player.getUniqueId()) == null) {
								isConfirming = true;
							}
						}
					}
				}
			}
			if (GuildUtil.getPlayerCache().get(player.getUniqueId()) != null || isConfirming) {
				String prefix = null;
				GuildData guildData = null;
				Guild guild = null;
				if (!isConfirming) {
					prefix = GuildUtil.getPlayerCache().get(player.getUniqueId());
					guildData = GuildUtil.getGuildData(prefix);
					guild = guildData.getData();
				}
				if (args.length == 0) {
					sendHelpMessage(player);
				} else {
					if (args[0].equalsIgnoreCase("invite")) {
						if (guild.hasMinRank(player.getUniqueId(), GuildRank.RECRUITER)) {
							if (args.length == 2) {
								if (Bukkit.getPlayerExact(args[1]) != null) {
									if (GuildUtil.getPlayerCache().get(Bukkit.getPlayer(args[1]).getUniqueId()) == null) {
										sendMessage(Bukkit.getPlayerExact(args[1]), "&eYou have been invited to join the guild " + guild.getGuildName() + " by " + player.getName() + ". Type /guild accept to accept the invitation, or /guild decline to deny the invitation.");
										sendMessage(player, "&eYou have invited a player to the guild. An invitation has been sent.");
										invites.put(Bukkit.getPlayerExact(args[1]).getUniqueId(), player.getUniqueId());
										guildData.queueToSave();
										Bukkit.getServer().getPluginManager().callEvent(new GuildMemberInvitedEvent(guild, Bukkit.getPlayerExact(args[1]).getUniqueId(), player.getUniqueId()));
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
					} else if (args[0].equalsIgnoreCase("settings")) {
						if (args.length >= 2) {
							if (args[1].equalsIgnoreCase("bank")) {
								if (args.length == 4) {
									GuildRank rank = GuildRank.getByIdentifier(args[2]);
									if (rank == null) {
										sendMessage(player, "&eThat isn't a valid guild rank!");
									} else if (rank == GuildRank.OWNER) {
										sendMessage(player, "&eYou cannot deny/allow bank access to the guild owner!");
									} else {
										if (args[3].equalsIgnoreCase("yes") || args[3].equalsIgnoreCase("true")) {
											guild.setBankAccess(rank, true);
											guildData.queueToSave();
											sendMessage(player, "&eUpdated guild bank permissions.");
										} else if (args[3].equalsIgnoreCase("no") || args[3].equalsIgnoreCase("false")) {
											guild.setBankAccess(rank, false);
											guildData.queueToSave();
											sendMessage(player, "&eUpdated guild bank permissions.");
										} else {
											sendMessage(player, "&ePlease enter \"yes\" or \"no\".");
										}
									}
								} else {
									sendMessage(player, "&eUse the command: /guild bank settings <rank> <yes|no> to allow or deny a specific rank access to your bank.");
								}
							} else {
								sendMessage(player, "&eUse the command: /guild bank settings <rank> <yes|no> to allow or deny a specific rank access to your bank.");
							}
						} else {
							sendMessage(player, "&eUse the command: /guild bank settings <rank> <yes|no> to allow or deny a specific rank access to your bank.");
						}
					} else if (args[0].equalsIgnoreCase("bank")) {
						if (player.isOp()) {
							GuildBankUtil.open(player, 1);
						} else {
							sendMessage(player , "&eYou do not have permission to use this command.");
						}
					} else if (args[0].equalsIgnoreCase("kick")) {
						if (guild.hasMinRank(player.getUniqueId(), GuildRank.OFFICER)) {
							if (args.length == 2) {
								UUID otherPlayer = GuildUtil.getOfflinePlayerUUID(args[1]);
								if (!otherPlayer.toString().equalsIgnoreCase(player.getUniqueId().toString())) {
									if (guild.isInGuild(args[1])) {
										if (guild.getMember(player.getUniqueId()).getRank().getRankNumber() < guild.getMember(otherPlayer).getRank().getRankNumber()) {
											guild.removeMember(otherPlayer);
											PlayerGuildDataUtil.setGuildForPlayer("None", otherPlayer.toString());
											sendMessage(player, "&eRemoved player from the guild!");
											if (GuildUtil.getPlayerCache().containsKey(otherPlayer)) {
												GuildUtil.getPlayerCache().put(otherPlayer, null);
											}
											guildData.queueToSave();
											Bukkit.getServer().getPluginManager().callEvent(new GuildMemberKickedEvent(guild, Bukkit.getPlayerExact(args[1]).getUniqueId(), player.getUniqueId(), false));
											if (GuildBankUtil.isViewingBank(otherPlayer) && Bukkit.getPlayerExact(args[1]) != null) {
												GuildBankUtil.close(Bukkit.getPlayerExact(args[1]));
											}
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
							sendMessage(player, "&eYou must be of rank officer or higher to kick other players.");
						}
					} else if (args[0].equalsIgnoreCase("info")) {
						sendMessage(player, "&6[" + guild.getScore() + "]&r &e&l" + guild.getGuildName());
						sendMessage(player, "&6Guild Owner: &7[" + guild.getOwner().getScore() + "] &e" + guild.getOwner().getLastKnownName());
						HashMap<GuildRank, StringBuilder> members = new HashMap<GuildRank, StringBuilder>();
						for (GuildMember member : guild.getMembers()) {
						    if (!members.containsKey(member.getRank())) {
						        members.put(member.getRank(), new StringBuilder());
                            }
						    members.get(member.getRank())
                                    .append("&7[")
                                    .append(member.getScore())
                                    .append("] &e")
                                    .append(member.getLastKnownName())
                                    .append("&r, ");
                        }
						for (GuildRank rank : GuildRank.values()) {
							if (members.containsKey(rank)) {
								sendMessage(player, "&6Guild " + rank.getPlural() + "s: &r" + members.get(rank).toString().substring(0, members.get(rank).toString().length() - 2));
							}
						}
					} else if (args[0].equalsIgnoreCase("promote") || args[0].equalsIgnoreCase("demote")) {
						if (guild.hasMinRank(player.getUniqueId(), GuildRank.OFFICER)) {
							if (args.length == 2) {
								if (guild.isInGuild(args[1])) {
									GuildMember member = null;
									for (GuildMember target : guild.getMembers()) {
										if (target.getLastKnownName().equalsIgnoreCase(args[1])) {
											member = target;
										}
									}
									if (member != null) {
										if (args[0].equalsIgnoreCase("promote")) {
											if (member.getRank().getRankNumber() > guild.getMember(player.getUniqueId()).getRank().getRankNumber() &&
													member.getRank() != GuildRank.OFFICER) {
												member.setRank(GuildRank.getByNumber(member.getRank().getRankNumber() - 1));
												sendMessage(player, "&eMember has been promoted.");
												guildData.queueToSave();
												Bukkit.getServer().getPluginManager().callEvent(new GuildMemberPromotedEvent(guild, member.getUUID(), player.getUniqueId()));
											} else {
												if (member.getRank() == GuildRank.OFFICER) {
													sendMessage(player, "&eYou cannot promote another player to owner. To transfer guild ownership, use /guild transfer.");
												} else {
													sendMessage(player, "&eYou can only promote members that are under your rank.");
												}
											}
										} else {
											if (member.getRank().getRankNumber() > guild.getMember(player.getUniqueId()).getRank().getRankNumber() &&
													member.getRank() != GuildRank.RECRUIT) {
												member.setRank(GuildRank.getByNumber(member.getRank().getRankNumber() + 1));
												sendMessage(player, "&eMember has been demoted.");
												guildData.queueToSave();
												Bukkit.getServer().getPluginManager().callEvent(new GuildMemberDemotedEvent(guild, member.getUUID(), player.getUniqueId()));
											} else {
												if (member.getRank() == GuildRank.RECRUIT) {
													sendMessage(player, "&eYou cannot demote players of the lowest guild rank.");
												} else {
													sendMessage(player, "&eYou can only demote players that are under your rank.");
												}
											}
										}
									} else {
										sendMessage(player, "&eThat player is not in your guild!");
									}
								} else {
									sendMessage(player, "&eThat player is not in your guild.");
								}
							} else {
								sendHelpMessage(player);
							}
						} else {
							sendMessage(player, "&eYou must be of rank officer or higher to " + (args[0].equalsIgnoreCase("promote") ? "promote" : "demote") + " other players.");
						}
					} else if (args[0].equalsIgnoreCase("transfer")) {
						if (guild.getMember(player.getUniqueId()).getRank() == GuildRank.OWNER) {
							if (args.length == 2) {
								if (guild.isInGuild(args[1])) {
									if (!args[1].equalsIgnoreCase(player.getName())) {
										sendMessage(player, "&eType /guild confirm to confirm your actions, or /guild cancel to cancel. &cWARNING - You will be demoted to officer if you confirm!");
										transferOwnership.put(player.getUniqueId(), GuildUtil.getOfflinePlayerUUID(args[1]));
										if (disbanding.contains(player.getUniqueId())) {
											disbanding.remove(player.getUniqueId());
										}
									} else {
										sendMessage(player, "&eYou cannot transfer ownership to yourself.");
									}
								} else {
									sendMessage(player, "&eThat player is not in your guild.");
								}
							} else {
								sendHelpMessage(player);
							}
						} else {
							sendMessage(player, "&eYou must be the guild owner to use this command.");
						}
					} else if (args[0].equalsIgnoreCase("set")) {
						if (guild.getMember(player.getUniqueId()).getRank() == GuildRank.OWNER) {
							if (args.length >= 2) {
								if (args[1].equalsIgnoreCase("name")) {
									if (args.length >= 3) {
										sendMessage(player, "&e" + GuildUtil.renameGuild(guildData, combineArgs(args, 2)).getMessage());
									} else {
										sendMessage(player, "&eType &6/guild set name &e<name>.");
									}
								} else if (args[1].equalsIgnoreCase("prefix")) {
									if (args.length == 3) {
										GuildData finalGuildData = guildData;
										Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> {
											sendMessage(player, "&e" + GuildUtil.reprefixGuild(finalGuildData, args[2]).getMessage());
										});
									} else {
										sendMessage(player, "&eType &6/guild set prefix &e<prefix>.");
									}
								} else {
									sendMessage(player, "&eType &6/guild set name&e/&6prefix&e.");
								}
							} else {
								sendMessage(player, "&eType &6/guild set name&e/&6prefix&e.");
							}
						} else {
							sendMessage(player, "&eYou must be guild owner to use that command!");
						}
					} else if (args[0].equalsIgnoreCase("leave")) {
						if (guild.getMember(player.getUniqueId()).getRank() != GuildRank.OWNER) {
							PlayerGuildDataUtil.setGuildForPlayer("None", player.getUniqueId().toString());
							guild.removeMember(player.getUniqueId());
							sendMessage(player, "&eYou have left your guild.");
							GuildUtil.getPlayerCache().put(player.getUniqueId(), null);
							guildData.queueToSave();
							Bukkit.getServer().getPluginManager().callEvent(new GuildMemberLeaveEvent(guild, player.getUniqueId()));
							if (transferOwnership.containsKey(player.getUniqueId())) {
								transferOwnership.remove(player.getUniqueId());
							}
							if (disbanding.contains(player.getUniqueId())) {
								disbanding.remove(player.getUniqueId());
							}
							if (GuildBankUtil.isViewingBank(player.getUniqueId())) {
								GuildBankUtil.close(player);
							}
						} else {
							sendMessage(player, "&eYou cannot leave the guild because you are the owner! To disband guild or transfer ownership, use those commands.");
						}
					} else if (args[0].equalsIgnoreCase("confirm")) {
						if (Plugin.getPlayersCreatingGuild().contains(player.getUniqueId())) {
							if (args.length >= 3) {
								if (player.getInventory().contains(Material.GOLD_NUGGET, Plugin.GUILD_COST)) {
									GuildCreationResult result = RunicGuildsAPI.createGuild(player.getUniqueId(), combineArgs(args, 2), args[1], false);
									if (result == GuildCreationResult.SUCCESSFUL) {
										PlayerGuildDataUtil.setGuildForPlayer(GuildUtil.getGuildData(player.getUniqueId()).getData().getGuildName(), player.getUniqueId().toString());
										ItemRemover.takeItem(player, Material.GOLD_NUGGET, Plugin.GUILD_COST);
										Plugin.getPlayersCreatingGuild().remove(player.getUniqueId());
										sendMessage(player, "&e" + result.getMessage());
									} else {
										sendMessage(player, "&e"+ result.getMessage() + " Try again, or type &6/guild cancel&e.");
									}
								} else {
									sendMessage(player, "&ePut " + Plugin.GUILD_COST + " coins in your inventory, and speak with the guild herald again.");
									Plugin.getPlayersCreatingGuild().remove(player.getUniqueId());
								}
 							} else {
								sendMessage(player, "&eTo confirm creation of your guild, type &6/guild confirm <guild-prefix> <guild-name>&e. The prefix must be of 3-6 english letters.");
							}
						} else if (transferOwnership.containsKey(player.getUniqueId())) {
							guild.transferOwnership(guild.getMember(transferOwnership.get(player.getUniqueId())));
							sendMessage(player, "&eSuccessfully transferred guild ownership. You have been demoted to officer.");
							guildData.queueToSave();
							Bukkit.getServer().getPluginManager().callEvent(new GuildOwnershipTransferedEvent(guild, transferOwnership.get(player.getUniqueId()), player.getUniqueId()));
							transferOwnership.remove(player.getUniqueId());
						} else if (disbanding.contains(player.getUniqueId())) {
							for (GuildMember member : guild.getMembers()) {
								PlayerGuildDataUtil.setGuildForPlayer("None", member.getUUID().toString());
								if (GuildUtil.getPlayerCache().containsKey(member.getUUID())) {
									GuildUtil.getPlayerCache().put(member.getUUID(), null);
								}
								if (GuildBankUtil.isViewingBank(member.getUUID())) {
									GuildBankUtil.close(Bukkit.getPlayer(member.getUUID()));
								}
							}
							GuildUtil.getPlayerCache().put(guild.getOwner().getUUID(), null);
							PlayerGuildDataUtil.setGuildForPlayer("None", guild.getOwner().getUUID().toString());
							if (GuildBankUtil.isViewingBank(guild.getOwner().getUUID())) {
								GuildBankUtil.close(Bukkit.getPlayer(guild.getOwner().getUUID()));
							}
							Bukkit.getServer().getPluginManager().callEvent(new GuildDisbandEvent(guild, player.getUniqueId(), false));
							guildData.deleteData();
							GuildUtil.getGuildDatas().remove(guild.getGuildPrefix());
							sendMessage(player, "&eSuccessfully disbanded guild.");
							disbanding.remove(player.getUniqueId());
						} else {
							sendMessage(player, "&eYou have nothing to confirm.");
						}
					} else if (args[0].equalsIgnoreCase("cancel")) {
						if (Plugin.getPlayersCreatingGuild().contains(player.getUniqueId())) {
							sendMessage(player, "&eCanceled creating guild.");
							Plugin.getPlayersCreatingGuild().remove(player.getUniqueId());
						} else if (transferOwnership.containsKey(player.getUniqueId())) {
							sendMessage(player, "&eCanceled owner transfership.");
							transferOwnership.remove(player.getUniqueId());
						} else if (disbanding.contains(player.getUniqueId())) {
							sendMessage(player, "&eCanceled disbanding of the guild.");
							disbanding.remove(player.getUniqueId());
						}
					} else if (args[0].equalsIgnoreCase("accept")) {
						sendMessage(player, "&eYou cannot use this command since you are in a guild.");
					} else if (args[0].equalsIgnoreCase("disband")) {
						if (guild.getMember(player.getUniqueId()).getRank() == GuildRank.OWNER) {
							if (args.length == 1) {
								sendMessage(player, "&eType /guild confirm if you with to proceed with disbanding the guild, or /guild cancel to cancel this.");
								disbanding.add(player.getUniqueId());
								if (transferOwnership.containsKey(player.getUniqueId())) {
									transferOwnership.remove(player.getUniqueId());
								}
							} else {
								sendHelpMessage(player);
							}
						} else {
							sendMessage(player, "&eYou must be the guild owner to use this command.");
						}
					} else {
						sendHelpMessage(player);
					}
				}
			} else if (args.length == 0) {
				sendHelpMessage(player);
			} else {
				if (args[0].equalsIgnoreCase("accept")) {
					if (invites.containsKey(player.getUniqueId())) {
						if (Plugin.getPlayersCreatingGuild().contains(player.getUniqueId())) {
							Plugin.getPlayersCreatingGuild().remove(player.getUniqueId());
						}
						GuildData guildData = GuildUtil.getGuildData(invites.get(player.getUniqueId()));
						Guild guild = guildData.getData();
						guild.getMembers().add(new GuildMember(player.getUniqueId(), GuildRank.RECRUIT, 0, player.getName()));
						PlayerGuildDataUtil.setGuildForPlayer(guild.getGuildName(), player.getUniqueId().toString());
						sendMessage(player, "&eYou have accepted the guild invitation.");
						guildData.queueToSave();
						GuildUtil.getPlayerCache().put(player.getUniqueId(), guild.getGuildPrefix());
						Bukkit.getServer().getPluginManager().callEvent(new GuildInvitationAcceptedEvent(guild, player.getUniqueId(), invites.get(player.getUniqueId())));
						invites.remove(player.getUniqueId());
					} else {
						sendMessage(player, "&eYou don't have any pending invitations.");
					}
				} else if (args[0].equalsIgnoreCase("decline")) {
					if (invites.containsKey(player.getUniqueId())) {
						sendMessage(player, "&eYou have decline the guild invitation.");
						Guild guild = GuildUtil.getGuildData(invites.get(player.getUniqueId())).getData();
						Bukkit.getServer().getPluginManager().callEvent(new GuildInvitationDeclinedEvent(guild, player.getUniqueId(), invites.get(player.getUniqueId())));
						invites.remove(player.getUniqueId());
					} else {
						sendMessage(player, "&eYou don't have any pending invitations.");
					}
				} else {
					sendMessage(player, "&eYou must be in a guild to use this command.");
				}
			}
		}
		return true;
	}

	private static void sendHelpMessage(Player player) {
		sendMessage(player, "&6Guild Commands:");
		sendMessage(player, "&e/guild info &r- gets guild members and score.");
		sendMessage(player, "&e/guild invite &6[player] &r- invites a player to the guild.");
		sendMessage(player, "&e/guild bank &r- opens your guild bank.");
		sendMessage(player, "&e/guild set name&6/&eprefix <text> &r- sets your guild name/prefix.");
		sendMessage(player, "&e/guild kick &6[player] &r- kicks a player from the guild.");
		sendMessage(player, "&e/guild promote&6/&edemote &6[player] &r- promotes/demotes a guild member.");
		sendMessage(player, "&e/guild disband &r- disbands your guild.");
		sendMessage(player, "&e/guild transfer &6[player] &r- transfers the guild ownership to another member.");
		sendMessage(player, "&e/guild leave &r- removes you from your guild.");
		sendMessage(player, "&e/guild accept&6/&edecline &r- accepts/declines an invitation to join a guild.");
		sendMessage(player, "&e/guild confirm&6/&ecancel &r- for confirming/canceling certain actions.");
	}

	private static void sendMessage(Player player, String message) {
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	public static Map<UUID, UUID> getTransferOwnership() {
		return transferOwnership;
	}

	public static Map<UUID, UUID> getInvites() {
		return invites;
	}

	public static Set<UUID> getDisbanding() {
		return disbanding;
	}

	private static String combineArgs(String[] args, int start) {
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < args.length; i++) {
			builder.append(args[i]);
			if (i != args.length - 1) {
				builder.append(" ");
			}
		}
		return builder.toString();
	}

}