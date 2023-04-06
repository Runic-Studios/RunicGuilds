package com.runicrealms.runicguilds.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class BannerClickListener implements Listener {

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractAtEntityEvent event) {
//        if (event.getHand() != EquipmentSlot.HAND) return;
//        if (!(event.getRightClicked() instanceof ArmorStand)) return;
//        if (!event.getRightClicked().getPersistentDataContainer().has(PostedGuildBanner.KEY, PersistentDataType.STRING))
//            return;
//
//        String guildUUIDFromBanner = event.getRightClicked().getPersistentDataContainer().get(PostedGuildBanner.KEY, PersistentDataType.STRING);
//        if (guildUUIDFromBanner == null) return;
//        GuildUUID guildUUID = new GuildUUID(UUID.fromString(guildUUIDFromBanner));
//        GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(guildUUIDFromBanner);
//        if (guildInfo == null) return;
//
//
//        ClickGuildBannerEvent bannerEvent = new ClickGuildBannerEvent(guildInfo.getGuildUUID());
//        Bukkit.getPluginManager().callEvent(bannerEvent);
//        if (bannerEvent.isCancelled()) return;
//
//        Player player = event.getPlayer();
//        player.sendMessage(ColorUtil.format("&6[" + guildInfo.getScore() + "]&r &e&l" + guildInfo.getName()));
//        player.sendMessage(ColorUtil.format("&6Guild Experience: " + guildInfo.getExp()));
//        player.sendMessage(ColorUtil.format("&6Guild Owner: &7[" + guild.getOwner().getScore() + "] &e" + guild.getOwner().getLastKnownName()));
//        HashMap<GuildRank, StringBuilder> members = new HashMap<>();
//         todo: do we want any of this?
//        for (GuildMember member : guild.getMembers()) {
//            if (!members.containsKey(member.getRank())) {
//                members.put(member.getRank(), new StringBuilder());
//            }
//            members.get(member.getRank())
//                    .append("&7[")
//                    .append(member.getScore())
//                    .append("] &e")
//                    .append(member.getLastKnownName())
//                    .append("&r, ");
//        }
//        for (GuildRank rank : GuildRank.values()) {
//            if (members.containsKey(rank)) {
//                player.sendMessage(ColorUtil.format("&6Guild " + rank.getPlural() + "s: &r" + members.get(rank).substring(0, members.get(rank).toString().length() - 2)));
//            }
//        }
    }
}
