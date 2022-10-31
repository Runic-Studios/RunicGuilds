package com.runicrealms.runicguilds.api.chat;

import com.runicrealms.api.chat.ChatChannel;
import com.runicrealms.runicguilds.api.RunicGuildsAPI;
import com.runicrealms.runicguilds.util.GuildUtil;
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

    @Override
    public String getPrefix() {
        return "&6[Guild] [%guild_score%] &r";
    }

    @Override
    public String getName() {
        return "guild";
    }

    @Override
    public List<Player> getRecipients(Player player) {
        List<Player> recipients = new ArrayList<>();
        if (GuildUtil.getPlayerCache().get(player.getUniqueId()) != null) {
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target != null) {
                    if (GuildUtil.getPlayerCache().get(target.getUniqueId()) != null) {
                        if (GuildUtil.getPlayerCache().get(player.getUniqueId()).equalsIgnoreCase(GuildUtil.getPlayerCache().get(target.getUniqueId()))) {
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
                                ChatColor.DARK_AQUA + "Title: " + ChatColor.AQUA + "%core_prefix%"
                        ))
                )
        );
        return textComponent;
    }

    private String displayScore(Player player) {
        if (RunicGuildsAPI.getGuild(player.getUniqueId()) != null
                && RunicGuildsAPI.getGuild(player.getUniqueId()).getMember(player.getUniqueId()) != null) {
            return String.valueOf(RunicGuildsAPI.getGuild(player.getUniqueId()).getMember(player.getUniqueId()).getScore());
        } else {
            return "0";
        }
    }
}
