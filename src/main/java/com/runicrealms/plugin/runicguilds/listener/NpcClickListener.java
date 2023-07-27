package com.runicrealms.plugin.runicguilds.listener;

import com.runicrealms.plugin.npcs.api.NpcClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NpcClickListener implements Listener {

    private static final long BANKER_COOLDOWN = 2000; // 2 secs
    public static Map<UUID, Long> cooldowns = new HashMap<>();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onRightClick(NpcClickEvent event) {
        if(event.isCancelled()) return;
        if (!cooldowns.containsKey(event.getPlayer().getUniqueId())) {
            runClickEvent(event);
            cooldowns.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        } else if (cooldowns.get(event.getPlayer().getUniqueId()) + BANKER_COOLDOWN <= System.currentTimeMillis()) {
            runClickEvent(event);
            cooldowns.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }

    /**
     * Checks whether the player is speaking to a guild banker.
     * And if they are, checks whether they are in a guild.
     * Finally, if their rank permits it, allows the player
     */
    private void runClickEvent(NpcClickEvent event) {
        if(event.isCancelled()) return;

//        UUID uuid = event.getPlayer().getUniqueId();
//        for (Integer bankerId : RunicGuilds.GUILD_BANKERS) {
//            // Ensure we are talking to a guild banker
//            if (bankerId != event.getNpc().getId()) continue;
//            GuildInfo guildInfo = RunicGuilds.getDataAPI().getGuildInfo(event.getPlayer());
//            if (guildInfo == null) {
//                event.getPlayer().sendMessage(ChatColor.YELLOW + "You have to be in a guild to use the guild bank.");
//                return;
//            }
//            // Initiate bank open logic
//            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
//            // Call redis async
//            TaskChain<?> chain = RunicGuilds.newChain();
//            chain
//                    .asyncFirst(() -> RunicGuilds.getDataAPI().loadMemberData(guildInfo.getUUID(), uuid))
//                    .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicGuilds failed to load member data!")
//                    .syncLast(memberData -> {
//                        // If this is NOT the owner
//                        if (guildInfo.getOwnerUuid() != event.getPlayer().getUniqueId()) {
//                            // Perform a rank check
//                            SettingsData settingsData = guildInfo.getSettingsData();
//                            if (!settingsData.canAccessBank(memberData.getRank())) {
//                                event.getPlayer().sendMessage(ChatColor.YELLOW + "Your guild rank does not have access to the guild bank!");
//                                return;
//                            }
//                        }
//                        // Open the bank!
//                        GuildBankUtil.open(event.getPlayer(), 1);
//                    })
//                    .execute();
//        }
    }

}