package com.runicrealms.runicguilds;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import com.runicrealms.RunicChat;
import com.runicrealms.runicguilds.api.chat.GuildChannel;
import com.runicrealms.runicguilds.command.admin.GuildModCMD;
import com.runicrealms.runicguilds.command.player.GuildCommand;
import com.runicrealms.runicguilds.guild.BannerClickListener;
import com.runicrealms.runicguilds.guild.GuildBannerLoader;
import com.runicrealms.runicguilds.guild.PostedGuildBanner;
import com.runicrealms.runicguilds.listeners.*;
import com.runicrealms.runicguilds.model.GuildDataManager;
import com.runicrealms.runicguilds.shop.GuildShopManager;
import com.runicrealms.runicguilds.ui.GuildBankUtil;
import com.runicrealms.runicguilds.ui.GuildBannerUIListener;
import com.runicrealms.runicguilds.ui.GuildInfoUIListener;
import com.runicrealms.runicguilds.ui.GuildMembersUIListener;
import com.runicrealms.runicguilds.util.GuildUtil;
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
import java.util.logging.Level;

public class RunicGuilds extends JavaPlugin implements Listener {

    private static RunicGuilds instance;
    private static PaperCommandManager commandManager;
    private static final Set<UUID> playersCreatingGuild = new HashSet<>();
    private static final Set<PostedGuildBanner> postedGuildBanners = new HashSet<>();

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

    public static int GUILD_COST;
    public static int MAX_BANK_PAGES;
    public static List<Integer> GUILD_BANKERS;
    public static List<Integer> GUILD_HERALDS;
    public static List<Integer> GUILD_VENDORS;

    @Override
    public void onEnable() {

        instance = this;
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
                        new GuildDataManager(),
                        new GuildBannerUIListener(),
                        new BannerClickListener(),
                        new RewardExpListener(),
                        new RewardDamageListener(),
                        new RewardMountListener(),
                        new GuildInfoUIListener(),
                        new GuildMembersUIListener()
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
        new GuildBannerLoader().runTaskTimerAsynchronously(this, 1200, 72000);
		/*
		Shops
		 */
        new GuildShopManager();
    }

    @EventHandler
    public void onShutdown(ServerShutdownEvent event) {
        instance = null;
        commandManager = null;
    }

    private void registerEvents(Listener... listeners) {
        for (Listener listener : listeners) {
            this.getServer().getPluginManager().registerEvents(listener, this);
        }
    }

}
