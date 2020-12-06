package com.runicrealms.runicguilds;

import com.runicrealms.RunicChat;
import com.runicrealms.runicguilds.api.RunicGuildsAPI;
import com.runicrealms.runicguilds.boss.GuildBossListener;
import com.runicrealms.runicguilds.boss.GuildBossManager;
import com.runicrealms.runicguilds.chat.GuildChannel;
import com.runicrealms.runicguilds.command.OldGuildCommand;
import com.runicrealms.runicguilds.command.OldGuildModCommand;
import com.runicrealms.runicguilds.data.GuildUtil;
import com.runicrealms.runicguilds.data.TaskSavingQueue;
import com.runicrealms.runicguilds.event.EventClickNpc;
import com.runicrealms.runicguilds.event.EventPlayerJoinQuit;
import com.runicrealms.runicguilds.gui.GuildBankUtil;
import com.runicrealms.runicguilds.guilds.GuildBannerUIListener;
import com.runicrealms.runicguilds.listeners.DataListener;
import com.runicrealms.runicguilds.shop.GuildHeraldShop;
import com.runicrealms.runicguilds.shop.GuildShopManager;
import com.runicrealms.runicguilds.util.PlaceholderAPI;
import com.runicrealms.runicrestart.api.RunicRestartApi;
import com.runicrealms.runicrestart.api.ServerShutdownEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
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
	 */
	
	private static Plugin instance;
	private static GuildBossManager guildBossManager;
	private static final Set<UUID> playersCreatingGuild = new HashSet<>();

	public static List<Integer> GUILD_HERALDS;
	public static int GUILD_COST;
	public static List<Integer> GUILD_BANKERS;
	public static int MAX_BANK_PAGES;
	
	@Override
	public void onEnable() {
		instance = this;
		guildBossManager = new GuildBossManager();
		FileConfiguration config = this.getConfig();
		config.options().copyDefaults(true);
		this.saveDefaultConfig();
		GuildUtil.loadGuilds(); // marks plugin loaded for RunicRestart
		Bukkit.getLogger().log(Level.INFO, "[RunicGuilds] All guilds have been loaded!");
		GUILD_HERALDS = this.getConfig().getIntegerList("guild-heralds");
		GUILD_COST = this.getConfig().getInt("guild-cost");
		GUILD_BANKERS = this.getConfig().getIntegerList("guild-bankers");
		MAX_BANK_PAGES = this.getConfig().getInt("max-bank-pages");
		EventPlayerJoinQuit.initializePlayerCache();
		//this.getServer().getPluginManager().registerEvents(this, this);
		this.getServer().getPluginManager().registerEvents(new EventPlayerJoinQuit(), this);
		this.getServer().getPluginManager().registerEvents(new GuildBankUtil(), this);
		this.getServer().getPluginManager().registerEvents(new EventClickNpc(), this);
		this.getServer().getPluginManager().registerEvents(new DataListener(), this);
		this.getServer().getPluginManager().registerEvents(new GuildShopManager(), this);
		this.getServer().getPluginManager().registerEvents(new GuildBossListener(), this);
		this.getServer().getPluginManager().registerEvents(new GuildBannerUIListener(), this);
		this.getCommand("guild").setExecutor(new OldGuildCommand()); //remove later
		this.getCommand("guildmod").setExecutor(new OldGuildModCommand()); //remove later
		TaskSavingQueue.scheduleTask();
		// register placeholder tags
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			new PlaceholderAPI().register();
		}
		RunicChat.getRunicChatAPI().registerChatChannel(new GuildChannel()); // register channels after place holders
		RunicGuildsAPI.registerGuildShop(new GuildHeraldShop());
	}

	@Override
	public void onDisable() {
		TaskSavingQueue.emptyQueue();
		getLogger().info(" §cRunicGuilds has been disabled.");
		RunicRestartApi.markPluginSaved("guilds");
		guildBossManager = null;
		instance = null;
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

	public static Plugin getInstance() {
		return instance;
	}

	public static GuildBossManager getGuildBossManager() {
		return guildBossManager;
	}

}
