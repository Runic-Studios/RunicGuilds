package com.runicrealms.runicguilds.listeners;

import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.api.event.ClickGuildBannerEvent;
import com.runicrealms.runicguilds.guild.Guild;
import com.runicrealms.runicguilds.guild.GuildMember;
import com.runicrealms.runicguilds.guild.GuildRank;
import com.runicrealms.runicguilds.guild.PostedGuildBanner;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;

public class BannerClickListener implements Listener {

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (!(event.getRightClicked() instanceof ArmorStand)) {
            return;
        }

        if (!event.getRightClicked().getPersistentDataContainer().has(PostedGuildBanner.KEY, PersistentDataType.STRING)) {
            return;
        }

        String result = event.getRightClicked().getPersistentDataContainer().get(PostedGuildBanner.KEY, PersistentDataType.STRING);

        Guild guild = RunicGuilds.getRunicGuildsAPI().getGuildData(result).getGuild();

        if (guild == null) {
            return;
        }

        ClickGuildBannerEvent bannerEvent = new ClickGuildBannerEvent(guild);
        Bukkit.getPluginManager().callEvent(bannerEvent);

        if (bannerEvent.isCancelled()) return;

        Player player = event.getPlayer();

        player.sendMessage(ColorUtil.format("&6[" + guild.getScore() + "]&r &e&l" + guild.getGuildName()));
        player.sendMessage(ColorUtil.format("&6Guild Experience: " + guild.getGuildExp()));
        player.sendMessage(ColorUtil.format("&6Guild Owner: &7[" + guild.getOwner().getScore() + "] &e" + guild.getOwner().getLastKnownName()));
        HashMap<GuildRank, StringBuilder> members = new HashMap<GuildRank, StringBuilder>();
        for (GuildMember member : guild.getMembers()) {
            if (!members.containsKey(member.getRank())) {
                members.put(member.getRank(), new StringBuilder());
            }
            members.get(member.getRank())
                    .append("&7[")
                    .append(member.getScore())
                    .append("] &e")
                    .append(member.getLastKnownName())
                    .append("&r, ");
        }
        for (GuildRank rank : GuildRank.values()) {
            if (members.containsKey(rank)) {
                player.sendMessage(ColorUtil.format("&6Guild " + rank.getPlural() + "s: &r" + members.get(rank).toString().substring(0, members.get(rank).toString().length() - 2)));
            }
        }
    }
}
