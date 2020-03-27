package com.runicrealms.runicguilds.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.runicrealms.runicguilds.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildMember;
import com.runicrealms.runicguilds.guilds.GuildRank;

public class DataFileConfiguration {

	private File file;
	private FileConfiguration config;
	private Guild cache;

	public DataFileConfiguration(String fileName) {
		this.file = new File(ConfigLoader.getGuildsFolder(), fileName);
		this.config = ConfigLoader.getYamlConfigFile(fileName, ConfigLoader.getGuildsFolder());
	}

	public void saveToFile() {
		try {
			this.config.save(this.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void save(Guild guild) {
		Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
			@Override
			public void run() {
				config.set("owner." + guild.getOwner().getUUID().toString() + ".score", guild.getOwner().getScore());
				config.set("members", null);
				for (GuildMember member : guild.getMembers()) {
					config.set("members." + member.getUUID().toString() + ".rank", member.getRank().getName());
					config.set("members." + member.getUUID().toString() + ".score", member.getScore());
				}
				config.set("prefix", guild.getGuildPrefix());
				config.set("name", guild.getGuildName());
				config.set("bank-size", guild.getBankSize());
				for (int i = 0; i < guild.getBankSize(); i++) {
					if (guild.getBank().get(i) != null) {
						config.set("bank." + i, guild.getBank().get(i));
					}
				}
				saveToFile();
			}
		});
	}

	public void deleteFile() {
		file.delete();
	}

	public File getFile() {
		return this.file;
	}

	public Guild getGuild() {
		if (this.cache == null) {
			ConfigurationSection guildMasterSec = this.config.getConfigurationSection("owner");
			UUID guildMasterUUID = UUID.fromString((String) (guildMasterSec.getKeys(false).toArray()[0]));
			GuildMember owner = new GuildMember(guildMasterUUID, GuildRank.OWNER, guildMasterSec.getInt(guildMasterUUID + ".score"), GuildUtil.getOfflinePlayerName(guildMasterUUID));
			Set<GuildMember> members = new HashSet<GuildMember>();
			if (config.contains("members")) {
				ConfigurationSection membersSec = this.config.getConfigurationSection("members");
				for (String key : membersSec.getKeys(false)) {
					members.add(new GuildMember(UUID.fromString(key), GuildRank.getByName(membersSec.getString(key + ".rank")), membersSec.getInt(key + ".score"), GuildUtil.getOfflinePlayerName(UUID.fromString(key))));
				}
			}
			List<ItemStack> items = new ArrayList<ItemStack>();
			if (config.contains("bank")) {
				for (int i = 0; i < config.getInt("bank-size"); i++) {
					if (config.contains("bank." + i)) {
						items.add(config.getItemStack("bank." + i));
					} else {
						items.add(null);
					}
				}
			} else {
				for (int i = 0; i < config.getInt("bank-size"); i++) {
					items.add(null);
				}
			}
			this.cache = new Guild(members, owner, this.config.getString("name"), this.config.getString("prefix"), items, config.getInt("bank-size"));
		}
		return this.cache;
	}

	public void setCache(Guild guild) {
		this.cache = guild;
	}

	public DataFileConfiguration clone(String prefix) {
		DataFileConfiguration newData = new DataFileConfiguration(prefix + ".yml");
		newData.setCache(this.getGuild().clone());
		return newData;
	}

}
