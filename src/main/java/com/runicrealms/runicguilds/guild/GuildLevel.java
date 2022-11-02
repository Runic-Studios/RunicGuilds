package com.runicrealms.runicguilds.guild;

import com.runicrealms.plugin.utilities.ChatUtils;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GuildLevel {
    private final Guild guild;
    private int guildEXP;
    private GuildStage guildStage;

    public GuildLevel(Guild guild, int guildEXP) {
        this.guild = guild;
        this.guildEXP = guildEXP;
        this.guildStage = this.expToStage();
    }

    public Guild getGuild() {
        return this.guild;
    }

    public int getGuildEXP() {
        return this.guildEXP;
    }

    public GuildStage getGuildStage() {
        return this.guildStage;
    }

    public void addGuildEXP(int guildEXP) {
        if (guildEXP + this.guildEXP > GuildStage.getMaxStage().getExp()) {
            this.guildEXP = GuildStage.getMaxStage().getExp();
        } else {
            this.guildEXP += guildEXP;
        }

        GuildStage stage = this.expToStage();

        if (stage == this.guildStage) {
            return;
        }

        String stageNumber = stage.name().substring(5);

        for (GuildMember member : this.guild.getMembersWithOwner()) {
            Player player = Bukkit.getOfflinePlayer(member.getUUID()).getPlayer();
            if (player == null) {
                continue;
            }

            player.sendMessage("");
            ChatUtils.sendCenteredMessage(player, ColorUtil.format("&6&lLEVEL UP"));
            player.sendMessage("");
            ChatUtils.sendCenteredMessage(player, ColorUtil.format("&6&lYour guild has advanced to stage " + stageNumber + "!"));
            for (String message : stage.getPerkInfo()) {
                ChatUtils.sendCenteredMessage(player, ColorUtil.format("&6&l" + message));
            }
            player.sendMessage("");
        }

        this.guildStage = stage;
    }

    private GuildStage expToStage() {
        for (GuildStage stage : GuildStage.values()) {
            if (this.guildEXP >= stage.getExp()) {
                return stage;
            }
        }
        return GuildStage.STAGE0;
    }
}
