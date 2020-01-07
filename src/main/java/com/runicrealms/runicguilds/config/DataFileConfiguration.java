package com.runicrealms.runicguilds.config;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

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

	private void saveToFile() {
		try {
			this.config.save(this.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void save(Guild guild) {
		this.config.set("owner." + guild.getOwner().getUUID().toString() + ".score", guild.getOwner().getScore());
		for (GuildMember member : guild.getMembers()) {
			this.config.set("members." + member.getUUID().toString() + ".rank", member.getRank().getName());
			this.config.set("members." + member.getUUID().toString() + ".score", member.getScore());
		}
		this.config.set("prefix", guild.getGuildPrefix());
		this.config.set("name", guild.getGuildName());
		this.saveToFile();
	}

	public Guild getGuild() {
		if (this.cache == null) {
			ConfigurationSection guildMasterSec = this.config.getConfigurationSection("owner");
			UUID guildMasterUUID = UUID.fromString((String) guildMasterSec.getKeys(false).toArray()[0]);
			GuildMember owner = new GuildMember(guildMasterUUID, GuildRank.OWNER, guildMasterSec.getInt(guildMasterUUID + ".score"));
			Set<GuildMember> members = new HashSet<GuildMember>();
			if (config.contains("members")) {
				ConfigurationSection membersSec = this.config.getConfigurationSection("members");
				for (String key : membersSec.getKeys(false)) {
					members.add(new GuildMember(UUID.fromString(key), GuildRank.getByName(membersSec.getString(key + ".rank")), membersSec.getInt(key + ".score")));
				}
			}
			this.cache = new Guild(members, owner, this.config.getString("name"), this.config.getString("prefix"));
		}
		return this.cache;
	}

}
