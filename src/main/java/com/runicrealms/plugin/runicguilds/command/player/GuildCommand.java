package com.runicrealms.plugin.runicguilds.command.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.taskchain.TaskChain;
import com.runicrealms.plugin.common.api.guilds.GuildCreationResult;
import com.runicrealms.plugin.common.api.guilds.GuildRank;
import com.runicrealms.plugin.common.api.guilds.GuildStage;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.OfflinePlayerUtil;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.runicguilds.RunicGuilds;
import com.runicrealms.plugin.runicguilds.api.event.GuildInvitationAcceptedEvent;
import com.runicrealms.plugin.runicguilds.api.event.GuildInvitationDeclinedEvent;
import com.runicrealms.plugin.runicguilds.api.event.GuildMemberDemotedEvent;
import com.runicrealms.plugin.runicguilds.api.event.GuildMemberInvitedEvent;
import com.runicrealms.plugin.runicguilds.api.event.GuildMemberKickedEvent;
import com.runicrealms.plugin.runicguilds.api.event.GuildMemberLeaveEvent;
import com.runicrealms.plugin.runicguilds.api.event.GuildMemberPromotedEvent;
import com.runicrealms.plugin.runicguilds.api.event.GuildOwnershipTransferEvent;
import com.runicrealms.plugin.runicguilds.command.GuildCommandMapManager;
import com.runicrealms.plugin.runicguilds.guild.banner.GuildBannerUI;
import com.runicrealms.plugin.runicguilds.model.GuildData;
import com.runicrealms.plugin.runicguilds.model.GuildInfo;
import com.runicrealms.plugin.runicguilds.model.MemberData;
import com.runicrealms.plugin.runicguilds.ui.GuildInfoUI;
import com.runicrealms.plugin.runicguilds.util.GuildBankUtil;
import com.runicrealms.plugin.runicguilds.util.GuildUtil;
import com.runicrealms.plugin.runicguilds.util.TaskChainUtil;
import com.runicrealms.plugin.runicitems.util.CurrencyUtil;
import com.runicrealms.plugin.runicitems.util.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.UUID;

@CommandAlias("guild")
@SuppressWarnings("unused")
public class GuildCommand extends BaseCommand {
    private static final String GUILD_COMMAND_STRING = "guildCommand";

    private String combineArgs(String[] args) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            builder.append(args[i]);
            if (i != args.length - 1) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }

    /**
     * Attempts to create a guild with the given player as owner
     *
     * @param player to become owner
     * @param args   the name and prefix of the guild
     * @return true if successful, false if failed
     */
    private boolean createGuildFromCommand(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "To confirm creation of your guild, type &6/guild confirm <guild-prefix> <guild-name>&e. The prefix must be of 3-6 english letters."));
            return false;
        }

        if (!player.getInventory().contains(Material.GOLD_NUGGET, RunicGuilds.GUILD_COST)) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Put " + RunicGuilds.GUILD_COST + " coins in your inventory, and speak with the guild herald again."));
            RunicGuilds.getPlayersCreatingGuild().remove(player.getUniqueId());
            return false;
        }

        // Let's make a guild!
        GuildCreationResult result = RunicGuilds.getGuildsAPI().createGuild(player, this.combineArgs(args), args[0], false);
        if (result != GuildCreationResult.SUCCESSFUL) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + result.getMessage() + " Try again, or type &6/guild cancel&e."));
            return false;
        }

        ItemUtils.takeItem(player, CurrencyUtil.goldCoin(), RunicGuilds.GUILD_COST);
        RunicGuilds.getPlayersCreatingGuild().remove(player.getUniqueId());
        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + result.getMessage()));
        return true;
    }

    @Subcommand("accept")
    @Conditions("is-player")
    @CommandCompletion("@nothing")
    public void onGuildAcceptCommand(Player player) {
        if (RunicGuilds.getGuildsAPI().isInGuild(player)) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You cannot use this command since you are in a guild."));
            return;
        }

        if (!GuildCommandMapManager.getInvites().containsKey(player.getUniqueId())) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You don't have any pending invitations."));
            return;
        }

        // Get the guild of the inviter
        Player inviter = Bukkit.getPlayer(GuildCommandMapManager.getInvites().get(player.getUniqueId()));
        if (inviter == null) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "The guild inviter has gone offline!"));
            return;
        }
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(inviter);

        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> RunicGuilds.getDataAPI().loadGuildData(guildInfo.getUUID()))
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, player, "There was an error trying to accept guild invitation!")
                .syncLast(guildData -> {
                    RunicGuilds.getPlayersCreatingGuild().remove(player.getUniqueId());

                    // Ensure there is room for new member
                    GuildStage stage = GuildStage.getFromExp(guildInfo.getExp());
                    if (guildData.getMemberDataMap().size() >= stage.getMaxMembers()) {
                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You have reached your guild's maximum size."));
                    }
                    // Let's add a guild member!
                    guildData.getMemberDataMap().put(player.getUniqueId(), new MemberData(player.getUniqueId(), player.getName(), player.getPlayerProfile().getTextures().getSkin(), GuildRank.RECRUIT, 0));
                    // Save to MongoDB with TaskChain
                    RunicGuilds.getGuildWriteOperation().updateGuildData
                            (
                                    guildInfo.getUUID(),
                                    "memberDataMap",
                                    guildData.getMemberDataMap(),
                                    () -> {
                                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + ChatColor.GREEN + "You have accepted the guild invitation!"));
                                        Bukkit.getServer().getPluginManager().callEvent(new GuildInvitationAcceptedEvent
                                                (
                                                        guildData.getUUID(),
                                                        player.getUniqueId(),
                                                        GuildCommandMapManager.getInvites().get(player.getUniqueId())
                                                ));
                                        // Add to in-memory cache
                                        guildInfo.getMembersUuids().add(player.getUniqueId());
                                        GuildCommandMapManager.getInvites().remove(player.getUniqueId());
                                    }
                            );
                })
                .execute();
    }

    @Subcommand("bank")
    @Conditions("is-player|is-op")
    public void onGuildBankCommand(Player player) {
        if (!RunicGuilds.getGuildsAPI().isInGuild(player)) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You are not in a guild!"));
            return;
        }
        GuildBankUtil.open(player, 1);
    }

    @Subcommand("banner")
    @Conditions("is-player")
    @CommandCompletion("@nothing")
    public void onGuildBannerCommand(Player player) {
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (guildInfo == null) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You are not in a guild!"));
            return;
        }
        // Load member data async then return to main thread
        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> RunicGuilds.getDataAPI().loadMemberData(guildInfo.getUUID(), player.getUniqueId()))
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, player, "There was an error trying to open guild banner ui!")
                .syncLast(memberData -> {
                    if (memberData.getRank() != GuildRank.OWNER) {
                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You must be the owner of your guild to execute this command!"));
                        return;
                    }

                    if (guildInfo.getExp() < GuildStage.STAGE_2.getExp()) {
                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You must be at least guild stage two to create a banner!"));
                        return;
                    }

                    player.openInventory(new GuildBannerUI(guildInfo.getUUID()).getInventory());
                })
                .execute();
    }

    @Subcommand("cancel")
    @Conditions("is-player")
    @CommandCompletion("@nothing")
    public void onGuildCancelCommand(Player player) {
        if (RunicGuilds.getPlayersCreatingGuild().contains(player.getUniqueId())) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Canceled creating guild."));
            RunicGuilds.getPlayersCreatingGuild().remove(player.getUniqueId());
            return;
        }

        if (GuildCommandMapManager.getTransferOwnership().containsKey(player.getUniqueId())) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Canceled owner transfership."));
            GuildCommandMapManager.getTransferOwnership().remove(player.getUniqueId());
            return;
        }

        if (GuildCommandMapManager.getDisbanding().contains(player.getUniqueId())) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Canceled disbanding of the guild."));
            GuildCommandMapManager.getDisbanding().remove(player.getUniqueId());
            return;
        }
        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You have nothing to cancel."));
    }

    @Subcommand("confirm")
    @Conditions("is-player")
    @CommandCompletion("@nothing")
    public void onGuildConfirmCommand(Player player, String[] args) {
        // Creating a guild
        if (RunicGuilds.getPlayersCreatingGuild().contains(player.getUniqueId())) {
            boolean result = this.createGuildFromCommand(player, args);
            return;
        }

        // Ensure a guild exists for all following commands
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (guildInfo == null) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "A guild was not found."));
            return;
        }

        if (GuildCommandMapManager.getDisbanding().contains(player.getUniqueId())) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Disbanding guild..."));
            GuildData.disband(guildInfo.getUUID(), player, false);
        } else if (GuildCommandMapManager.getTransferOwnership().containsKey(player.getUniqueId())) {
            // Transferring ownership
            this.transferOwnership(player, guildInfo.getUUID());
        } else {
            // Not confirming
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You have nothing to confirm."));
        }
    }

    @Subcommand("decline")
    @Conditions("is-player")
    @CommandCompletion("@nothing")
    public void onGuildDeclineCommand(Player player) {
        if (!GuildCommandMapManager.getInvites().containsKey(player.getUniqueId())) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You don't have any pending invitations."));
            return;
        }

        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You have decline the guild invitation."));
        Player inviter = Bukkit.getPlayer(GuildCommandMapManager.getInvites().get(player.getUniqueId()));
        if (inviter != null) {
            GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(inviter); // Guild of the inviter
            Bukkit.getServer().getPluginManager().callEvent(new GuildInvitationDeclinedEvent(guildInfo.getUUID(), player.getUniqueId(), GuildCommandMapManager.getInvites().get(player.getUniqueId())));
            GuildCommandMapManager.getInvites().remove(player.getUniqueId());
        } else {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "The guild inviter has gone offline!"));
        }
    }

    @Subcommand("demote")
    @Syntax("<player>")
    @CommandCompletion("@players @nothing")
    @Conditions("is-player")
    public void onGuildDemoteCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(player);
            return;
        }

        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (guildInfo == null) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You are not in a guild!"));
            return;
        }

        String name = args[0];

        OfflinePlayerUtil.getUUID(name).thenAcceptAsync(uuid -> {
            TaskChain<?> chain = RunicGuilds.newChain();
            chain
                    .asyncFirst(() -> RunicGuilds.getDataAPI().loadGuildData(guildInfo.getUUID()))
                    .abortIfNull(TaskChainUtil.CONSOLE_LOG, player, "RunicGuilds failed to load data no bank!")
                    .sync(guildDataNoBank -> {
                        if (uuid == null || !guildDataNoBank.isInGuild(uuid)) {
                            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "That player is not in your guild."));
                            return null;
                        }

                        // Get member data of command user
                        if (!guildDataNoBank.isAtLeastRank(player, GuildRank.OFFICER)) {
                            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You must be of rank officer or higher to demote other players."));
                            return null;
                        }

                        GuildRank commandSenderRank = guildDataNoBank.getMemberDataMap().get(player.getUniqueId()).getRank();
                        GuildRank targetRank = guildDataNoBank.getMemberDataMap().get(uuid).getRank();
                        if (targetRank == GuildRank.RECRUIT) {
                            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You cannot demote players of the lowest guild rank!"));
                            return null;
                        }

                        if (targetRank.getRankNumber() <= commandSenderRank.getRankNumber()) {
                            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You can only demote players that are below your rank!"));
                            return null;
                        }

                        return guildDataNoBank;
                    })
                    .abortIfNull()
                    .async(guildDataNoBank -> {
                        GuildRank targetRank = guildDataNoBank.getMemberDataMap().get(uuid).getRank();
                        guildDataNoBank.getMemberDataMap().get(uuid).setRank(GuildRank.getByNumber(targetRank.getRankNumber() + 1));
                        return guildDataNoBank;
                    })
                    .syncLast(guildData -> {
                        // Demote success!
                        RunicGuilds.getGuildWriteOperation().updateGuildData
                                (
                                        guildInfo.getUUID(),
                                        "memberDataMap",
                                        guildData.getMemberDataMap(),
                                        () -> {
                                            GuildRank newRank = guildData.getMemberDataMap().get(uuid).getRank();
                                            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + name + " has been demoted to rank " + newRank + "!"));
                                            Bukkit.getServer().getPluginManager().callEvent(new GuildMemberDemotedEvent
                                                    (
                                                            guildData.getUUID(),
                                                            uuid,
                                                            player.getUniqueId(),
                                                            newRank
                                                    ));
                                        }
                                );
                    })
                    .execute();
        });


    }

    @Subcommand("disband")
    @Conditions("is-player")
    @CommandCompletion("@nothing")
    public void onGuildDisbandCommand(Player player) {
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (guildInfo == null) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You are not in a guild!"));
            return;
        }

        UUID ownerUUid = guildInfo.getOwnerUuid();
        if (!player.getUniqueId().equals(ownerUUid)) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You must be the guild owner to use this command."));
            return;
        }

        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Type /guild confirm if you with to proceed with disbanding the guild, or /guild cancel to cancel this."));
        GuildCommandMapManager.getDisbanding().add(player.getUniqueId());
        GuildCommandMapManager.getTransferOwnership().remove(player.getUniqueId());
    }

    @Default
    @CatchUnknown
    public void onGuildHelpCommand(CommandSender sender) {
        this.sendHelpMessage(sender);
    }

    @Subcommand("info")
    @Conditions("is-player")
    @CommandCompletion("@nothing")
    public void onGuildInfoCommand(Player player) {
        if (!RunicGuilds.getGuildsAPI().isInGuild(player)) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You are not in a guild!"));
            return;
        }
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        player.openInventory(new GuildInfoUI(player, guildInfo).getInventory());
    }

    @Subcommand("invite")
    @Syntax("<player>")
    @CommandCompletion("@players @nothing")
    @Conditions("is-player")
    public void onGuildInviteCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(player);
            return;
        }

        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (guildInfo == null) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You are not in a guild!"));
            return;
        }

        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> RunicGuilds.getDataAPI().loadGuildData(guildInfo.getUUID()))
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, player, "RunicGuilds failed to load data no bank!")
                .syncLast(guildDataNoBank -> {
                    if (!guildDataNoBank.isAtLeastRank(player, GuildRank.RECRUITER)) {
                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You must be of rank recruiter or higher to invite other players."));
                        return;
                    }

                    GuildStage guildStage = GuildStage.getFromExp(guildInfo.getExp());
                    if (guildDataNoBank.getMemberDataMap().size() >= guildStage.getMaxMembers()) {
                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You have reached your guild's maximum size."));
                        return;
                    }

                    Player target = Bukkit.getPlayerExact(args[0]);
                    if (target == null) {
                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "That player is not online."));
                        return;
                    }

                    if (RunicGuilds.getGuildsAPI().isInGuild(target)) {
                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "That player is already in a guild."));
                        return;
                    }

                    target.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You have been invited to join the guild " + guildInfo.getName() + " by " + player.getName() + ". Type /guild accept to accept the invitation, or /guild decline to deny the invitation."));
                    player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You have invited a player to the guild. An invitation has been sent."));
                    GuildCommandMapManager.getInvites().put(target.getUniqueId(), player.getUniqueId());
                    Bukkit.getServer().getPluginManager().callEvent(new GuildMemberInvitedEvent(guildInfo.getUUID(), target.getUniqueId(), player.getUniqueId()));
                })
                .execute();
    }

    @Subcommand("kick")
    @Syntax("<player>")
    @CommandCompletion("@players @nothing")
    @Conditions("is-player")
    public void onGuildKickCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(player);
            return;
        }

        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (guildInfo == null) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You are not in a guild!"));
            return;
        }


        OfflinePlayerUtil.getUUID(args[0]).thenAcceptAsync(otherPlayerUuid -> {
            if (otherPlayerUuid == null) {
                player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "UUID of player to remove was not found!"));
                return;
            }
            if (otherPlayerUuid.toString().equalsIgnoreCase(player.getUniqueId().toString())) {
                player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You can't remove yourself from the guild. To leave, type /guild leave."));
                return;
            }
            TaskChain<?> chain = RunicGuilds.newChain();
            chain
                    .asyncFirst(() -> {
                        GuildData guildDataNoBank = RunicGuilds.getDataAPI().loadGuildData(guildInfo.getUUID());
                        if (!guildDataNoBank.isAtLeastRank(player, GuildRank.OFFICER)) {
                            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You must be of rank officer or higher to kick other players."));
                            return null;
                        }

                        if (!guildDataNoBank.isInGuild(otherPlayerUuid)) {
                            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "That player is not in your guild."));
                            return null;
                        }

                        GuildRank commandSenderRank = guildDataNoBank.getMemberDataMap().get(player.getUniqueId()).getRank();
                        GuildRank targetRank = guildDataNoBank.getMemberDataMap().get(otherPlayerUuid).getRank();
                        if (targetRank.getRankNumber() <= commandSenderRank.getRankNumber()) {
                            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You can only kick players that are below your rank!"));
                            return null;
                        }
                        // Remove player
                        try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
                            guildDataNoBank.removeMember(otherPlayerUuid, jedis);
                        }
                        return guildDataNoBank;
                    })
                    .abortIfNull(TaskChainUtil.CONSOLE_LOG, player, "RunicGuilds failed to load data no bank!")
                    .syncLast(guildData -> {
                        // Complete operation and call event to remove player
                        Bukkit.getServer().getPluginManager().callEvent(new GuildMemberKickedEvent
                                (
                                        guildData,
                                        otherPlayerUuid,
                                        player.getUniqueId(),
                                        false
                                ));
                    })
                    .execute();
        });
    }

    @Subcommand("leave")
    @Conditions("is-player")
    @CommandCompletion("@nothing")
    public void onGuildLeaveCommand(Player player) {
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (guildInfo == null) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You are not in a guild!"));
            return;
        }

        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> {
                    GuildData guildDataNoBank = RunicGuilds.getDataAPI().loadGuildData(guildInfo.getUUID());
                    if (guildDataNoBank == null) return null;
                    if (guildDataNoBank.getMemberDataMap().get(player.getUniqueId()).getRank() == GuildRank.OWNER) {
                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You cannot leave the guild because you are the owner! To disband guild or transfer ownership, use those commands."));
                        return null;
                    }
                    try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
                        guildDataNoBank.removeMember(player.getUniqueId(), jedis);
                    }
                    return guildDataNoBank;
                })
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, player, "RunicGuilds failed to load data no bank!")
                .syncLast(guildData -> {
                    // Call event to complete operation
                    Bukkit.getServer().getPluginManager().callEvent(new GuildMemberLeaveEvent
                            (
                                    guildData.getUUID(),
                                    player,
                                    guildData
                            ));
                    GuildCommandMapManager.getTransferOwnership().remove(player.getUniqueId());
                    GuildCommandMapManager.getDisbanding().remove(player.getUniqueId());
                })
                .execute();
    }

    @Subcommand("promote")
    @Syntax("<player>")
    @CommandCompletion("@players @nothing")
    @Conditions("is-player")
    public void onGuildPromoteCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(player);
            return;
        }

        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (guildInfo == null) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You are not in a guild!"));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Target player was not found!"));
            return;
        }

        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> RunicGuilds.getDataAPI().loadGuildData(guildInfo.getUUID()))
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, player, "RunicGuilds failed to load data no bank!")
                .sync(guildData -> {
                    if (!guildData.isAtLeastRank(player, GuildRank.OFFICER)) {
                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You must be of rank officer or higher to promote other players."));
                        return null;
                    }
                    GuildRank targetRank = guildData.getMemberDataMap().get(target.getUniqueId()).getRank();
                    GuildRank promoterRank = guildData.getMemberDataMap().get(player.getUniqueId()).getRank();

                    if (!guildData.isInGuild(target.getUniqueId())) {
                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "That player is not in your guild."));
                        return null;
                    }

                    if (targetRank == GuildRank.OFFICER) {
                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You cannot promote another player to owner. To transfer guild ownership, use /guild transfer."));
                        return null;
                    }

                    if (targetRank == GuildRank.OWNER) {
                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You cannot promote the guild owner."));
                        return null;
                    }

                    if (targetRank.getRankNumber() - 1 <= promoterRank.getRankNumber()) {
                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You can only promote players that are below your rank!"));
                        return null;
                    }

                    return guildData;
                })
                .abortIfNull()
                .async(guildData -> {
                    GuildRank targetRank = guildData.getMemberDataMap().get(target.getUniqueId()).getRank();
                    guildData.getMemberDataMap().get(target.getUniqueId()).setRank(GuildRank.getByNumber(targetRank.getRankNumber() - 1));
                    return guildData;
                })
                .syncLast(guildData -> RunicGuilds.getGuildWriteOperation().updateGuildData
                        (
                                guildInfo.getUUID(),
                                "memberDataMap",
                                guildData.getMemberDataMap(),
                                () -> {
                                    GuildRank newRank = guildData.getMemberDataMap().get(target.getUniqueId()).getRank();
                                    player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + target.getName() + " has been promoted to rank " + newRank + "!"));
                                    Bukkit.getServer().getPluginManager().callEvent(new GuildMemberPromotedEvent
                                            (
                                                    guildData.getUUID(),
                                                    target.getUniqueId(),
                                                    player.getUniqueId(),
                                                    newRank
                                            ));
                                }
                        ))
                .execute();
    }

    @Subcommand("settings bank")
    @Conditions("is-player")
    @CommandCompletion("Recruit|Member|Recruiter|Officer yes|no")
    public void onGuildSettingsBankCommand(Player player, String[] args) {
//        if (args.length != 2) {
//            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You have use improper arguments to execute this command!"));
//            this.sendHelpMessage(player);
//            return;
//        }
//
//        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
//        if (guildInfo == null) {
//            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You are not in a guild!"));
//            return;
//        }
//
//        GuildRank rank = GuildRank.getByIdentifier(args[0]);
//        if (rank == null) {
//            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "That isn't a valid guild rank!"));
//            return;
//        }
//
//        if (rank == GuildRank.OWNER) {
//            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You cannot deny/allow bank access to the guild owner!"));
//            return;
//        }
//
//        TaskChain<?> chain = RunicGuilds.newChain();
//        chain
//                .asyncFirst(() -> {
//                    try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
//                        return RunicGuilds.getDataAPI().loadSettingsData(guildInfo.getGuildUUID(), jedis);
//                    }
//                })
//                .abortIfNull(TaskChainUtil.CONSOLE_LOG, player, "RunicGuilds failed to load data no bank!")
//                .syncLast(guildData -> {
//                    SettingsData settingsData = guildData.getSettingsData();
//                    if (args[1].equalsIgnoreCase("yes") || args[1].equalsIgnoreCase("true")) {
//                        settingsData.getBankSettingsMap().put(rank, true);
//                        // guildData.queueToSave();
//                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Updated guild bank permissions."));
//                    } else if (args[1].equalsIgnoreCase("no") || args[1].equalsIgnoreCase("false")) {
//                        settingsData.getBankSettingsMap().put(rank, true);
//                        // guildData.queueToSave();
//                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Updated guild bank permissions."));
//                    } else {
//                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Please enter \"YES\" or \"NO\"."));
//                    }
//                })
//                .execute();
    }

    @Subcommand("transfer")
    @Syntax("<player>")
    @CommandCompletion("@players @nothing")
    @Conditions("is-player")
    public void onGuildTransferCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(player);
            return;
        }

        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (guildInfo == null) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You are not in a guild!"));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "That player is not online."));
            return;
        }

        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> RunicGuilds.getDataAPI().loadGuildData(guildInfo.getUUID()))
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, player, "RunicGuilds failed to load data no bank!")
                .syncLast(guildDataNoBank -> {
                    if (!guildDataNoBank.getOwnerUuid().equals(player.getUniqueId())) {
                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You must be the guild owner to use this command."));
                        return;
                    }

                    if (!guildDataNoBank.isInGuild(target.getUniqueId())) {
                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "That player is not in your guild."));
                        return;
                    }

                    if (args[0].equalsIgnoreCase(player.getName())) {
                        player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "You cannot transfer ownership to yourself."));
                        return;
                    }

                    player.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "Type /guild confirm to confirm your actions, or /guild cancel to cancel. &cWARNING - You will be demoted to officer if you confirm!"));
                    GuildCommandMapManager.getTransferOwnership().put(player.getUniqueId(), target.getUniqueId());
                    GuildCommandMapManager.getDisbanding().remove(player.getUniqueId());
                })
                .execute();
    }

    private void sendHelpMessage(CommandSender sender) {
        String[] messages = new String[]{"&6Guild Commands:",
                "&e/guild info &r- gets guild members, score and experience.",
                "&e/guild invite &6<player> &r- invites a player to the guild.",
                "&e/guild kick &6<player> &r- kicks a player from the guild.",
                "&e/guild promote&6/&edemote &6<player> &r- promotes/demotes a guild member.",
                "&e/guild disband &r- disbands your guild.",
                "&e/guild transfer &6<player> &r- transfers the guild ownership to another member.",
                "&e/guild leave &r- removes you from your guild.",
                "&e/guild accept&6/&edecline &r- accepts/declines an invitation to join a guild.",
                "&e/guild confirm&6/&ecancel &r- for confirming/canceling certain actions.",
                "&e/guild banner &r- to make a custom guild banner."};
        for (String message : messages) {
            sender.sendMessage(ColorUtil.format(message));
        }
    }

    /**
     * Initiates the process of transferring a guild from one player to another
     *
     * @param player    who initiated the transfer
     * @param guildUUID of the guild
     */
    private void transferOwnership(Player player, UUID guildUUID) {
        Player newOwner = Bukkit.getPlayer(GuildCommandMapManager.getTransferOwnership().get(player.getUniqueId()));
        if (newOwner == null) {
            player.sendMessage(GuildUtil.PREFIX + "The new owner must be in the guild and online to transfer the guild!");
            return;
        }
        Bukkit.getServer().getPluginManager().callEvent(new GuildOwnershipTransferEvent
                (
                        guildUUID,
                        newOwner,
                        player
                ));
        GuildCommandMapManager.getTransferOwnership().remove(player.getUniqueId());
    }
}