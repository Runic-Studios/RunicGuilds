package com.runicrealms.runicguilds.guild;

import com.runicrealms.plugin.utilities.ChatUtils;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicguilds.RunicGuilds;
import com.runicrealms.runicguilds.guild.stage.GuildStage;
import com.runicrealms.runicguilds.model.GuildData;
import com.runicrealms.runicguilds.ui.GuildBankUtil;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Guild implements Cloneable {

    private final Set<GuildMember> members;
    private final GuildBanner guildBanner;
    private final Map<GuildRank, Boolean> bankAccess;
    private GuildMember owner;
    private String guildName;
    private String guildPrefix;
    private Integer score;
    private List<ItemStack> bank;
    private Integer bankSize;
    private int guildExp;
    private GuildStage guildStage;

    /**
     * @param members
     * @param owner
     * @param guildName
     * @param guildPrefix
     * @param bank
     * @param bankSize
     * @param bankAccess
     * @param guildEXP
     */
    public Guild(Set<GuildMember> members, GuildMember owner, String guildName, String guildPrefix,
                 List<ItemStack> bank, Integer bankSize, Map<GuildRank, Boolean> bankAccess, int guildEXP) {
        this.members = members;
        this.owner = owner;
        this.guildName = guildName;
        this.guildPrefix = guildPrefix;
        this.bank = bank;
        this.bankSize = bankSize;
        this.bankAccess = bankAccess;
        this.recalculateScore();
        this.guildBanner = new GuildBanner(this);
        this.guildExp = guildEXP;
        this.guildStage = this.expToStage();
    }

    public void recalculateScore() {
        this.score = 0;
        for (GuildMember member : members) {
            score += member.getScore();
        }
        score += owner.getScore();
    }

    /**
     * @return
     */
    private GuildStage expToStage() {
        for (GuildStage stage : GuildStage.values()) {
            if (this.guildExp >= stage.getExp()) {
                return stage;
            }
        }
        return GuildStage.STAGE0;
    }

    /**
     * @param members
     * @param guildBanner
     * @param owner
     * @param guildName
     * @param guildPrefix
     * @param bank
     * @param bankSize
     * @param bankAccess
     * @param guildEXP
     */
    public Guild(Set<GuildMember> members, ItemStack guildBanner, GuildMember owner, String guildName, String guildPrefix,
                 List<ItemStack> bank, Integer bankSize, Map<GuildRank, Boolean> bankAccess, int guildEXP) {
        this.members = members;
        this.owner = owner;
        this.guildName = guildName;
        this.guildPrefix = guildPrefix;
        this.bank = bank;
        this.bankSize = bankSize;
        this.bankAccess = bankAccess;
        this.recalculateScore();
        this.guildBanner = new GuildBanner(this, guildBanner);
        this.guildExp = guildEXP;
        this.guildStage = this.expToStage();
    }

    public boolean canAccessBank(GuildRank rank) {
        return rank == GuildRank.OWNER || this.bankAccess.get(rank);
    }

    @Override
    public Guild clone() {
        List<ItemStack> newItems = new ArrayList<>();
        for (ItemStack item : this.bank) {
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
        return new Guild(newMembers, this.owner.clone(), this.guildName, this.guildPrefix, newItems, this.bankSize, this.bankAccess, this.guildExp);
    }

    public List<ItemStack> getBank() {
        return this.bank;
    }

    public void setBank(List<ItemStack> bank) {
        this.bank = bank;
    }

    public Map<GuildRank, Boolean> getBankAccess() {
        return this.bankAccess;
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
     * @param guildExp
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

    public GuildStage getGuildStage() {
        return guildStage;
    }

    public List<GuildMember> getMembersWithOwner() {
        List<GuildMember> membersWithOwner = new ArrayList<>(this.members);
        membersWithOwner.add(this.owner);
        return membersWithOwner;
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

    private void setGuildStage(GuildStage guildStage) {
        this.guildStage = guildStage;
    }

    public Set<GuildMember> getMembers() {
        return this.members;
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

    public void increasePlayerScore(UUID player, Integer score) {
        GuildMember member = getMember(player);
        member.setScore(member.getScore() + score);
        this.recalculateScore();
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

    public boolean isInGuild(String playerName) {
        @SuppressWarnings("deprecation")
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player.hasPlayedBefore()) {
            return this.getMember(player.getUniqueId()) != null;
        }
        return false;
    }

    public void removeMember(UUID uuid) {
        for (GuildMember member : members) {
            if (member.getUUID().toString().equalsIgnoreCase(uuid.toString())) {
                members.remove(member);
                break;
            }
        }
    }

    public void setBankAccess(GuildRank rank, Boolean canAccess) {
        this.bankAccess.put(rank, canAccess);
    }

    public void setPlayerScore(UUID player, Integer score) {
        GuildMember member = getMember(player);
        member.setScore(score);
        this.recalculateScore();
    }

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
        Map<String, GuildData> guildDataMap = RunicGuilds.getRunicGuildsAPI().getGuildDataMap();
        for (String otherGuild : guildDataMap.keySet()) {
            if (otherGuild.equalsIgnoreCase(prefix)) {
                if (!guildDataMap.get(otherGuild).getGuild().getGuildName().equalsIgnoreCase(guildData.getGuild().getGuildName())) {
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

    public String getGuildName() {
        return this.guildName;
    }

    public String getGuildPrefix() {
        return this.guildPrefix;
    }

    public void setGuildPrefix(String prefix) {
        this.guildPrefix = prefix;
    }

    public void setGuildName(String name) {
        this.guildName = name;
    }

}
