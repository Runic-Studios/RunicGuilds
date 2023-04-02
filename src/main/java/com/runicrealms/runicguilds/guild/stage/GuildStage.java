package com.runicrealms.runicguilds.guild.stage;

public enum GuildStage {
    STAGE_5("Stage 5", 5, 186000, 35, StageReward.EXP_BONUS),
    STAGE_4("Stage 4", 4, 94500, 30, StageReward.COMBAT_BONUS),
    STAGE_3("Stage 3", 3, 39000, 25, StageReward.MOUNT_SPEED_BONUS),
    STAGE_2("Stage 2", 2, 10500, 20, StageReward.BANNER),
    STAGE_1("Stage 1", 1, 1092, 15, StageReward.NONE),
    STAGE_0("Stage 0", 0, 0, 10, StageReward.NONE);

    private final String name;
    private final int rank;
    private final int exp;
    private final int maxMembers;
    private final StageReward stageReward;

    /**
     * A guild stage is similar to a player's combat level. Increases as the player earns exp
     *
     * @param name        of the stage "Stage 5"
     * @param rank        (it's numerical rank)
     * @param exp         required to achieve this rank
     * @param maxMembers  allowed in a guild of this rank
     * @param stageReward an enum representing a type of reward at this stage
     */
    GuildStage(String name, int rank, int exp, int maxMembers, StageReward stageReward) {
        this.name = name;
        this.rank = rank;
        this.exp = exp;
        this.maxMembers = maxMembers;
        this.stageReward = stageReward;
    }

    public static GuildStage getMaxStage() {
        return STAGE_5;
    }

    public static GuildStage getFromReward(StageReward stageReward) {
        for (GuildStage guildStage : GuildStage.values()) {
            if (guildStage.getStageReward() == stageReward)
                return guildStage;
        }
        return null;
    }

    public static GuildStage getFromExp(int exp) {
        GuildStage highestStage = STAGE_0;

        for (GuildStage guildStage : GuildStage.values()) {
            if (exp >= guildStage.getExp()) {
                highestStage = guildStage;
                return highestStage;
            }
        }

        return highestStage;
    }

    public int getExp() {
        return this.exp;
    }

    public int getMaxMembers() {
        return this.maxMembers;
    }

    public String getName() {
        return name;
    }

    public int getRank() {
        return rank;
    }

    public StageReward getStageReward() {
        return this.stageReward;
    }
}
