package com.runicrealms.runicguilds.listener;

import com.runicrealms.plugin.api.NpcClickEvent;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.util.GuildBankUtil;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NpcClickListener implements Listener {

    public static Map<UUID, Long> cooldowns = new HashMap<>();

    private static void runClickEvent(NpcClickEvent event) {
        for (Integer bankerId : RunicGuilds.GUILD_BANKERS) {
            if (bankerId == event.getNpc().getId()) {
                if (RunicGuilds.getGuildsAPI().isInGuild(event.getPlayer().getUniqueId())) {
                    GuildData guildData = RunicGuilds.getGuildsAPI().getGuildData(event.getPlayer().getUniqueId());
                    if (guildData.getGuild().getOwner().getUUID() != event.getPlayer().getUniqueId()) {
                        if (!guildData.getGuild().canAccessBank(guildData.getGuild().getMember(event.getPlayer().getUniqueId()).getRank())) {
                            event.getPlayer().sendMessage(ChatColor.YELLOW + "Your guild rank does not have access to the guild bank!");
                            return;
                        }
                    }
                    event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                    GuildBankUtil.open(event.getPlayer(), 1);
                } else {
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "You have to be in a guild to use the guild bank.");
                }
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onRightClick(NpcClickEvent event) {
        if (!cooldowns.containsKey(event.getPlayer().getUniqueId())) {
            runClickEvent(event);
            cooldowns.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        } else if (cooldowns.get(event.getPlayer().getUniqueId()) + 1000 <= System.currentTimeMillis()) {
            runClickEvent(event);
            cooldowns.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }

}