package com.runicrealms.runicguilds.guilds;

public enum GuildStage {
    STAGE1(1),
    STAGE2(5),
    STAGE3(10),
    STAGE4(15),
    STAGE5(20),
    STAGE6(25),
    STAGE7(30);

    private final int level;

    GuildStage(int level) {
        this.level = level;
    }

    public int levelToEXP() {
        return 0; //placeholder
    }
}
