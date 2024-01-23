package com.runicrealms.plugin.runicguilds;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import com.runicrealms.plugin.chat.RunicChat;
import com.runicrealms.plugin.common.api.GuildsAPI;
import com.runicrealms.plugin.runicguilds.api.DataAPI;
import com.runicrealms.plugin.runicguilds.api.GuildWriteOperation;
import com.runicrealms.plugin.runicguilds.api.chat.GuildChannel;
import com.runicrealms.plugin.runicguilds.command.GuildChannelCommand;
import com.runicrealms.plugin.runicguilds.command.admin.GuildModCMD;
import com.runicrealms.plugin.runicguilds.command.player.GuildCommand;
import com.runicrealms.plugin.runicguilds.guild.banner.GuildBannerLoader;
import com.runicrealms.plugin.runicguilds.guild.banner.GuildBannerUIListener;
import com.runicrealms.plugin.runicguilds.guild.banner.PostedGuildBanner;
import com.runicrealms.plugin.runicguilds.listener.GuildEventListener;
import com.runicrealms.plugin.runicguilds.listener.GuildExpBonusListener;
import com.runicrealms.plugin.runicguilds.listener.GuildScoreboardListener;
import com.runicrealms.plugin.runicguilds.listener.NpcClickListener;
import com.runicrealms.plugin.runicguilds.listener.RewardDamageListener;
import com.runicrealms.plugin.runicguilds.listener.RewardMountListener;
import com.runicrealms.plugin.runicguilds.model.DataManager;
import com.runicrealms.plugin.runicguilds.model.MongoTask;
import com.runicrealms.plugin.runicguilds.order.WorkOrderManager;
import com.runicrealms.plugin.runicguilds.order.ui.WorkOrderUIListener;
import com.runicrealms.plugin.runicguilds.shop.GuildShopManager;
import com.runicrealms.plugin.runicguilds.ui.GuildInfoUIListener;
import com.runicrealms.plugin.runicguilds.ui.GuildMembersUIListener;
import com.runicrealms.plugin.runicguilds.util.GuildBankUtil;
import com.runicrealms.plugin.runicguilds.util.PlaceholderAPI;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicrestart.event.PreShutdownEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
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
    private static MongoTask mongoTask;
    private static GuildWriteOperation guildWriteOperation;
    private static WorkOrderManager workOrderManager;

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

    public static MongoTask getMongoTask() {
        return mongoTask;
    }

    public static GuildWriteOperation getGuildWriteOperation() {
        return guildWriteOperation;
    }

    public static WorkOrderManager getWorkOrderManager() {
        return workOrderManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        try {
            workOrderManager = new WorkOrderManager();
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        DataManager dataManager = new DataManager();
        dataAPI = dataManager;
        guildWriteOperation = dataManager;
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
                        new GuildExpBonusListener(),
                        new RewardDamageListener(),
                        new GuildScoreboardListener(),
                        new RewardMountListener(),
                        new GuildInfoUIListener(),
                        new GuildMembersUIListener(),
                        new GuildEventListener(),
                        new WorkOrderUIListener()
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
        GuildChannel guildChannel = new GuildChannel();
        RunicChat.getRunicChatAPI().registerChatChannel(guildChannel); // register channels after place holders
        commandManager.registerCommand(new GuildChannelCommand(guildChannel));
        new GuildBannerLoader().runTaskTimer(this, 5 * 20L, 60 * 20L); // 10s delay, 1 min
		/*
		Shops
		 */
        new GuildShopManager();
        mongoTask = new MongoTask();

        // Anti dupe handler for guild bank
        RunicItemsAPI.registerAntiDupeInventoryHandler(player -> GuildBankUtil.isViewingBank(player.getUniqueId()));
    }

    @EventHandler
    public void onShutdown(PreShutdownEvent event) {
        instance = null;
        dataAPI = null;
        guildsAPI = null;
        commandManager = null;
        mongoTask = null;
        guildWriteOperation = null;
        workOrderManager = null;
    }

    private void registerEvents(Listener... listeners) {
        for (Listener listener : listeners) {
            this.getServer().getPluginManager().registerEvents(listener, this);
        }
    }
}
