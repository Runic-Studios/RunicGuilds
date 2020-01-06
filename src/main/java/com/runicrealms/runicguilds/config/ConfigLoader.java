package com.runicrealms.runicguilds.config;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.runicrealms.runicguilds.Plugin;

public class ConfigLoader {
	
	public static FileConfiguration getYamlConfigFile(String fileName, File folder) {
		FileConfiguration config;
		File file;
		file = new File(folder, fileName);
		config = new YamlConfiguration();
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			config.load(file);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return config;
	}

	public static File getSubFolder(File folder, String subfolder) {
		for (File file : folder.listFiles()) {
			if (file.getName().equalsIgnoreCase(subfolder)) {
				return file;
			}
		}
		return null;
	}
	
	public static void initDirs() {
		if (!Plugin.getInstance().getDataFolder().exists()) {
			Plugin.getInstance().getDataFolder().mkdir();
		}
		File folder = ConfigLoader.getSubFolder(Plugin.getInstance().getDataFolder(), "guilds");
		if (folder == null) {
			folder = new File(Plugin.getInstance().getDataFolder(), "guilds");
			folder.mkdir();
		}
	}

	public static File getGuildsFolder() {
		return ConfigLoader.getSubFolder(Plugin.getInstance().getDataFolder(), "guilds");
	}
	
}
