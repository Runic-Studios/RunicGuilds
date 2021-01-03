package com.runicrealms.runicguilds.timechallenge;

import com.runicrealms.plugin.party.Party;
import com.runicrealms.runicguilds.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TimeChallengeManager {
    private final Set<Integer> npcIDS;
    private final Map<Party, Long> partiesInChallenge = new HashMap<>();

    //time to complete in milliseconds
    public static final long SEBATHS_CAVE = 600000L;
    public static final long ODINS_KEEP = 1200000L;
    public static final long SUNKEN_LIBRARY = 1200000L;
    public static final long CRYPTS_OF_DERA = 1500000L;
    public static final long THE_FROZEN_FORTRESS = 2700000L;

    //guild score for completion
    public static final int SEBATHS_CAVE_REWARD = 10;
    public static final int ODINS_KEEP_REWARD = 15;
    public static final int SUNKEN_LIBRARY_REWARD = 20;
    public static final int CRYPTS_OF_DERA_REWARD = 25;
    public static final int THE_FROZEN_FORTRESS_REWARD = 50;

    public TimeChallengeManager() {
        this.npcIDS = new HashSet<>(Plugin.getInstance().getConfig().getIntegerList("time-challengers"));
    }

    public void startChallenge(Party party) {
        this.partiesInChallenge.put(party, System.currentTimeMillis());
        party.sendMessageInChannel("&r&aYour party has just started the time challenge.");
    }

    public void removeFromChallenge(Party party) {
        this.partiesInChallenge.remove(party);
        party.sendMessageInChannel("&r&aYour party is no longer taking part of the time challenge.");
    }

    public long finishChallenge(Party party) {
        long startTime = this.partiesInChallenge.get(party);
        this.partiesInChallenge.remove(party);
        return startTime;
    }

    public boolean contains(Party party) {
        return this.partiesInChallenge.containsKey(party);
    }

    public Set<Integer> getNpcIDS() {
        return this.npcIDS;
    }
}
