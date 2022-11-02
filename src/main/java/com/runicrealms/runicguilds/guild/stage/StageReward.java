package com.runicrealms.runicguilds.guild.stage;

public enum StageReward {

    NONE
            (
                    0,
                    "",
                    ""
            ),
    BANNER
            (
                    0,
                    "Your guild can now create a banner!",
                    "Guild Banner"
            ),
    MOUNT_SPEED_BONUS
            (
                    0.1,
                    "All guild members receive {MODIFIER}% bonus speed while on a mount!",
                    "{MODIFIER}% bonus mount speed"
            ),
    COMBAT_BONUS
            (
                    .05,
                    "All guild members receive a {MODIFIER}% exp buff!",
                    "{MODIFIER}% combat exp buff"
            ),
    EXP_BONUS
            (
                    0.05,
                    "All guild members receive a {MODIFIER}% damage buff against monsters!",
                    "{MODIFIER}% damage buff vs. monsters"
            );

    private final double buffPercent;
    private final String message;
    private final String formattedReward;

    /**
     * @param buffPercent
     * @param message
     * @param formattedReward
     */
    StageReward(double buffPercent, String message, String formattedReward) {
        this.buffPercent = buffPercent;
        this.message = message;
        this.formattedReward = formattedReward;
    }

    public double getBuffPercent() {
        return buffPercent;
    }

    public String getMessage() {
        return message.replace("{MODIFIER}", (this.buffPercent * 100) + "");
    }

    public String getFormattedReward() {
        return formattedReward.replace("{MODIFIER}", (this.buffPercent * 100) + "");
    }
}
