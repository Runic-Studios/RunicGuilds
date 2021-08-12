package com.runicrealms.runicguilds.guilds;

import com.runicrealms.runicguilds.util.GuildRewardUtil;

import java.util.Arrays;
import java.util.List;

public enum GuildStage {
    STAGE7(513000, 50, "Your guild size has advanced to 50",
            "All guild members receive a " + (GuildRewardUtil.getGuildExpBuff() * 100) + "% exp buff!"),
    STAGE6(322500, 35, "Your guild size has advanced to 35",
            "All guild members receive a " + (GuildRewardUtil.getGuildCombatBuff() * 100) + "% damage buff while fighting monsters!"),
    STAGE5(186000, 30, "Your guild size has advanced to 30",
            "All guild members receive " + (GuildRewardUtil.getGuildMountSpeedBonus() * 100) + "% bonus speed while on a mount!"),
    STAGE4(94500, 25, "Your guild size has advanced to 25",
            "All guild members receive a " + (GuildRewardUtil.getGuildRepBonus() * 100) + "% reputation bonus!"),
    STAGE3(39000, 20, "Your guild size has advanced to 20",
            "You have unlocked all banner patterns!"),
    STAGE2(10500, 15, "Your guild size has advanced to 15",
            "You can now create a guild banner!"),
    STAGE1(1092, 10, "Your guild size has advanced to 10!"),
    STAGE0(0, 5, "");

    private final int exp;
    private final int maxMembers;
    private final List<String> perkInfo;

    GuildStage(int exp, int maxMembers, String... perkInfo) {
        this.exp = exp;
        this.maxMembers = maxMembers;
        this.perkInfo = Arrays.asList(perkInfo);
    }

    public int getExp() {
        return this.exp;
    }

    public int getMaxMembers() {
        return this.maxMembers;
    }

    public List<String> getPerkInfo() {
        return this.perkInfo;
    }
}
