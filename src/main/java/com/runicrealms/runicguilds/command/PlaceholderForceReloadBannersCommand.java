package com.runicrealms.runicguilds.command;

import com.runicrealms.runicguilds.guilds.ForceLoadBanners;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PlaceholderForceReloadBannersCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        new ForceLoadBanners().run();
        return true;
    }
}
