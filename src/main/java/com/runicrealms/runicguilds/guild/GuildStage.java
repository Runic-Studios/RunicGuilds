package com.runicrealms.runicguilds.guild;

import com.runicrealms.runicguilds.util.GuildRewardUtil;

import java.util.Arrays;
import java.util.List;

public enum GuildStage {
    STAGE5(186000, 35, "Your guild size has advanced to 30",
            "All guild members receive a " + (GuildRewardUtil.getGuildExpBuff() * 100) + "% exp buff!"),
    STAGE4(94500, 30, "Your guild size has advanced to 25",
            "All guild members receive a " + (GuildRewardUtil.getGuildCombatBuff() * 100) + "% damage buff while fighting monsters!"),
    STAGE3(39000, 25, "Your guild size has advanced to 20",
            "All guild members receive " + (GuildRewardUtil.getGuildMountSpeedBonus() * 100) + "% bonus speed while on a mount!"),
    STAGE2(10500, 20, "Your guild size has advanced to 15",
            "You can now create a guild banner!"),
    STAGE1(1092, 15, "Your guild size has advanced to 10!"),
    STAGE0(0, 10, "");

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

    public static GuildStage getMaxStage() {
        return STAGE5;
    }
}
