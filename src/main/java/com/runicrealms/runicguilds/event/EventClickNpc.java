package com.runicrealms.runicguilds.event;

import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.data.GuildUtil;
import com.runicrealms.runicguilds.gui.GuildBankUtil;
import net.citizensnpcs.api.event.NPCRightClickEvent;
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
    public void onRightClick(NPCRightClickEvent event) {
        if (!cooldowns.containsKey(event.getClicker())) {
            runClickEvent(event);
            cooldowns.put(event.getClicker().getUniqueId(), System.currentTimeMillis());
        } else if (cooldowns.get(event.getClicker().getUniqueId()) + 1000 <= System.currentTimeMillis()){
            runClickEvent(event);
            cooldowns.put(event.getClicker().getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (cooldowns.containsKey(event.getPlayer().getUniqueId())) {
            cooldowns.remove(event.getPlayer().getUniqueId());
        }
    }

    private static void runClickEvent(NPCRightClickEvent event) {
        for (Integer heraldId : Plugin.GUILD_HERALDS) {
            if (heraldId == event.getNPC().getId()) {
                if (GuildUtil.getPlayerCache().get(event.getClicker().getUniqueId()) == null) {
                    if (!Plugin.getPlayersCreatingGuild().contains(event.getClicker().getUniqueId())) {
                        event.getClicker().sendMessage(ChatColor.YELLOW + "Creating a guild will cost you " + Plugin.GUILD_COST + " gold. To confirm or cancel the purchasing of this guild, type " + ChatColor.GOLD + "/guild confirm" + ChatColor.YELLOW + " or " + ChatColor.GOLD + "/guild cancel" + ChatColor.YELLOW + " in chat.");
                        Plugin.getPlayersCreatingGuild().add(event.getClicker().getUniqueId());
                    } else {
                        event.getClicker().sendMessage(ChatColor.YELLOW + "Type " + ChatColor.GOLD + "/guild confirm" + ChatColor.YELLOW + " or " + ChatColor.GOLD + "/guild cancel" + ChatColor.YELLOW + " in chat to confirm/cancel the creation of your guild.");
                    }
                } else {
                    event.getClicker().sendMessage(ChatColor.YELLOW + "You cannot create a guild because you are already in one!");
                }
                return;
            }
        }
        for (Integer bankerId : Plugin.GUILD_BANKERS) {
            if (bankerId == event.getNPC().getId()) {
                if (GuildUtil.getPlayerCache().get(event.getClicker().getUniqueId()) != null) {
                    GuildBankUtil.open(event.getClicker(), 1);
                } else {
                    event.getClicker().sendMessage(ChatColor.YELLOW + "You have to be in a guild to use the guild bank.");
                }
                return;
            }
        }
    }

}