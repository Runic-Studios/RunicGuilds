package com.runicrealms.runicguilds;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import com.runicrealms.RunicChat;
import com.runicrealms.runicguilds.boss.GuildBossListener;
import com.runicrealms.runicguilds.boss.GuildBossManager;
import com.runicrealms.runicguilds.chat.GuildChannel;
import com.runicrealms.runicguilds.command.GuildCommand;
import com.runicrealms.runicguilds.command.GuildModCommand;
import com.runicrealms.runicguilds.gui.GuildBankUtil;
import com.runicrealms.runicguilds.guilds.BannerClickListener;
import com.runicrealms.runicguilds.guilds.GuildBannerLoader;
import com.runicrealms.runicguilds.guilds.GuildBannerUIListener;
import com.runicrealms.runicguilds.guilds.PostedGuildBanner;
import com.runicrealms.runicguilds.listeners.GuildRewardDamageListener;
import com.runicrealms.runicguilds.listeners.GuildRewardExpListener;
import com.runicrealms.runicguilds.listeners.NpcClickListener;
import com.runicrealms.runicguilds.listeners.PlayerJoinListener;
import com.runicrealms.runicguilds.model.DataListener;
import com.runicrealms.runicguilds.shop.GuildShopManager;
import com.runicrealms.runicguilds.util.GuildUtil;
import com.runicrealms.runicguilds.util.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class Plugin extends JavaPlugin implements Listener {

    private static Plugin instance;
    private static PaperCommandManager commandManager;
    private static GuildBossManager guildBossManager;
    private static final Set<UUID> playersCreatingGuild = new HashSet<>();
    private static final Set<PostedGuildBanner> postedGuildBanners = new HashSet<>();

    public static int GUILD_COST;
    public static int MAX_BANK_PAGES;
    public static List<Integer> GUILD_BANKERS;
    public static List<Integer> GUILD_HERALDS;
    public static List<Integer> GUILD_VENDORS;

    @Override
    public void onEnable() {

        instance = this;
        guildBossManager = new GuildBossManager();
        this.saveDefaultConfig();
        GuildUtil.loadGuilds(); // marks plugin loaded for RunicRestart
        Bukkit.getLogger().log(Level.INFO, "[RunicGuilds] All guilds have been loaded!");
        GUILD_BANKERS = this.getConfig().getIntegerList("guild-bankers");
        GUILD_HERALDS = this.getConfig().getIntegerList("guild-heralds");
        GUILD_VENDORS = this.getConfig().getIntegerList("guild-vendors");
        GUILD_COST = this.getConfig().getInt("guild-cost");
        MAX_BANK_PAGES = this.getConfig().getInt("max-bank-pages");
        PlayerJoinListener.initializePlayerCache();

		/*
		Events
		 */
        this.registerEvents
                (
                        this,
                        new PlayerJoinListener(),
                        new GuildBankUtil(),
                        new NpcClickListener(),
                        new DataListener(),
                        new GuildBossListener(),
                        new GuildBannerUIListener(),
                        new BannerClickListener(),
                        new GuildRewardExpListener(),
                        new GuildRewardDamageListener()
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
        commandManager.registerCommand(new GuildModCommand());

        // register placeholder tags
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPI().register();
        }
        RunicChat.getRunicChatAPI().registerChatChannel(new GuildChannel()); // register channels after place holders
        new GuildBannerLoader().runTaskTimerAsynchronously(this, 1200, 72000);
		/*
		Shops
		 */
        new GuildShopManager();
    }

    // todo: this
//	@EventHandler
//	public void onShutdown(ServerShutdownEvent event) {
//		TaskSavingQueue.emptyQueue();
//		getLogger().info(" §cRunicGuilds has been disabled.");
//		RunicRestartApi.markPluginSaved("guilds");
//		guildBossManager = null;
//		instance = null;
//	}

    public static Set<UUID> getPlayersCreatingGuild() {
        return playersCreatingGuild;
    }

    public static Set<PostedGuildBanner> getPostedGuildBanners() {
        return postedGuildBanners;
    }

    public static Plugin getInstance() {
        return instance;
    }

    public static PaperCommandManager getCommandManager() {
        return commandManager;
    }

    public static GuildBossManager getGuildBossManager() {
        return guildBossManager;
    }

    private void registerEvents(Listener... listeners) {
        for (Listener listener : listeners) {
            this.getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    private void registerCommands() {

    }

}
