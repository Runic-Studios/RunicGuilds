package com.runicrealms.runicguilds.guild;

import com.runicrealms.runicguilds.model.MemberData;

import java.util.Comparator;

public class RankCompare implements Comparator<MemberData> {

    @Override
    public int compare(MemberData member1, MemberData member2) {
        return member1.getRank().getRankNumber().compareTo(member2.getRank().getRankNumber());
    }
}
