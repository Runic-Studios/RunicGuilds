package com.runicrealms.runicguilds.timechallenge;

import com.runicrealms.plugin.party.Party;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.data.GuildData;
import com.runicrealms.runicguilds.data.GuildUtil;
import com.runicrealms.runicnpcs.Npc;
import org.bukkit.Bukkit;
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
            Bukkit.broadcastMessage("leader no guild"); //remove
            return;
        }

        String prefix = leaderGuildData.getData().getGuildPrefix();

        for (Player player : this.party.getMembersWithLeader()) {
            if (!player.getWorld().getName().equals(this.party.getLeader().getWorld().getName())) {
                Bukkit.broadcastMessage("not same world"); //remove
                return;
            }

            if (player.getLocation().distanceSquared(this.npc.getLocation()) > 500) {
                Bukkit.broadcastMessage("distance squared"); //remove
                return;
            }

            GuildData data = GuildUtil.getGuildData(player.getUniqueId());
            if (data == null) {
                Bukkit.broadcastMessage("member not in guild"); //remove
                return;
            }

            if (!data.getData().getGuildPrefix().equals(prefix)) {
                Bukkit.broadcastMessage("member not in same guild"); //remove
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
