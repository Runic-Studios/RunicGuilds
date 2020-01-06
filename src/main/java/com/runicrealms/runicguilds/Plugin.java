package com.runicrealms.runicguilds;

import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {
	
	private static Plugin instance;
	
	@Override
	public void onEnable() {
		instance = this;
	}
	
	public static Plugin getInstance() {
		return instance;
	}
	
}
