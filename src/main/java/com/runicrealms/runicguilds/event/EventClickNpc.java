package com.runicrealms.runicguilds.event;

import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.data.GuildUtil;
import com.runicrealms.runicguilds.gui.GuildBankUtil;
import com.runicrealms.runicnpcs.api.NpcClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventClickNpc implements Listener {

    public static Map<UUID, Long> cooldowns = new HashMap<UUID, Long>();

    @EventHandler
    public void onRightClick(NpcClickEvent event) {
        if (!cooldowns.containsKey(event.getPlayer())) {
            runClickEvent(event);
            cooldowns.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        } else if (cooldowns.get(event.getPlayer().getUniqueId()) + 1000 <= System.currentTimeMillis()){
            runClickEvent(event);
            cooldowns.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (cooldowns.containsKey(event.getPlayer().getUniqueId())) {
            cooldowns.remove(event.getPlayer().getUniqueId());
        }
    }

    private static void runClickEvent(NpcClickEvent event) {
        for (Integer bankerId : Plugin.GUILD_BANKERS) {
            if (bankerId == event.getNpc().getId()) {
                if (GuildUtil.getPlayerCache().get(event.getPlayer().getUniqueId()) != null) {
                    GuildBankUtil.open(event.getPlayer(), 1);
                } else {
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "You have to be in a guild to use the guild bank.");
                }
                return;
            }
        }
    }

}