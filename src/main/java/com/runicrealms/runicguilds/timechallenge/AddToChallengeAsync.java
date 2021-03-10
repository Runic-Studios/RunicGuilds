package com.runicrealms.runicguilds.timechallenge;

import com.runicrealms.plugin.party.Party;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.data.GuildData;
import com.runicrealms.runicguilds.data.GuildUtil;
import com.runicrealms.runicnpcs.Npc;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AddToChallengeAsync extends BukkitRunnable {
    private final Party party;
    private final Npc npc;
    private boolean completed;

    public AddToChallengeAsync(Party party, Npc npc) {
        this.party = party;
        this.npc = npc;
        this.completed = false;
    }

    @Override
    public void run() {
        GuildData leaderGuildData = GuildUtil.getGuildData(this.party.getLeader().getUniqueId());

        if (leaderGuildData == null) {
            return;
        }

        if (this.party.getSize() > 10) {
            return;
        }

        String prefix = leaderGuildData.getData().getGuildPrefix();

        for (Player player : this.party.getMembersWithLeader()) {
            if (!player.getWorld().getName().equals(this.party.getLeader().getWorld().getName())) {
                return;
            }

            if (player.getLocation().distanceSquared(this.npc.getLocation()) > 500) {
                return;
            }

            GuildData data = GuildUtil.getGuildData(player.getUniqueId());
            if (data == null) {
                return;
            }

            if (!data.getData().getGuildPrefix().equals(prefix)) {
                return;
            }
        }

        Plugin.getTimeChallengeManager().startChallenge(party);
        this.completed = true;
    }

    public boolean isCompleted() {
        return this.completed;
    }
}
