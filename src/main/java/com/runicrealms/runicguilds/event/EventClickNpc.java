package com.runicrealms.runicguilds.event;

import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.config.GuildUtil;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EventClickNpc implements Listener {

    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {
        for (Integer heraldId : Plugin.GUILD_HERALDS) {
            if (heraldId == event.getNPC().getId()) {
                if (GuildUtil.getPlayerCache().get(event.getClicker().getUniqueId()) == null) {
                    if (!Plugin.getPlayersCreatingGuild().contains(event.getClicker().getUniqueId())) {
                        event.getClicker().sendMessage(ChatColor.YELLOW + "Creating a guild will cost you " + Plugin.GUILD_COST + " gold. To confirm or cancel the purchasing of this guild, type " + ChatColor.GOLD + "/guild confirm" + ChatColor.YELLOW + " or " + ChatColor.GOLD + "/guild cancel" + ChatColor.YELLOW + " in chat.");
                        Plugin.getPlayersCreatingGuild().add(event.getClicker().getUniqueId());
                    } else {
                        event.getClicker().sendMessage(ChatColor.YELLOW + "Type " +  ChatColor.GOLD + "/guild confirm" + ChatColor.YELLOW + " or " + ChatColor.GOLD + "/guild cancel" + ChatColor.YELLOW + " in chat to confirm/cancel the creation of your guild.");
                    }
                } else {
                    event.getClicker().sendMessage(ChatColor.YELLOW + "You cannot create a guild because you are already in one!");
                }
                return;
            }
        }
    }

}