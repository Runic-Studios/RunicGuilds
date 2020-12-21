package com.runicrealms.runicguilds.guilds;

public class GuildLevel {
    private int guildEXP;
    private GuildStage guildStage;

    public GuildLevel(int guildEXP) {
        this.guildEXP = guildEXP;
        this.guildStage = this.expToStage();
    }

    public int getGuildEXP() {
        return this.guildEXP;
    }

    public GuildStage getGuildStage() {
        return this.guildStage;
    }

    public void setGuildEXP(int guildEXP) {
        if (guildEXP > GuildStage.STAGE7.getExp()) {
            guildEXP = GuildStage.STAGE7.getExp();
        }
        this.guildEXP = guildEXP;
        this.guildStage = this.expToStage();
    }

    public void addGuildEXP(int guildEXP) {
        if (guildEXP + this.guildEXP > GuildStage.STAGE7.getExp()) {
            this.guildEXP = GuildStage.STAGE7.getExp();
        } else {
            this.guildEXP += guildEXP;
        }
        this.guildStage = this.expToStage();
    }

    private GuildStage expToStage() {
        for (GuildStage stage : GuildStage.values()) {
            if (this.guildEXP >= stage.getExp()) {
                return stage;
            }
        }
        return null;
    }
}
