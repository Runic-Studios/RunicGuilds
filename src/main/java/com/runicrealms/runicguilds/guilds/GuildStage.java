package com.runicrealms.runicguilds.guilds;

public enum GuildStage {
    STAGE7(513000),
    STAGE6(322500),
    STAGE5(186000),
    STAGE4(94500),
    STAGE3(39000),
    STAGE2(10500),
    STAGE1(1092),
    STAGE0(0);

    private final int exp;

    GuildStage(int exp) {
        this.exp = exp;
    }

    public int getExp() {
        return this.exp;
    }
}
