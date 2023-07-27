package com.runicrealms.plugin.runicguilds.ui;

import co.aikar.taskchain.TaskChain;
import com.runicrealms.plugin.common.util.GUIUtil;
import com.runicrealms.plugin.runicguilds.RunicGuilds;
import com.runicrealms.plugin.runicguilds.guild.RankCompare;
import com.runicrealms.plugin.runicguilds.model.GuildInfo;
import com.runicrealms.plugin.runicguilds.model.MemberData;
import com.runicrealms.plugin.runicguilds.util.GuildUtil;
import com.runicrealms.plugin.runicguilds.util.TaskChainUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GuildMembersUI implements InventoryHolder {
    private final Inventory inventory;
    private final Player player;

    /**
     * Builds an info UI that can be used to see info about a guild
     *
     * @param player who entered the command
     */
    public GuildMembersUI(Player player) {
        this.player = player;
        String name = RunicGuilds.getDataAPI().getGuildInfo(player).getName();
        this.inventory = Bukkit.createInventory(this, 54, ChatColor.GOLD + name);
        openMenu();
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public Player getPlayer() {
        return this.player;
    }

    /**
     * Opens the inventory associated w/ this GUI, ordering perks
     */
    private void openMenu() {
        this.inventory.clear();
        GUIUtil.fillInventoryBorders(this.inventory);
        this.inventory.setItem(0, GUIUtil.BACK_BUTTON);
        this.inventory.setItem(8, GUIUtil.CLOSE_BUTTON);
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        UUID guildUUID = guildInfo.getUUID();
        // Load members async, populate inventory async, then open inv sync
        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> {
                    Map<UUID, MemberData> memberDataMap = RunicGuilds.getDataAPI().loadMemberDataMap(guildUUID);
                    List<MemberData> memberDataList = new ArrayList<>(memberDataMap.values());
                    memberDataList.sort(new RankCompare());
                    return memberDataList;
                })
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicGuilds failed to load member data!")
                .syncLast(memberDataList -> {
                    // Open the UI!
                    for (MemberData guildMember : memberDataList) {
                        // Use player's last known name to async fill inventory
                        this.inventory.setItem(this.inventory.firstEmpty(), GuildUtil.guildMemberItem
                                (
                                        guildMember.getUuid(),
                                        ChatColor.GOLD + guildMember.getLastKnownName(), // Last known name
                                        ChatColor.YELLOW + "Rank: " + guildMember.getRank() +
                                                "\n" + ChatColor.YELLOW + "Score: [" + guildMember.getScore() + "]"
                                ));
                    }
                    player.openInventory(this.inventory);
                })
                .execute();
    }
}