package com.runicrealms.runicguilds.command.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.taskchain.TaskChain;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.api.event.GiveGuildEXPEvent;
import com.runicrealms.runicguilds.api.event.GuildCreationEvent;
import com.runicrealms.runicguilds.api.event.GuildDisbandEvent;
import com.runicrealms.runicguilds.api.event.GuildMemberKickedEvent;
import com.runicrealms.runicguilds.api.event.GuildScoreChangeEvent;
import com.runicrealms.runicguilds.command.GuildCommandMapManager;
import com.runicrealms.runicguilds.guild.GuildCreationResult;
import com.runicrealms.runicguilds.guild.banner.GuildBannerLoader;
import com.runicrealms.runicguilds.guild.stage.GuildEXPSource;
import com.runicrealms.runicguilds.model.GuildInfo;
import com.runicrealms.runicguilds.util.GuildBankUtil;
import com.runicrealms.runicguilds.util.GuildUtil;
import com.runicrealms.runicguilds.util.TaskChainUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CommandAlias("guildmod")
@CommandPermission("permissions.guild-mod")
@Conditions("is-player")
@SuppressWarnings("unused")
public class GuildModCMD extends BaseCommand {

    private final String prefix = "&r&6&lGuilds (Mod) »&r &e";

    public GuildModCMD() {
        RunicGuilds.getCommandManager().getCommandCompletions().registerAsyncCompletion("reasons", context -> {
            List<String> names = new ArrayList<>();
            for (GuildEXPSource source : GuildEXPSource.values()) {
                String name = source.name().charAt(0) + source.name().substring(1).toLowerCase();
                names.add(name);
            }
            return names;
        });
    }

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

    private GuildEXPSource getGuildExpSource(String name) {
        for (GuildEXPSource source : GuildEXPSource.values()) {
            if (source.name().replace("_", " ").equalsIgnoreCase(name)) {
                return source;
            }
        }
        return null;
    }

    @Default
    @CatchUnknown
    public void onGuildHelpCommand(Player player) {
        this.sendHelpMessage(player);
    }

    @Subcommand("bank")
    @Syntax("<name>")
    @CommandPermission("runicadmin.guilds.bank")
    @CommandCompletion("name @nothing")
    public void onGuildModBankCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(player);
            return;
        }

        String guildName = args[0];
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(guildName);
        if (guildInfo == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have entered an invalid guild name!"));
            return;
        }

        GuildBankUtil.open(player, 1, guildInfo.getUUID());
        player.sendMessage(ColorUtil.format(this.prefix + "You have opened the bank of " + guildInfo.getName()));
    }

    @Subcommand("create")
    @Syntax("<owner> <name> <prefix>")
    @Conditions("is-op")
    @CommandCompletion("@players name prefix")
    public void onGuildModCreateCommand(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(player);
            return;
        }

        Player owner = Bukkit.getPlayer(args[0]);
        if (owner == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have entered an invalid player!"));
            return;
        }

        UUID uuid = owner.getUniqueId();

        GuildCreationResult result = RunicGuilds.getGuildsAPI().createGuild(owner, args[1], args[2], true);
        player.sendMessage(ColorUtil.format(this.prefix + "&e" + result.getMessage()));
        if (result == GuildCreationResult.SUCCESSFUL) {
            GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(owner);
            Bukkit.getServer().getPluginManager().callEvent(new GuildCreationEvent(guildInfo.getUUID(), uuid, true));
        }
    }

    @Subcommand("disband")
    @Syntax("<prefix>")
    @CommandPermission("runicadmin.guilds.disband")
    @CommandCompletion("prefix @nothing")
    public void onGuildModDisbandCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(player);
            return;
        }

        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(args[0]);
        if (guildInfo == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have entered an invalid guild!"));
            return;
        }

        UUID ownerUuid = guildInfo.getOwnerUuid();
        GuildCommandMapManager.getTransferOwnership().remove(ownerUuid);
        GuildCommandMapManager.getDisbanding().remove(ownerUuid);
        Bukkit.getServer().getPluginManager().callEvent(new GuildDisbandEvent(guildInfo.getUUID(), null, true));
        player.sendMessage(ColorUtil.format(this.prefix + "Successfully disbanded guild."));
    }

    @Subcommand("give exp")
    @Syntax("<player> <reason> <amount>")
    @CommandPermission("runicadmin.guilds.giveexp")
    @CommandCompletion("@players @reasons 0|1|-1 @nothing")
    public void onGuildModGiveEXPCommand(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(ColorUtil.format(this.prefix + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(sender);
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ColorUtil.format(this.prefix + "&cYou must enter a valid player this command!"));
            return;
        }

        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(target);
        if (guildInfo == null) {
            sender.sendMessage(ColorUtil.format(this.prefix + "&cThe targeted player must be in a guild to execute this command!"));
            return;
        }

        GuildEXPSource source = this.getGuildExpSource(args[1]);
        if (source == null) {
            sender.sendMessage(ColorUtil.format(this.prefix + "&cYou have entered an invalid source, here is a list of sources that you can use to execute this command!"));
            sender.sendMessage(ColorUtil.format(this.prefix + "&cHere are the valid sources: Order, Other"));
            sender.sendMessage(ColorUtil.format(this.prefix + "&c/giveguildexp <player> <source> <amount>"));
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ColorUtil.format(this.prefix + "&cYou have entered an invalid integer, the argument used must be an integer to execute this command!"));
            sender.sendMessage(ColorUtil.format(this.prefix + "&c/giveguildexp <player> <source> <amount>"));
            return;
        }

        GiveGuildEXPEvent event = new GiveGuildEXPEvent(guildInfo.getUUID(), amount, source);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        target.sendMessage(ColorUtil.format(GuildUtil.PREFIX + "&eYou received " + amount + " guild experience!"));
        if (sender instanceof Player) {
            sender.sendMessage(ColorUtil.format(this.prefix + "You gave " + target.getName() + "'s guild " + amount + " guild experience!"));
        }
    }

    @Subcommand("give score")
    @Syntax("<player> <amount>")
    @CommandPermission("runicadmin.guilds.givescore")
    @CommandCompletion("@players 0|1|-1 @nothing")
    public void onGuildModGiveScoreCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ColorUtil.format(this.prefix + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(sender);
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ColorUtil.format(this.prefix + "&cYou must enter a valid player this command!"));
            return;
        }

        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(target);
        if (guildInfo == null) {
            sender.sendMessage(ColorUtil.format(this.prefix + "&cThe targeted player must be in a guild to execute this command!"));
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ColorUtil.format(this.prefix + "&cYou have entered an invalid integer, the argument used must be an integer to execute this command!"));
            return;
        }

        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> RunicGuilds.getDataAPI().loadMemberData(guildInfo.getUUID(), target.getUniqueId()))
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, target, "There was an error trying to give guild score!")
                .syncLast(memberData -> {
                    Bukkit.getPluginManager().callEvent(new GuildScoreChangeEvent(guildInfo.getUUID(), memberData, amount));
                    sender.sendMessage(ColorUtil.format(this.prefix + "You have given " + target.getName() + " " + amount + " points!"));
                })
                .execute();
    }

    @Subcommand("kick")
    @Syntax("<player>")
    @CommandPermission("runicadmin.guilds.kick")
    @CommandCompletion("@players @nothing")
    public void onGuildModKickCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(player);
            return;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
        if (!offlinePlayer.hasPlayedBefore()) {
            player.sendMessage(ColorUtil.format(this.prefix + "The specified player was not found!"));
            return;
        }

        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(offlinePlayer);
        if (guildInfo == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "The specified player must be in a guild to execute this command!"));
            return;
        }

        if (guildInfo.getOwnerUuid().toString().equalsIgnoreCase(offlinePlayer.getUniqueId().toString())) {
            player.sendMessage(ColorUtil.format(this.prefix + "That user is the guild owner. To disband the guild, use /guildmod disband [prefix]."));
            return;
        }

        if (GuildBankUtil.isViewingBank(offlinePlayer.getUniqueId())) {
            GuildBankUtil.close((Player) offlinePlayer);
        }

        Bukkit.getServer().getPluginManager().callEvent(new GuildMemberKickedEvent(guildInfo.getUUID(), offlinePlayer.getUniqueId(), player.getUniqueId(), true));
        player.sendMessage(ColorUtil.format(this.prefix + "Successfully kicked guild member."));
    }

    @Subcommand("forceloadbanners")
    @Conditions("is-op")
    public void onGuildModReloadBanners(Player player) {
        new GuildBannerLoader().run();
        player.sendMessage(ColorUtil.format(this.prefix + "Force reloaded top three guild banners!"));
    }

    @Subcommand("reset")
    @Syntax("<player>")
    @CommandPermission("runicadmin.guilds.reset")
    @CommandCompletion("@players @nothing")
    public void onGuildModResetCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(player);
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have entered an invalid player!"));
            return;
        }

        UUID targetUUID = target.getUniqueId();
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(target);
        if (guildInfo == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "The specified player must be in a guild to execute this command!"));
            return;
        }

        // todo: this doesn't actually reset anything
        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> RunicGuilds.getDataAPI().loadMemberData(guildInfo.getUUID(), targetUUID))
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicGuilds failed to load guild data!")
                .syncLast(memberData -> {
                    Bukkit.getPluginManager().callEvent(new GuildScoreChangeEvent(guildInfo.getUUID(), memberData, memberData.getScore()));
                    player.sendMessage(ColorUtil.format(this.prefix + "Successfully reset guild member score."));
                })
                .execute();
    }

    private void sendHelpMessage(CommandSender sender) {
        String[] lines = new String[]{"&6Guild Moderator Commands:",
                "&e/guildmod disband &6<prefix> &r- force disbands a guild.",
                "&e/guildmod kick &6<player> &r- force kicks a player from their guild.",
                "&e/guildmod reset &6<player> &r- resets a player's guild score and guild experience.",
                "&e/guildmod create &6<owner> <name> <prefix> &r- creates a guild. &cThis is only for operators.",
                "&e/guildmod set name/prefix&r&6 <player> <text> &r- sets a player's guild's name/prefix.",
                "&e/guildmod bank &6<prefix> &r- views another guild's bank",
                "&e/guildmod give exp &6<player> <reason> <amount> &r- give a player guild experience",
                "&e/guildmod give score &6<player> <amount> &r- give a player guild score"};
        for (String line : lines) {
            sender.sendMessage(ColorUtil.format(line));
        }
    }
}
