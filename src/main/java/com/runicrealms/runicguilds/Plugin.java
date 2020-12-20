package com.runicrealms.runicguilds;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import com.runicrealms.RunicChat;
import com.runicrealms.runicguilds.api.RunicGuildsAPI;
import com.runicrealms.runicguilds.boss.GuildBossListener;
import com.runicrealms.runicguilds.boss.GuildBossManager;
import com.runicrealms.runicguilds.chat.GuildChannel;
import com.runicrealms.runicguilds.command.GuildCommand;
import com.runicrealms.runicguilds.command.GuildModCommand;
import com.runicrealms.runicguilds.command.OldGuildCommand;
import com.runicrealms.runicguilds.command.OldGuildModCommand;
import com.runicrealms.runicguilds.command.PlaceholderForceReloadBannersCommand;
import com.runicrealms.runicguilds.command.PlaceholderMakeGuildBannerCommand;
import com.runicrealms.runicguilds.data.GuildUtil;
import com.runicrealms.runicguilds.data.TaskSavingQueue;
import com.runicrealms.runicguilds.event.EventClickNpc;
import com.runicrealms.runicguilds.event.EventPlayerJoinQuit;
import com.runicrealms.runicguilds.gui.GuildBankUtil;
import com.runicrealms.runicguilds.guilds.BannerClickListener;
import com.runicrealms.runicguilds.guilds.ForceLoadBanners;
import com.runicrealms.runicguilds.guilds.GuildBannerUIListener;
import com.runicrealms.runicguilds.guilds.PostedGuildBanner;
import com.runicrealms.runicguilds.listeners.DataListener;
import com.runicrealms.runicguilds.shop.GuildHeraldShop;
import com.runicrealms.runicguilds.shop.GuildShopManager;
import com.runicrealms.runicguilds.util.PlaceholderAPI;
import com.runicrealms.runicrestart.api.RunicRestartApi;
import com.runicrealms.runicrestart.api.ServerShutdownEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class Plugin extends JavaPlugin implements Listener {

	/*
	TODO:
	1: Fill in the blanks of the new commands
	2: Go into GuildBannerUIListener.java and find the comment that says "save new guild in cache somewhere no clue" (use command f)
	3: Finish guild exp methods in GuildStage.java and Guild.java
	4: Add logic to all methods that need it to check GuildStage enum and do something if it is the right stage or do not do something if they are too low
	 */
	
	private static Plugin instance;
	private static PaperCommandManager commandManager;
	private static GuildBossManager guildBossManager;
	private static final Set<UUID> playersCreatingGuild = new HashSet<>();
	private static final Set<PostedGuildBanner> postedGuildBanners = new HashSet<>();

	public static List<Integer> GUILD_HERALDS;
	public static int GUILD_COST;
	public static List<Integer> GUILD_BANKERS;
	public static int MAX_BANK_PAGES;
	
	@Override
	public void onEnable() {
		instance = this;
		guildBossManager = new GuildBossManager();
		this.saveDefaultConfig();
		GuildUtil.loadGuilds(); // marks plugin loaded for RunicRestart
		Bukkit.getLogger().log(Level.INFO, "[RunicGuilds] All guilds have been loaded!");
		GUILD_HERALDS = this.getConfig().getIntegerList("guild-heralds");
		GUILD_COST = this.getConfig().getInt("guild-cost");
		GUILD_BANKERS = this.getConfig().getIntegerList("guild-bankers");
		MAX_BANK_PAGES = this.getConfig().getInt("max-bank-pages");
		EventPlayerJoinQuit.initializePlayerCache();
		//Events
		this.registerEvents(this, new EventPlayerJoinQuit(), new GuildBankUtil(), new EventClickNpc(), new DataListener(),
				new GuildShopManager(), new GuildBossListener(), new GuildBannerUIListener(), new BannerClickListener());

		commandManager = new PaperCommandManager(this);
		commandManager.getCommandConditions().addCondition("is-player", context -> {
			if (!(context.getIssuer().getIssuer() instanceof Player)) throw new ConditionFailedException("This command cannot be run from console!");
		});
		commandManager.getCommandConditions().addCondition("is-op", context -> {
			if (!context.getIssuer().getIssuer().isOp()) throw new ConditionFailedException("You must be an operator to run this command!");
		});

		//this.getCommand("guild").setExecutor(new OldGuildCommand()); //remove later
        commandManager.registerCommand(new GuildCommand()); // New Command registering
		//this.getCommand("guildmod").setExecutor(new OldGuildModCommand()); //remove later
		commandManager.registerCommand(new GuildModCommand());
		this.getCommand("forcereloadbanners").setExecutor(new PlaceholderForceReloadBannersCommand()); //remove later
		this.getCommand("makeguildbanner").setExecutor(new PlaceholderMakeGuildBannerCommand()); //remove later

		TaskSavingQueue.scheduleTask();
		// register placeholder tags
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			new PlaceholderAPI().register();
		}
		RunicChat.getRunicChatAPI().registerChatChannel(new GuildChannel()); // register channels after place holders
		RunicGuildsAPI.registerGuildShop(new GuildHeraldShop());
		new ForceLoadBanners().runTaskTimer(this, 400, 72000);
	}

	@EventHandler
	public void onShutdown(ServerShutdownEvent event) {
		TaskSavingQueue.emptyQueue();
		getLogger().info(" §cRunicGuilds has been disabled.");
		RunicRestartApi.markPluginSaved("guilds");
		guildBossManager = null;
		instance = null;
	}

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

}
