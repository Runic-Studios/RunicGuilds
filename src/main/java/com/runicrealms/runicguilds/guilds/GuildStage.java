package com.runicrealms.runicguilds.guilds;

public enum GuildStage {
    STAGE7(513000, 50, "Your guild size has advanced to 50, and all guild members receive a 5% exp buff!"),
    STAGE6(322500, 35, "Your guild size has advanced to 35, and all guild members receive a 10% damage buff while fighting monsters!"),
    STAGE5(186000, 30, "Your guild size has advanced to 30, and all guild members receive 15% bonus speed while mounted! (disabled in combat)"),
    STAGE4(94500, 25, "Your guild size has advanced to 25, and all guild members receive a 10% reputation bonus!"),
    STAGE3(39000, 20, "Your guild size has advanced to 20, and you have unlocked all banner patterns!"),
    STAGE2(10500, 15, "Your guild size has advanced to 15, and you can now create a guild banner!"),
    STAGE1(1092, 10, "Your guild size has advanced to 10!"),
    STAGE0(0, 5, "");

    private final int exp;
    private final int maxMembers;
    private final String perkInfo;

    GuildStage(int exp, int maxMembers, String perkInfo) {
        this.exp = exp;
        this.maxMembers = maxMembers;
        this.perkInfo = perkInfo;
    }

    public int getExp() {
        return this.exp;
    }

    public int getMaxMembers() {
        return this.maxMembers;
    }

    public String getPerkInfo() {
        return this.perkInfo;
    }
}
