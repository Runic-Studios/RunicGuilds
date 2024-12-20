package com.runicrealms.plugin.runicguilds.api.chat;

import com.runicrealms.plugin.chat.api.chat.ChatChannel;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.runicguilds.RunicGuilds;
import com.runicrealms.plugin.runicguilds.model.GuildInfo;
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
    public TextComponent createMessage(Player player, String message) {
        TextComponent textComponent = new TextComponent(ColorUtil.format(PlaceholderAPI.setPlaceholders(player, "&6[Guild] %luckperms_meta_name_color%%player_name%: &f")) + message);
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

    @Override
    public TextComponent createSpyMessage(Player player, Player spy, String message) {
        GuildInfo guild = RunicGuilds.getDataAPI().getGuildInfo(player);
        String guildName = "Unknown";
        if (guild != null) guildName = guild.getPrefix();
        TextComponent textComponent = new TextComponent(ColorUtil.format(PlaceholderAPI.setPlaceholders(player, "&6[" + guildName + " Guild] %luckperms_meta_name_color%%player_name%: &f")) + message);
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

    private TextComponent createMessage(Player player, String message, boolean spy) {
        String guildName = "";
        if (spy) {
            GuildInfo guild = RunicGuilds.getDataAPI().getGuildInfo(player);
            guildName = "Unknown";
            if (guild != null) guildName = guild.getPrefix();
        }
        TextComponent textComponent = new TextComponent(ColorUtil.format(PlaceholderAPI.setPlaceholders(player, "&6[" + (spy ? guildName + " " : "") + "Guild] %luckperms_meta_name_color%%player_name%: &f")) + message);
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

    @Override
    public boolean isSpyable() {
        return true;
    }

    @Override
    public boolean canSpy(Player sender, Player spy) {
        return RunicGuilds.getDataAPI().getGuildInfo(sender) != null;
    }

}
