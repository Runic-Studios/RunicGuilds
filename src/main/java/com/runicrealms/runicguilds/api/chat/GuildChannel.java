package com.runicrealms.runicguilds.api.chat;

import com.runicrealms.api.chat.ChatChannel;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.model.GuildInfo;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuildChannel extends ChatChannel {

    @Override
    public String getPrefix() {
        return "&6[Guild] %luckperms_meta_name_color%%player_name%: "; // [%guild_rank%]
    }

    @Override
    public String getName() {
        return "guild";
    }

    @Override
    public List<Player> getRecipients(Player player) {
        List<Player> recipients = new ArrayList<>();
        GuildInfo guildInfoSender = RunicGuilds.getDataAPI().getGuildInfo(player);
        if (guildInfoSender == null) {
            player.sendMessage(ChatColor.RED + "You must be in a guild to use guild chat!");
            return recipients;
        }
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target == null) continue;
            if (!RunicGuilds.getGuildsAPI().isInGuild(target)) continue;
            UUID guildUUID = RunicGuilds.getDataAPI().getGuildInfo(target).getUUID();
            if (guildInfoSender.getUUID() == guildUUID) {
                recipients.add(target);
            }
        }
        return recipients;
    }

    @Override
    public String getMessageFormat() {
        return "&e%message%";
    }

    @Override
    public TextComponent getTextComponent(Player player, String finalMessage) {
        TextComponent textComponent = new TextComponent(finalMessage);
        String title = PlaceholderAPI.setPlaceholders(player, "%core_prefix%");
        if (title.isEmpty()) title = "None";
        String titleColor = ColorUtil.format(PlaceholderAPI.setPlaceholders(player, "%core_name_color%"));
        textComponent.setHoverEvent(new HoverEvent
                (
                        HoverEvent.Action.SHOW_TEXT,
                        new Text(
                                ChatColor.DARK_AQUA + "Title: " + titleColor + title
                        ) // ChatColor.GOLD + "\nGuild Score: %guild_score%"
                )
        );
        return textComponent;
    }
}
