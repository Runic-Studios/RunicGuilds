package com.runicrealms.runicguilds.guilds;

public enum GuildStage {
    STAGE7(513000, 40),
    STAGE6(322500, 30),
    STAGE5(186000, 25),
    STAGE4(94500, 20),
    STAGE3(39000, 15),
    STAGE2(10500, 10),
    STAGE1(1092, 5),
    STAGE0(0, 5);

    private final int exp;
    private final int maxMembers;

    GuildStage(int exp, int maxMembers) {
        this.exp = exp;
        this.maxMembers = maxMembers;
    }

    public int getExp() {
        return this.exp;
    }

    public int getMaxMembers() {
        return this.maxMembers;
    }
}
