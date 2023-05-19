package com.runicrealms.runicguilds.guild;

import com.runicrealms.plugin.model.SessionData;
import com.runicrealms.plugin.utilities.ChatUtils;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.api.event.GuildDisbandEvent;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.util.GuildBankUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Guild implements Cloneable {

    private final Set<GuildMember> members;
    private final GuildBanner guildBanner;
    private final Map<GuildRank, Boolean> bankSettingsMap;
    private GuildMember owner;
    private String guildName;
    private String guildPrefix;
    private Integer score;
    private List<ItemStack> bankContents;
    private Integer bankSize;
    private int guildExp;
    private GuildStage guildStage;

    /**
     * @param members         of set of members in the guild
     * @param owner           the owner of the guild
     * @param guildName       the name of the guild
     * @param guildPrefix     the prefix of the guild (used as its identifier in mongo)
     * @param bankContents    a list of bank items
     * @param bankSize        the size of the bank (in total slots)
     * @param bankSettingsMap a map of which ranks can access the guild bank
     * @param guildEXP        the total exp of the guild
     */
    public Guild(Set<GuildMember> members, GuildMember owner, String guildName, String guildPrefix,
                 List<ItemStack> bankContents, Integer bankSize, Map<GuildRank, Boolean> bankSettingsMap, int guildEXP) {
        this.members = members;
        this.owner = owner;
        this.guildName = guildName;
        this.guildPrefix = guildPrefix;
        this.bankContents = bankContents;
        this.bankSize = bankSize;
        this.bankSettingsMap = bankSettingsMap;
        this.recalculateScore();
        this.guildBanner = new GuildBanner(this);
        this.guildExp = guildEXP;
        this.guildStage = this.expToStage();
    }

    /**
     * @param guildBanner an ItemStack to represent the guild's banner
     */
    public Guild(Set<GuildMember> members, ItemStack guildBanner, GuildMember owner, String guildName, String guildPrefix,
                 List<ItemStack> bankContents, Integer bankSize, Map<GuildRank, Boolean> bankSettingsMap, int guildEXP) {
        this.members = members;
        this.owner = owner;
        this.guildName = guildName;
        this.guildPrefix = guildPrefix;
        this.bankContents = bankContents;
        this.bankSize = bankSize;
        this.bankSettingsMap = bankSettingsMap;
        this.recalculateScore();
        this.guildBanner = new GuildBanner(this, guildBanner);
        this.guildExp = guildEXP;
        this.guildStage = this.expToStage();
    }

    public boolean canAccessBank(GuildRank rank) {
        return rank == GuildRank.OWNER || this.bankSettingsMap.get(rank);
    }

    @Override
    public Guild clone() {
        List<ItemStack> newItems = new ArrayList<>();
        for (ItemStack item : this.bankContents) {
            if (item != null) {
                newItems.add(item.clone());
            } else {
                newItems.add(null);
            }
        }
        Set<GuildMember> newMembers = new HashSet<>();
        for (GuildMember member : this.members) {
            newMembers.add(member.clone());
        }
        return new Guild(newMembers, this.owner.clone(), this.guildName, this.guildPrefix, newItems, this.bankSize, this.bankSettingsMap, this.guildExp);
    }

    /**
     * Disbands this guild, removing its data
     *
     * @param player    who disbanded the guild
     * @param guildData the data object wrapper
     */
    public void disband(Player player, GuildData guildData) {
        Bukkit.getServer().getPluginManager().callEvent(new GuildDisbandEvent(this, player, false));
    }

    /**
     * @return the expected guild stage based on the guild's exp
     */
    private GuildStage expToStage() {
        for (GuildStage stage : GuildStage.values()) {
            if (this.guildExp >= stage.getExp()) {
                return stage;
            }
        }
        return GuildStage.STAGE0;
    }

    public List<ItemStack> getBankContents() {
        return this.bankContents;
    }

    public void setBankContents(List<ItemStack> bankContents) {
        this.bankContents = bankContents;
    }

    public Map<GuildRank, Boolean> getBankSettingsMap() {
        return this.bankSettingsMap;
    }

    public Integer getBankSize() {
        return this.bankSize;
    }

    public void setBankSize(Integer size) {
        this.bankSize = size;
    }

    public GuildBanner getGuildBanner() {
        return this.guildBanner;
    }

    public int getGuildExp() {
        return guildExp;
    }

    /**
     * @param guildExp the new exp to set for the guild
     */
    public void setGuildExp(int guildExp) {
        if (guildExp + this.guildExp > GuildStage.getMaxStage().getExp()) {
            this.guildExp = GuildStage.getMaxStage().getExp();
        } else {
            this.guildExp += guildExp;
        }

        GuildStage newStage = this.expToStage();
        if (newStage == this.getGuildStage()) return;
        this.setGuildStage(newStage); // gained a level!

        for (GuildMember member : this.getMembersWithOwner()) {
            Player player = Bukkit.getOfflinePlayer(member.getUUID()).getPlayer();
            if (player == null) continue;
            sendStageNotification(player, newStage);
        }

        this.guildStage = newStage;
    }

    public String getGuildName() {
        return this.guildName;
    }

    public void setGuildName(String name) {
        this.guildName = name;
    }

    public String getGuildPrefix() {
        return this.guildPrefix;
    }

    public void setGuildPrefix(String prefix) {
        this.guildPrefix = prefix;
    }

    public GuildStage getGuildStage() {
        return guildStage;
    }

    private void setGuildStage(GuildStage guildStage) {
        this.guildStage = guildStage;
    }

    public GuildMember getMember(UUID player) {
        if (this.owner.getUUID().toString().equalsIgnoreCase(player.toString())) {
            return this.owner;
        }
        for (GuildMember member : this.members) {
            if (member.getUUID().toString().equalsIgnoreCase(player.toString())) {
                return member;
            }
        }
        return null;
    }

    public Set<GuildMember> getMembers() {
        return this.members;
    }

    public List<GuildMember> getMembersWithOwner() {
        List<GuildMember> membersWithOwner = new ArrayList<>(this.members);
        membersWithOwner.add(this.owner);
        return membersWithOwner;
    }

    public GuildMember getOwner() {
        return this.owner;
    }

    public Integer getScore() {
        return this.score;
    }

    public boolean hasMinRank(UUID player, GuildRank rank) {
        if (this.owner.getUUID().toString().equalsIgnoreCase(player.toString())) {
            return true;
        }
        for (GuildMember member : this.members) {
            if (member.getUUID().toString().equalsIgnoreCase(player.toString())) {
                if (member.getRank().getRankNumber() <= rank.getRankNumber()) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    /**
     * @param playerName
     * @return
     */
    public boolean isInGuild(String playerName) {
        @SuppressWarnings("deprecation")
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player.hasPlayedBefore()) {
            return this.getMember(player.getUniqueId()) != null;
        }
        return false;
    }

    /**
     *
     */
    public void recalculateScore() {
        this.score = 0;
        for (GuildMember member : members) {
            score += member.getScore();
        }
        score += owner.getScore();
    }

    /**
     * @param uuid of the member to remove
     */
    public void removeMember(UUID uuid) {
        for (GuildMember member : members) {
            if (member.getUUID().toString().equalsIgnoreCase(uuid.toString())) {
                members.remove(member);
                break;
            }
        }
    }

    /**
     * Sends a notification when a guild advances a stage
     *
     * @param player     to send the message / firework to
     * @param guildStage that the guild has just reached
     */
    private void sendStageNotification(Player player, GuildStage guildStage) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 0.5f);
        player.sendMessage("");
        ChatUtils.sendCenteredMessage(player, ColorUtil.format("&6&lGUILD STAGE INCREASE"));
        ChatUtils.sendCenteredMessage(player, ColorUtil.format("&6Your guild has advanced to " + guildStage.getName() + "!"));
        ChatUtils.sendCenteredMessage(player, ColorUtil.format("&6Your max guild size has risen to " + guildStage.getMaxMembers() + "!"));
        if (!guildStage.getStageReward().getMessage().equalsIgnoreCase("")) {
            ChatUtils.sendCenteredMessage(player, ColorUtil.format("&a" + guildStage.getStageReward().getMessage()));
        }
        player.sendMessage("");
        Firework firework = player.getWorld().spawn(player.getEyeLocation(), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.setPower(0);
        meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.ORANGE).build());
        firework.setFireworkMeta(meta);
    }

    public void setBankAccess(GuildRank rank, Boolean canAccess) {
        this.bankSettingsMap.put(rank, canAccess);
    }

    /**
     * @param member
     */
    public void transferOwnership(GuildMember member) {
        this.members.add(new GuildMember(this.owner.getUUID(), GuildRank.OFFICER, this.owner.getScore(), this.owner.getLastKnownName()));
        this.owner = null;
        this.owner = member;
        this.members.remove(member);
    }

    /**
     * Attempts to update prefix for the given guild. However, guilds are keyed / stored by prefix, so it
     * MUST ALWAYS be unique
     *
     * @param guildData the data wrapper of the guild
     * @param prefix    the intended new prefix
     * @return a "re-prefix" result
     */
    public GuildReprefixResult updateGuildPrefix(GuildData guildData, String prefix) { // Must be called async
        Pattern pattern = Pattern.compile("[a-zA-Z]");
        Matcher matcher = pattern.matcher(prefix);
        if (!matcher.find() || (prefix.length() > 6 || prefix.length() < 3)) {
            return GuildReprefixResult.BAD_PREFIX;
        }
        Map<Object, SessionData> guildDataMap = RunicGuilds.getGuildsAPI().getGuildDataMap();
        for (Object otherGuildPrefix : guildDataMap.keySet()) {
            String otherGuildPrefixStr = (String) otherGuildPrefix;
            if (otherGuildPrefixStr.equalsIgnoreCase(prefix)) {
                GuildData otherGuildData = (GuildData) guildDataMap.get(otherGuildPrefixStr);
                if (!otherGuildData.getGuild().getGuildName().equalsIgnoreCase(guildData.getGuild().getGuildName())) {
                    return GuildReprefixResult.PREFIX_NOT_UNIQUE;
                }
            }
        }
        try {
            for (GuildMember member : guildData.getGuild().getMembersWithOwner()) {
                if (GuildBankUtil.isViewingBank(member.getUUID())) {
                    GuildBankUtil.close(Bukkit.getPlayer(member.getUUID()));
                }
//                if (players.containsKey(member.getUUID())) {
//                    players.put(member.getUUID(), prefix);
//                }
            }
            guildDataMap.remove(guildData.getGuild().getGuildPrefix());
            Guild guild = guildData.getGuild();
            guild.setGuildPrefix(prefix);
//            guildData.getMongoData().set("prefix", prefix);
//            guildData.getMongoData().save();
//            GuildData newGuildData = new GuildData(guild, false);
//            guildDataMap.put(prefix, newGuildData);
        } catch (Exception exception) {
            exception.printStackTrace();
            return GuildReprefixResult.INTERNAL_ERROR;
        }
        return GuildReprefixResult.SUCCESSFUL;
    }

}
