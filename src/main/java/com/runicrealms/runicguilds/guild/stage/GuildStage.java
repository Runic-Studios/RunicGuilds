package com.runicrealms.runicguilds.guild.stage;

public enum GuildStage {
    STAGE5("Stage 5", 5, 186000, 35, StageReward.EXP_BONUS),
    STAGE4("Stage 4", 4, 94500, 30, StageReward.COMBAT_BONUS),
    STAGE3("Stage 3", 3, 39000, 25, StageReward.MOUNT_SPEED_BONUS),
    STAGE2("Stage 2", 2, 10500, 20, StageReward.BANNER),
    STAGE1("Stage 1", 1, 1092, 15, StageReward.NONE),
    STAGE0("Stage 0", 0, 0, 10, StageReward.NONE);

    private final String name;
    private final int rank;
    private final int exp;
    private final int maxMembers;
    private final StageReward stageReward;

    /**
     * @param name
     * @param rank
     * @param exp
     * @param maxMembers
     * @param stageReward
     */
    GuildStage(String name, int rank, int exp, int maxMembers, StageReward stageReward) {
        this.name = name;
        this.rank = rank;
        this.exp = exp;
        this.maxMembers = maxMembers;
        this.stageReward = stageReward;
    }

    public String getName() {
        return name;
    }

    public int getRank() {
        return rank;
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
