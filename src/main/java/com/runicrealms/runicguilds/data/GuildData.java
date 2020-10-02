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
    private final String prefix;
    private final MongoData guildData;

    public GuildData(Guild guild) {
        this(guild, true);
    }

    public GuildData(Guild guild, boolean createNewDocument) {
        this.prefix = guild.getGuildPrefix();
        if (createNewDocument) {
            RunicCore.getDatabaseManager().getGuildData().insertOne(new Document("prefix", guild.getGuildPrefix()));
        }
        this.guildData = new GuildMongoData(guild.getGuildPrefix());
        this.save(guild, true);
    }

    public GuildData(String prefix) {
        this.prefix = prefix;
        this.guildData = new GuildMongoData(prefix);
        MongoDataSection ownerSection = this.guildData.getSection("owner");
        UUID ownerUuid = UUID.fromString(ownerSection.getKeys().iterator().next());
        GuildMember owner = new GuildMember(ownerUuid, GuildRank.OWNER, ownerSection.get(ownerUuid.toString() + ".score", Integer.class), GuildUtil.getOfflinePlayerName(ownerUuid));
        Set<GuildMember> members = new HashSet<>();
        if (this.guildData.has("members")) {
            MongoDataSection membersSection = this.guildData.getSection("members");
            for (String key : membersSection.getKeys()) {
                members.add(new GuildMember(UUID.fromString(key), GuildRank.getByName(membersSection.get(key + ".rank", String.class)), membersSection.get(key + ".score", Integer.class), GuildUtil.getOfflinePlayerName(UUID.fromString(key))));
            }
        }
        List<ItemStack> items = new ArrayList<>();
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
        Map<GuildRank, Boolean> bankPermissions = new HashMap<GuildRank, Boolean>();
        if (this.guildData.has("settings")) {
            if (this.guildData.getSection("settings").has("bank-access")) {
                for (String key : this.guildData.getSection("settings.bank-access").getKeys()) {
                    GuildRank rank = GuildRank.getByIdentifier(key);
                    if (rank != null && rank != GuildRank.OWNER) {
                        bankPermissions.put(rank, this.guildData.get("settings.bank-access." + key, Boolean.class));
                    }
                }
            }
        }
        for (GuildRank rank : GuildRank.values()) {
            if (rank != GuildRank.OWNER && !bankPermissions.containsKey(rank)) {
                bankPermissions.put(rank, rank.canAccessBankByDefault());
            }
        }
        this.guild = new Guild(members, owner, this.guildData.get("name", String.class), this.guildData.get("prefix", String.class), items, this.guildData.get("bank-size", Integer.class), bankPermissions);
    }

    public Guild getData() {
        return this.guild;
    }

    public MongoData getMongoData() {
        return this.guildData;
    }

    public void queueToSave() {
        TaskSavingQueue.add(this);
    }

    public void save(Guild guild, boolean saveAsync) {
        this.guild = guild;
        if (saveAsync) {
            Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> {
                this.save(guild);
            });
        } else {
            this.save(guild);
        }
    }

    private void save(Guild guild) {
        guildData.remove("members");
        guildData.remove("bank");
        if (guildData.has("owner") && !guildData.getSection("owner").getKeys().iterator().next().equalsIgnoreCase(guild.getOwner().getUUID().toString())) {
            guildData.remove("owner");
        }
        guildData.save();
        guildData.set("owner." + guild.getOwner().getUUID().toString() + ".score", guild.getOwner().getScore());
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
        guildData.set("score", guild.getScore());
        for (GuildRank rank : this.guild.getBankAccess().keySet()) {
            guildData.set("settings.bank-access." + rank.getIdentifier(), this.guild.canAccessBank(rank));
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
        RunicCore.getDatabaseManager().getGuildData().deleteOne(Filters.eq("prefix", this.prefix));
    }

}
