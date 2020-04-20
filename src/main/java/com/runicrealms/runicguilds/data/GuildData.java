package com.runicrealms.runicguilds.data;

import com.mongodb.client.model.Filters;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.GuildMongoData;
import com.runicrealms.plugin.database.MongoData;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildMember;
import com.runicrealms.runicguilds.guilds.GuildRank;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class GuildData {

    private Guild guild;
    private String prefix;
    private MongoData guildData;

    public GuildData(Guild guild) {
        this.prefix = guild.getGuildPrefix();
        RunicCore.getDatabaseManager().getGuildData().insertOne(new Document("prefix", guild.getGuildPrefix()));
        this.guildData = new GuildMongoData(guild.getGuildPrefix());
        this.save(guild);
    }

    public GuildData(String prefix) {
        this.prefix = prefix;
        this.guildData = new GuildMongoData(prefix);
        MongoDataSection ownerSection = this.guildData.getSection("owner");
        UUID ownerUuid = UUID.fromString(ownerSection.getKeys().iterator().next());
        GuildMember owner = new GuildMember(ownerUuid, GuildRank.OWNER, ownerSection.get(ownerUuid.toString() + ".score", Integer.class), GuildUtil.getOfflinePlayerName(ownerUuid));
        Set<GuildMember> members = new HashSet<GuildMember>();
        if (this.guildData.has("members")) {
            MongoDataSection membersSection = this.guildData.getSection("members");
            for (String key : membersSection.getKeys()) {
                members.add(new GuildMember(UUID.fromString(key), GuildRank.getByName(membersSection.get(key + ".rank", String.class)), membersSection.get(key + ".score", Integer.class), GuildUtil.getOfflinePlayerName(UUID.fromString(key))));
            }
        }
        List<ItemStack> items = new ArrayList<ItemStack>();
        if (this.guildData.has("bank")) {
            for (int i = 0; i < this.guildData.get("bank-size", Integer.class); i++) {
                if (this.guildData.has("bank." + i)) {
                    items.add(deserializeItemStack(this.guildData.get("bank." + i, String.class)));
                } else {
                    items.add(null);
                }
            }
        } else {
            for (int i = 0; i < this.guildData.get("bank-size", Integer.class); i++) {
                items.add(null);
            }
        }
        this.guild = new Guild(members, owner, this.guildData.get("name", String.class), this.guildData.get("prefix", String.class), items, this.guildData.get("bank-size", Integer.class));
    }

    public Guild getData() {
        return this.guild;
    }

    public void queueToSave() {
        TaskSavingQueue.add(this);
    }

    public void save(Guild guild) {
        this.guild = guild;
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                guildData.set("owner." + guild.getOwner().getUUID().toString() + ".score", guild.getOwner().getScore());
                guildData.remove("members");
                guildData.save();
                for (GuildMember member : guild.getMembers()) {
                    guildData.set("members." + member.getUUID().toString() + ".rank", member.getRank().getName());
                    guildData.set("members." + member.getUUID().toString() + ".score", member.getScore());
                }
                guildData.set("prefix", guild.getGuildPrefix());
                guildData.set("name", guild.getGuildName());
                guildData.set("bank-size", guild.getBankSize());
                for (int i = 0; i < guild.getBankSize(); i++) {
                    if (guild.getBank().get(i) != null) {
                        guildData.set("bank." + i, serializeItemStack(guild.getBank().get(i)));
                    }
                }
                guildData.save();
            }
        });
    }

    public void saveSync(Guild guild) {
        this.guild = guild;
        guildData.set("owner." + guild.getOwner().getUUID().toString() + ".score", guild.getOwner().getScore());
        guildData.set("members", null);
        for (GuildMember member : guild.getMembers()) {
            guildData.set("members." + member.getUUID().toString() + ".rank", member.getRank().getName());
            guildData.set("members." + member.getUUID().toString() + ".score", member.getScore());
        }
        guildData.set("prefix", guild.getGuildPrefix());
        guildData.set("name", guild.getGuildName());
        guildData.set("bank-size", guild.getBankSize());
        for (int i = 0; i < guild.getBankSize(); i++) {
            if (guild.getBank().get(i) != null) {
                guildData.set("bank." + i, serializeItemStack(guild.getBank().get(i)));
            }
        }
        guildData.save();
    }

    private static String serializeItemStack(ItemStack item) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private static ItemStack deserializeItemStack(String item) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(item));
        try {
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            dataInput.close();
            return (ItemStack) dataInput.readObject();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public void setGuild(Guild guild) {
        this.guild = guild;
    }

    public void deleteData() {
        RunicCore.getDatabaseManager().getGuildData().deleteOne(Filters.eq("preifx", this.prefix));
    }

}
