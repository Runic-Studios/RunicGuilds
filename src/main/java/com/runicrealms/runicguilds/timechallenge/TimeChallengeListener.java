package com.runicrealms.runicguilds.timechallenge;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.party.Party;
import com.runicrealms.plugin.party.event.PartyEvent;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.data.GuildData;
import com.runicrealms.runicguilds.data.GuildUtil;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicnpcs.Npc;
import com.runicrealms.runicnpcs.api.NpcClickEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimeChallengeListener implements Listener {
    private final List<String> names = new ArrayList<>(Plugin.getInstance().getConfig().getStringList("dungeon-bosses"));

    @EventHandler
    public void onNpcClick(NpcClickEvent event) {
        Npc npc = event.getNpc();
        if (!Plugin.getTimeChallengeManager().getNpcIDS().contains(npc.getId())) {
            return;
        }

        Player player = event.getPlayer();
        Party party = RunicCore.getPartyManager().getPlayerParty(player);

        if (party == null) {
            player.sendMessage(ColorUtil.format("&r&cYou must be in a party with guild members to start the time challenge!"));
            return;
        }

        if (party.getSize() <= 1) {
            player.sendMessage(ColorUtil.format("&r&cYou must be in a party with guild members to start the time challenge!"));
            return;
        }

        if (!Plugin.getTimeChallengeManager().contains(party)) {
            this.addToChallenge(party, npc);
        } else {
            Plugin.getTimeChallengeManager().removeFromChallenge(party);
        }
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        MythicMob mythicMob = event.getMobType();
        if (!this.isBoss(mythicMob.getInternalName())) {
            return;
        }

        if (!(event.getKiller() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getKiller();
        Party party = RunicCore.getPartyManager().getPlayerParty(player);

        if (party == null) {
            return;
        }

        if (!Plugin.getTimeChallengeManager().contains(party)) {
            return;
        }

        long time = System.currentTimeMillis() - Plugin.getTimeChallengeManager().finishChallenge(party);
        long resultMin = this.getBossTime(mythicMob.getInternalName());

        boolean result = (time <= resultMin);

        int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(time);
        int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(minutes));

        party.sendMessageInChannel("&r&aYour party took " + minutes + " minutes and " + seconds + " seconds to complete the challenge!");

        if (result) {
            party.sendMessageInChannel("&r&aYour party has successfully completed the challenge!");
            this.giveReward(party, mythicMob.getInternalName());
        } else {
            party.sendMessageInChannel("&r&aYour party took too long to complete the challenge!");
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Party party = RunicCore.getPartyManager().getPlayerParty(event.getPlayer());

        if (party == null) {
            return;
        }

        if (!event.getFrom().getName().equals("dungeons")) {
            return;
        }

        if (!Plugin.getTimeChallengeManager().contains(party)) {
            return;
        }

        Plugin.getTimeChallengeManager().removeFromChallenge(party);
    }

    @EventHandler
    public void onPartyEvent(PartyEvent event) {
        Party party = event.getParty();

        if (!Plugin.getTimeChallengeManager().contains(party)) {
            return;
        }

        Plugin.getTimeChallengeManager().removeFromChallenge(party);
    }

    private void addToChallenge(Party party, Npc npc) {
        AddToChallengeAsync addToChallenge = new AddToChallengeAsync(party, npc);
        addToChallenge.runTaskAsynchronously(Plugin.getInstance());

        Bukkit.getScheduler().runTaskLater(Plugin.getInstance(), () -> {
            boolean result = !(addToChallenge.isCompleted());

            if (result) {
                party.sendMessageInChannel("&r&aYou cannot start a time challenge until all members are in the dungeon and all party members are in the same guild and your party has less then ten members!");
            }
        }, 1L);
    }

    private void giveReward(Party party, String name) {
        float preReward = this.getBossReward(name) / party.getSize().floatValue();

        int reward = (int) preReward;

        if (preReward % 1 != 0) {
            reward++;
        }

        GuildData guildData = GuildUtil.getGuildData(party.getLeader().getUniqueId());
        Guild guild = guildData.getData();

        for (Player player : party.getMembersWithLeader()) {
            guild.increasePlayerScore(player.getUniqueId(), reward);
        }
        guildData.queueToSave();
    }

    private boolean isBoss(String name) {
        for (String currentName : this.names) {
            if (name.equals(currentName)) {
                return true;
            }
        }
        return false;
    }

    private long getBossTime(String name) {
        if (name.equals(this.names.get(0))) {
            return TimeChallengeManager.SEBATHS_CAVE;
        } else if (name.equals(this.names.get(1))) {
            return TimeChallengeManager.ODINS_KEEP;
        } else if (name.equals(this.names.get(2))) {
            return TimeChallengeManager.SUNKEN_LIBRARY;
        } else if (name.equals(this.names.get(3))) {
            return TimeChallengeManager.CRYPTS_OF_DERA;
        } else if (name.equals(this.names.get(4))) {
            return TimeChallengeManager.THE_FROZEN_FORTRESS;
        }
        return -1L;
    }

    private int getBossReward(String name) {
        if (name.equals(this.names.get(0))) {
            return TimeChallengeManager.SEBATHS_CAVE_REWARD;
        } else if (name.equals(this.names.get(1))) {
            return TimeChallengeManager.ODINS_KEEP_REWARD;
        } else if (name.equals(this.names.get(2))) {
            return TimeChallengeManager.SUNKEN_LIBRARY_REWARD;
        } else if (name.equals(this.names.get(3))) {
            return TimeChallengeManager.CRYPTS_OF_DERA_REWARD;
        } else if (name.equals(this.names.get(4))) {
            return TimeChallengeManager.THE_FROZEN_FORTRESS_REWARD;
        }
        return 0;
    }
}
