package com.runicrealms.runicguilds.guild.stage;

public enum StageReward {

    NONE
            (
                    0,
                    ""
            ),
    BANNER
            (
                    0,
                    "Your guild can now create a banner!"
            ),
    MOUNT_SPEED_BONUS
            (
                    0.1,
                    "All guild members receive {MODIFIER}% bonus speed while on a mount!"
            ),
    COMBAT_BONUS
            (
                    .05,
                    "All guild members receive a {MODIFIER}% exp buff!"
            ),
    EXP_BONUS
            (
                    0.05,
                    "All guild members receive a {MODIFIER}% damage buff against monsters!"
            );

    private final double buffPercent;
    private final String message;

    StageReward(double buffPercent, String message) {
        this.buffPercent = buffPercent;
        this.message = message;
    }

    public double getBuffPercent() {
        return buffPercent;
    }

    public String getMessage() {
        return message.replace("{MODIFIER}", (this.buffPercent * 100) + "");
    }
}
