package com.runicrealms.runicguilds.util;

/**
 * Contains variables for the guild stage rewards to be used throughout the plugin
 */
public class GuildRewardUtil {

    private static final double GUILD_EXP_BUFF = .05;
    private static final double GUILD_COMBAT_BUFF = .1;
    private static final double GUILD_MOUNT_SPEED_BONUS = .15;
    private static final double GUILD_REP_BONUS = .1;

    public static double getGuildExpBuff() {
        return GUILD_EXP_BUFF;
    }

    public static double getGuildCombatBuff() {
        return GUILD_COMBAT_BUFF;
    }

    public static double getGuildMountSpeedBonus() {
        return GUILD_MOUNT_SPEED_BONUS;
    }

    public static double getGuildRepBonus() {
        return GUILD_REP_BONUS;
    }

}
