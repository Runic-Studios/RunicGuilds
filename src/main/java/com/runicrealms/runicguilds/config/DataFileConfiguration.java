package com.runicrealms.runicguilds.config;

import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildMember;
import com.runicrealms.runicguilds.guilds.GuildRank;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;

public class DataFileConfiguration {

    private File file;
    private FileConfiguration config;
    private Guild guild;

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
        this.config.set("owner." + guild.getOwner().getUUID().toString() + ".rank", GuildRank.OWNER);
        for (GuildMember member : guild.getMembers()) {
            this.config.set("members." + member.getUUID().toString() + ".rank", member.getRank().getName());
            this.config.set("members." + member.getUUID().toString() + ".score", member.getScore());
        }
        this.config.set("prefix", guild.getGuildPrefix());
        this.config.set("name", guild.getGuildName());
        this.saveToFile();
    }

}
