package com.runicrealms.runicguilds;

import com.runicrealms.RunicChat;
import com.runicrealms.libs.acf.ConditionFailedException;
import com.runicrealms.libs.acf.PaperCommandManager;
import com.runicrealms.libs.taskchain.BukkitTaskChainFactory;
import com.runicrealms.libs.taskchain.TaskChain;
import com.runicrealms.libs.taskchain.TaskChainFactory;
import com.runicrealms.runicguilds.api.DataAPI;
import com.runicrealms.runicguilds.api.GuildsAPI;
import com.runicrealms.runicguilds.api.chat.GuildChannel;
import com.runicrealms.runicguilds.command.admin.GuildModCMD;
import com.runicrealms.runicguilds.command.player.GuildCommand;
import com.runicrealms.runicguilds.guild.GuildBannerLoader;
import com.runicrealms.runicguilds.guild.PostedGuildBanner;
import com.runicrealms.runicguilds.listener.*;
import com.runicrealms.runicguilds.model.DataManager;
import com.runicrealms.runicguilds.shop.GuildShopManager;
import com.runicrealms.runicguilds.ui.GuildBannerUIListener;
import com.runicrealms.runicguilds.ui.GuildInfoUIListener;
import com.runicrealms.runicguilds.ui.GuildMembersUIListener;
import com.runicrealms.runicguilds.util.GuildBankUtil;
import com.runicrealms.runicguilds.util.PlaceholderAPI;
import com.runicrealms.runicrestart.event.ServerShutdownEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RunicGuilds extends JavaPlugin implements Listener {

    private static final Set<UUID> playersCreatingGuild = new HashSet<>();
    private static final Set<PostedGuildBanner> postedGuildBanners = new HashSet<>();
    public static int GUILD_COST;
    public static int MAX_BANK_PAGES;
    public static List<Integer> GUILD_BANKERS;
    public static List<Integer> GUILD_HERALDS;
    public static List<Integer> GUILD_VENDORS;
    private static RunicGuilds instance;
    private static DataAPI dataAPI;
    private static GuildsAPI guildsAPI;
    private static PaperCommandManager commandManager;
    private static TaskChainFactory taskChainFactory;

    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    public static Set<UUID> getPlayersCreatingGuild() {
        return playersCreatingGuild;
    }

    public static Set<PostedGuildBanner> getPostedGuildBanners() {
        return postedGuildBanners;
    }

    public static RunicGuilds getInstance() {
        return instance;
    }

    public static PaperCommandManager getCommandManager() {
        return commandManager;
    }

    public static DataAPI getDataAPI() {
        return dataAPI;
    }

    public static GuildsAPI getGuildsAPI() {
        return guildsAPI;
    }

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        dataAPI = new DataManager();
        guildsAPI = new GuildManager();
        taskChainFactory = BukkitTaskChainFactory.create(this);
        GUILD_BANKERS = this.getConfig().getIntegerList("guild-bankers");
        GUILD_HERALDS = this.getConfig().getIntegerList("guild-heralds");
        GUILD_VENDORS = this.getConfig().getIntegerList("guild-vendors");
        GUILD_COST = this.getConfig().getInt("guild-cost");
        MAX_BANK_PAGES = this.getConfig().getInt("max-bank-pages");

		/*
		Events
		 */
        this.registerEvents
                (
                        this,
                        new GuildBankUtil(),
                        new NpcClickListener(),
                        new GuildBannerUIListener(),
                        new BannerClickListener(),
                        new RewardExpListener(),
                        new RewardDamageListener(),
                        new RewardMountListener(),
                        new GuildInfoUIListener(),
                        new GuildMembersUIListener(),
                        new GuildEventListener()
                );

		/*
		Commands
		 */
        commandManager = new PaperCommandManager(this);
        commandManager.getCommandConditions().addCondition("is-player", context -> {
            if (!(context.getIssuer().getIssuer() instanceof Player))
                throw new ConditionFailedException("This command cannot be run from console!");
        });
        commandManager.getCommandConditions().addCondition("is-op", context -> {
            if (!context.getIssuer().getIssuer().isOp())
                throw new ConditionFailedException("You must be an operator to run this command!");
        });
        commandManager.registerCommand(new GuildCommand());
        commandManager.registerCommand(new GuildModCMD());

        // register placeholder tags
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPI().register();
        }
        RunicChat.getRunicChatAPI().registerChatChannel(new GuildChannel()); // register channels after place holders
        new GuildBannerLoader().runTaskTimerAsynchronously(this, 10 * 20L, 60 * 20L); // 1 min
		/*
		Shops
		 */
        new GuildShopManager();
    }

    @EventHandler
    public void onShutdown(ServerShutdownEvent event) {
        instance = null;
        dataAPI = null;
        guildsAPI = null;
        commandManager = null;
    }

    private void registerEvents(Listener... listeners) {
        for (Listener listener : listeners) {
            this.getServer().getPluginManager().registerEvents(listener, this);
        }
    }
}
