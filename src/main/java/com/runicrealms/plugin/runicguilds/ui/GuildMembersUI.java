package com.runicrealms.plugin.runicguilds.ui;

import co.aikar.taskchain.TaskChain;
import com.runicrealms.plugin.common.util.GUIUtil;
import com.runicrealms.plugin.runicguilds.RunicGuilds;
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
    private int page;

    /**
     * Builds an info UI that can be used to see info about a guild
     *
     * @param player who entered the command
     */
    public GuildMembersUI(Player player) {
        this.player = player;
        String name = RunicGuilds.getDataAPI().getGuildInfo(player).getName();
        this.inventory = Bukkit.createInventory(this, 54, ChatColor.GOLD + name);
        this.page = 1;
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

    public int getPage() {
        return this.page;
    }

    public void setPage(int page) {
        if (page < 1 || page > this.getMaxPage()) {
            return;
        }

        this.page = page;
        this.openMenu();
    }

    private int getMaxPage() {
        GuildInfo guild = RunicGuilds.getDataAPI().getGuildInfo(this.player);

        if (guild == null) {
            throw new IllegalStateException("Guild member page opened for " + this.player.getName() + " but they do not have a guild!");
        }

        //28 per page
        return Math.max(1, (int) Math.ceil((double) guild.getMembersUuids().size() / 28)); //round up to nearest integer;
    }

    /**
     * Opens the inventory associated w/ this GUI, ordering perks
     */
    private void openMenu() {
        this.inventory.clear();
        GUIUtil.fillInventoryBorders(this.inventory);
        if (this.page > 1) {
            this.inventory.setItem(0, GUIUtil.BACK_BUTTON);
        }
        if (this.page < this.getMaxPage()) {
            this.inventory.setItem(8, GUIUtil.FORWARD_BUTTON);
        }
        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(player);
        UUID guildUUID = guildInfo.getUUID();
        // Load members async, populate inventory async, then open inv sync
        TaskChain<?> chain = RunicGuilds.newChain();
        chain
                .asyncFirst(() -> {
                    Map<UUID, MemberData> memberDataMap = RunicGuilds.getDataAPI().loadMemberDataMap(guildUUID);
                    List<MemberData> memberDataList = new ArrayList<>(memberDataMap.values());
                    memberDataList.sort((member1, member2) -> {
                        // Compare by GuildRank first (lower integer value means higher rank)
                        int rankComparison = Integer.compare(member1.getRank().getRankNumber(), member2.getRank().getRankNumber());

                        if (rankComparison != 0) {
                            return rankComparison;
                        }

                        // If GuildRanks are equal, compare by score
                        return Integer.compare(member2.getScore(), member1.getScore());
                    });
                    return memberDataList;
                })
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicGuilds failed to load member data!")
                .syncLast(memberDataList -> {
                    for (int i = 0; i < 28; i++) {
                        int index = ((this.page - 1) * 28) + i;

                        if (index >= memberDataList.size()) {
                            break;
                        }

                        MemberData guildMember = memberDataList.get(index);

                        // Use player's last known name to async fill inventory
                        this.inventory.setItem(this.inventory.firstEmpty(), GuildUtil.guildMemberItem
                                (
                                        guildMember.getUuid(),
                                        ChatColor.GOLD + guildMember.getLastKnownName(), // Last known name
                                        ChatColor.YELLOW + "Rank: " + guildMember.getRank() +
                                                "\n" + ChatColor.YELLOW + "Score: [" + guildMember.getScore() + "]",
                                        guildMember.getLastKnownSkin()
                                ));
                    }

                    // Open the UI!
                    this.player.openInventory(this.inventory);
                })
                .execute();
    }
}