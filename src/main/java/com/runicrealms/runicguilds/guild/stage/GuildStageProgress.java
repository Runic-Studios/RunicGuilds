package com.runicrealms.runicguilds.guild.stage;

import com.runicrealms.plugin.utilities.ChatUtils;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.GuildMember;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * A wrapper class that holds a guild, its exp, and its current stage
 *
 * @author Skyfallin
 */
public class GuildStageProgress {
    private final Guild guild;
    private int guildEXP;
    private GuildStage guildStage;

    /**
     * Initialize a stage progress wrapper with the guild and its current exp
     *
     * @param guild    any guild
     * @param guildEXP the current exp of the guild
     */
    public GuildStageProgress(Guild guild, int guildEXP) {
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

    /**
     * @param guildEXP
     */
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
            if (!stage.getStageReward().getMessage().equalsIgnoreCase("")) {
                ChatUtils.sendCenteredMessage(player, ColorUtil.format("&6&l" + stage.getStageReward().getMessage()));
            }
            player.sendMessage("");
        }

        this.guildStage = stage;
    }

    /**
     * @return
     */
    private GuildStage expToStage() {
        for (GuildStage stage : GuildStage.values()) {
            if (this.guildEXP >= stage.getExp()) {
                return stage;
            }
        }
        return GuildStage.STAGE0;
    }
}
