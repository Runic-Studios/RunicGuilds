package com.runicrealms.runicguilds.api.chat;

import com.runicrealms.api.chat.ChatChannel;
import com.runicrealms.runicguilds.RunicGuilds;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GuildChannel extends ChatChannel {

    /**
     * Displays the guild score of the given player, or 0
     *
     * @param player to check
     * @return their guild score
     */
    private String displayScore(Player player) {
        if (RunicGuilds.getRunicGuildsAPI().getGuild(player.getUniqueId()) != null
                && RunicGuilds.getRunicGuildsAPI().getGuild(player.getUniqueId()).getMember(player.getUniqueId()) != null) {
            return String.valueOf(RunicGuilds.getRunicGuildsAPI().getGuild(player.getUniqueId()).getMember(player.getUniqueId()).getScore());
        } else {
            return "0";
        }
    }

    @Override
    public String getPrefix() {
        return "&6[Guild] [%guild_rank%] &r";
    }

    @Override
    public String getName() {
        return "guild";
    }

    @Override
    public List<Player> getRecipients(Player player) {
        List<Player> recipients = new ArrayList<>();
        if (RunicGuilds.getRunicGuildsAPI().isInGuild(player.getUniqueId())) {
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target != null) {
                    if (RunicGuilds.getRunicGuildsAPI().isInGuild(target.getUniqueId())) {
                        if (RunicGuilds.getRunicGuildsAPI().getGuild(player.getUniqueId())
                                .equals(RunicGuilds.getRunicGuildsAPI().getGuild(target.getUniqueId()))) {
                            recipients.add(target);
                        }
                    }
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "You must be in a guild to use guild chat!");
        }
        return recipients;
    }

    @Override
    public String getMessageFormat() {
        return "%luckperms_meta_name_color%%player_name%: &e%message%";
    }

    @Override
    public TextComponent getTextComponent(Player player, String finalMessage) {
        TextComponent textComponent = new TextComponent(finalMessage);
        textComponent.setHoverEvent(new HoverEvent
                (
                        HoverEvent.Action.SHOW_TEXT,
                        new Text(PlaceholderAPI.setPlaceholders(player,
                                ChatColor.DARK_AQUA + "Title: " + ChatColor.AQUA + "%core_prefix%" +
                                        ChatColor.GOLD + "\nGuild Score: %guild_score%"
                        ))
                )
        );
        return textComponent;
    }
}
