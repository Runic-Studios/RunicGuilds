package com.runicrealms.runicguilds.guild;

import java.util.Comparator;

public class RankCompare implements Comparator<GuildMember> {

    @Override
    public int compare(GuildMember member1, GuildMember member2) {
        return member1.getRank().getRankNumber().compareTo(member2.getRank().getRankNumber());
    }
}
