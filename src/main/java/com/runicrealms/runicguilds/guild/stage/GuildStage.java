package com.runicrealms.runicguilds.guild.stage;

public enum GuildStage {
    STAGE5(186000, 35, StageReward.EXP_BONUS),
    STAGE4(94500, 30, StageReward.COMBAT_BONUS),
    STAGE3(39000, 25, StageReward.MOUNT_SPEED_BONUS),
    STAGE2(10500, 20, StageReward.BANNER),
    STAGE1(1092, 15, StageReward.NONE),
    STAGE0(0, 10, StageReward.NONE);

    private final int exp;
    private final int maxMembers;
    private final StageReward stageReward;

    /**
     * @param exp
     * @param maxMembers
     * @param stageReward
     */
    GuildStage(int exp, int maxMembers, StageReward stageReward) {
        this.exp = exp;
        this.maxMembers = maxMembers;
        this.stageReward = stageReward;
    }

    public int getExp() {
        return this.exp;
    }

    public int getMaxMembers() {
        return this.maxMembers;
    }

    public StageReward getStageReward() {
        return this.stageReward;
    }

    public static GuildStage getMaxStage() {
        return STAGE5;
    }
}
