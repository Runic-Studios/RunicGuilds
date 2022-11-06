package com.runicrealms.runicguilds.command.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.api.event.GiveGuildEXPEvent;
import com.runicrealms.runicguilds.api.event.GuildCreationEvent;
import com.runicrealms.runicguilds.api.event.GuildDisbandEvent;
import com.runicrealms.runicguilds.api.event.GuildMemberKickedEvent;
import com.runicrealms.runicguilds.command.GuildCommandMapManager;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.GuildBannerLoader;
import com.runicrealms.runicguilds.guild.GuildCreationResult;
import com.runicrealms.runicguilds.guild.GuildMember;
import com.runicrealms.runicguilds.guild.stage.GuildEXPSource;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.ui.GuildBankUtil;
import com.runicrealms.runicguilds.util.GuildUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CommandAlias("guildmod")
@CommandPermission("permissions.guild-mod")
@Conditions("is-player")
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

    private String combineArgs(String[] args, int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < args.length; i++) {
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
    @Syntax("<prefix>")
    @CommandPermission("runicadmin.guilds.bank")
    @CommandCompletion("prefix @nothing")
    public void onGuildModBankCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(player);
            return;
        }

        GuildData guildData = RunicGuilds.getRunicGuildsAPI().getGuildData(args[0]);
        if (guildData == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have entered an invalid guild prefix!"));
            return;
        }

        GuildBankUtil.open(player, 1, args[0]);
        player.sendMessage(ColorUtil.format(this.prefix + "You have opened the bank of " + guildData.getGuild().getGuildName()));
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

        GuildCreationResult result = RunicGuilds.getRunicGuildsAPI().createGuild(owner, args[1], args[2], true);
        player.sendMessage(ColorUtil.format(this.prefix + "&e" + result.getMessage()));
        if (result == GuildCreationResult.SUCCESSFUL) {
            Guild guild = RunicGuilds.getRunicGuildsAPI().getGuildData(uuid).getGuild();
            GuildData.setGuildForPlayer(guild.getGuildName(), uuid.toString());
            Bukkit.getServer().getPluginManager().callEvent(new GuildCreationEvent(guild, true));
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

        if (!RunicGuilds.getRunicGuildsAPI().getGuildDataMap().containsKey(args[0])) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have entered an invalid guild!"));
            return;
        }

        Guild guild = RunicGuilds.getRunicGuildsAPI().getGuildData(args[0]).getGuild();

        if (GuildCommandMapManager.getTransferOwnership().containsKey(guild.getOwner().getUUID())) {
            GuildCommandMapManager.getTransferOwnership().remove(guild.getOwner().getUUID());
        }

        if (GuildCommandMapManager.getDisbanding().contains(guild.getOwner().getUUID())) {
            GuildCommandMapManager.getDisbanding().remove(guild.getOwner().getUUID());
        }

        for (GuildMember member : guild.getMembers()) {
            GuildData.setGuildForPlayer("None", member.getUUID().toString());
            if (GuildBankUtil.isViewingBank(member.getUUID())) {
                GuildBankUtil.close(Bukkit.getPlayer(member.getUUID()));
            }
        }

        Bukkit.getServer().getPluginManager().callEvent(new GuildDisbandEvent(guild, null, true));
        //RunicGuilds.getRunicGuildsAPI().getGuildDatas().get(args[0]).deleteData();
        // RunicGuilds.getRunicGuildsAPI().removeGuildFromCache(guild); todo: remove from memory cache
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

        GuildData guildData = RunicGuilds.getRunicGuildsAPI().getGuildData(target.getUniqueId());
        if (guildData == null) {
            sender.sendMessage(ColorUtil.format(this.prefix + "&cThe targeted player must be in a guild to execute this command!"));
            return;
        }

        Guild guild = guildData.getGuild();

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

        GiveGuildEXPEvent event = new GiveGuildEXPEvent(guild, amount, source);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        amount = event.getAmount();

        guild.setGuildExp(amount);
        // guildData.queueToSave();
        target.sendMessage(ColorUtil.format("&r&6&lGuilds »&r &eYou received " + amount + " guild experience!"));
        if (sender instanceof Player) {
            sender.sendMessage(ColorUtil.format(this.prefix + "You gave " + target.getName() + "'s guild " + amount + " guild experience!"));
        }
    }

    /*


            if (RunicGuildsAPIold.getGuild(player.getUniqueId()) != null
                && RunicCore.getPartyManager().getPlayerParty(player) != null) {

            player.getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.5f, 1.0f);
            player.sendTitle(ChatColor.GREEN + "Guild Boss Slain!",
                    ChatColor.GREEN + "Your party has earned an extra " +
                            ChatColor.YELLOW + RunicGuilds.getGuildBossManager().getKillPoints() +
                            ChatColor.GREEN + " points!",
                    20, 100, 20);
            // distribute the extra 20 points
            for (Player mem : RunicCore.getPartyManager().getPlayerParty(player).getMembersWithLeader()) {
                RunicGuildsAPIold.addPlayerScore(mem.getUniqueId(),
                        RunicGuilds.getGuildBossManager().getKillPoints() / RunicCore.getPartyManager().getPlayerParty(player).getSize());
            }
            // todo: guildmod give partyscore <caster.uuid> 100
            // todo: make 'damage table' a modular class in core
     */
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

        GuildData guildData = RunicGuilds.getRunicGuildsAPI().getGuildData(target.getUniqueId());

        Guild guild = guildData.getGuild();
        if (guild == null) {
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

        guild.increasePlayerScore(target.getUniqueId(), amount);
        // guildData.queueToSave();
        sender.sendMessage(ColorUtil.format(this.prefix + "You have given " + target.getName() + " " + amount + " points!"));
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

        UUID uuid = GuildUtil.getOfflinePlayerUUID(args[0]);
        GuildData guildData = RunicGuilds.getRunicGuildsAPI().getGuildData(uuid);

        if (guildData.getGuild() == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "The specified player must be in a guild to execute this command!"));
            return;
        }

        Guild guild = guildData.getGuild();

        if (guild.getOwner().getUUID().toString().equalsIgnoreCase(uuid.toString())) {
            player.sendMessage(ColorUtil.format(this.prefix + "That user is the guild owner. To disband the guild, use /guildmod disband [prefix]."));
            return;
        }

        if (GuildBankUtil.isViewingBank(uuid)) {
            GuildBankUtil.close(Bukkit.getPlayer(args[0]));
        }

//        if (RunicGuilds.getRunicGuildsAPI().getPlayerCache().containsKey(uuid)) {
//            RunicGuilds.getRunicGuildsAPI().getPlayerCache().put(uuid, null);
//        }

        GuildData.setGuildForPlayer("None", uuid.toString());
        guild.removeMember(uuid);
        // guildData.queueToSave();
        Bukkit.getServer().getPluginManager().callEvent(new GuildMemberKickedEvent(guild, uuid, player.getUniqueId(), true));
        player.sendMessage(ColorUtil.format(this.prefix + "Successfully kicked guild member."));
    }

    // todo: adapt to use as a MM command

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

//        UUID targetUUID = target.getUniqueId();
//        String targetCache = RunicGuilds.getRunicGuildsAPI().getPlayerCache().get(targetUUID);
//
//        if (targetCache == null) {
//            player.sendMessage(ColorUtil.format(this.prefix + "The specified player must be in a guild to execute this command!"));
//            return;
//        }
//
//        GuildData guildData = RunicGuilds.getRunicGuildsAPI().getGuildData(RunicGuilds.getRunicGuildsAPI().getPlayerCache().get(targetUUID));
//        Guild guild = guildData.getGuild();
//        guild.setPlayerScore(targetUUID, 0);
//        // guildData.queueToSave();
//        player.sendMessage(ColorUtil.format(this.prefix + "Successfully reset guild member score."));
    }

    @Subcommand("set name")
    @Conditions("is-player")
    @Syntax("<player> <name>")
    @CommandCompletion("@players name @nothing")
    public void onGuildSetNameCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(player);
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have entered an invalid player!"));
            return;
        }

//        if (RunicGuilds.getRunicGuildsAPI().getPlayerCache().get(target.getUniqueId()) == null) {
//            player.sendMessage(ColorUtil.format(this.prefix + "The targeted player must be in a guild execute this command!"));
//            return;
//        }

        GuildData guildData = RunicGuilds.getRunicGuildsAPI().getGuildData(target.getUniqueId());

        player.sendMessage(ColorUtil.format(this.prefix + RunicGuilds.getRunicGuildsAPI().renameGuild(guildData, this.combineArgs(args, 1)).getMessage()));
    }

    @Subcommand("set prefix")
    @Syntax("<player> <prefix>")
    @CommandCompletion("@players prefix @nothing")
    public void onGuildSetPrefixCommand(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have use improper arguments to execute this command!"));
            this.sendHelpMessage(player);
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(ColorUtil.format(this.prefix + "You have entered an invalid player!"));
            return;
        }

//        if (RunicGuilds.getRunicGuildsAPI().getPlayerCache().get(target.getUniqueId()) == null) {
//            player.sendMessage(ColorUtil.format("&eYou are not in a guild!")); //change
//            return;
//        }

        GuildData guildData = RunicGuilds.getRunicGuildsAPI().getGuildData(target.getUniqueId());

        player.sendMessage(ColorUtil.format(this.prefix + guildData.getGuild().updateGuildPrefix(guildData, args[1]).getMessage()));
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
