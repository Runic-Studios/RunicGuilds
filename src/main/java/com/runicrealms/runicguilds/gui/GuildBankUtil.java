package com.runicrealms.runicguilds.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Pattern;

import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.item.util.ItemRemover;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicguilds.data.GuildData;
import com.runicrealms.runicguilds.guilds.GuildRank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.runicrealms.runicguilds.data.GuildUtil;
import com.runicrealms.runicguilds.guilds.Guild;

public class GuildBankUtil implements Listener {

	private static final Map<UUID, ViewerInfo> viewers = new HashMap<>();

	public static void open(Player player, Integer page) {
		open(player, page, GuildUtil.getGuildData(player.getUniqueId()).getData().getGuildPrefix());
	}

	public static void open(Player player, Integer page, String prefix) {
		Guild guild = GuildUtil.getGuildData(prefix).getData();
		Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', "Guild Bank"));
		if (guild.getBankSize() > 45 && page != guild.getBankSize() / 45) {
			inventory.setItem(8, new ItemBuilder(Material.ARROW, 1, "&6Next Page").getItem());
		}
		if (page > 1) {
			inventory.setItem(0, new ItemBuilder(Material.ARROW, 1, "&6Previous Page").getItem());
		}
		int maxBankPages = Plugin.getInstance().getConfig().getInt("max-bank-pages");
		int pagePrice = (int) Math.pow(2, guild.getBankSize() / 45 + 8);
		if (guild.getBankSize() / 45 < maxBankPages) {
			if (guild.hasMinRank(player.getUniqueId(), GuildRank.OFFICER)) {
				inventory.setItem(4, new ItemBuilder(Material.GOLD_INGOT, (int) (guild.getBankSize() / 45), "&6Purchase New Bank Page", "&eCost: " + pagePrice + " coins").getItem());
			} else {
				inventory.setItem(4, new ItemBuilder(Material.GOLD_INGOT, (int) (guild.getBankSize() / 45), "&6Purchase New Bank Page", "&cYou must be of rank officer or higher to do this!", "&eCost: " + pagePrice + " coins").getItem());
			}
		} else {
			inventory.setItem(4, new ItemBuilder(Material.GOLD_INGOT, (int) (guild.getBankSize() / 45), "&6Purchase New Bank Page", "&cYou have reached the max amount of pages!").getItem());
		}
		for (int i = 1; i < 8; i++) {
			if (i != 4) {
				inventory.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE, 1, " ").getItem());
			}
		}
		for (int i = (page - 1) * 45; i < page * 45; i++) {
			inventory.setItem(i - (page - 1) * 45 + 9, guild.getBank().get(i));
		}
		player.openInventory(inventory);
		viewers.put(player.getUniqueId(), new ViewerInfo(page, guild.getGuildPrefix()));
	}

	public static void close(Player player) {
		player.closeInventory();
		viewers.remove(player.getUniqueId());
	}

	private static void saveToBank(Inventory inventory, Integer page, UUID uuid) {
		GuildData guildData = GuildUtil.getGuildData(uuid);
		List<ItemStack> bank = new ArrayList<ItemStack>(guildData.getData().getBank());
		for (int i = (page - 1) * 45; i < page * 45; i++) {
			bank.set(i, inventory.getItem(i - ((page - 1) * 45)));
		}
		guildData.getData().setBank(bank);
		guildData.queueToSave();
	}

	public static boolean isViewingBank(UUID uuid) {
		return viewers.containsKey(uuid);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		viewers.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getCurrentItem() != null) {
			if (event.getCurrentItem().getType() != Material.AIR) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					if (viewers.containsKey(player.getUniqueId())) {
						ItemStack clickedItem = event.getCurrentItem().clone();
						GuildData guildData = GuildUtil.getGuildData(player.getUniqueId());
						Guild guild = guildData.getData();
						Inventory bankInventory = Bukkit.createInventory(null, 45, "");
						Integer currentPage = new Integer(viewers.get(player.getUniqueId()).getPage());
						for (int i = 0; i < 45; i++) {
							if (guild.getBank().get((currentPage - 1) * 45 + i) != null) {
								bankInventory.setItem(i, guild.getBank().get((currentPage - 1) * 45 + i));
							}
						}
						ViewerInfo viewer = viewers.get(player.getUniqueId());
						if (event.getRawSlot() < event.getInventory().getSize() && event.getRawSlot() < 9) {
							if (event.getRawSlot() == 0 && event.getCurrentItem().getType() == Material.ARROW) {
								close(player);
								open(player, currentPage - 1);
							} else if (event.getRawSlot() == 8 && event.getCurrentItem().getType() == Material.ARROW) {
								close(player);
								open(player, currentPage + 1);
							} else if (event.getRawSlot() == 4 && event.getCurrentItem().getType() == Material.GOLD_INGOT) {
								if (guild.hasMinRank(player.getUniqueId(), GuildRank.OFFICER)) {
									if (guild.getBankSize() / 45 < Plugin.getInstance().getConfig().getInt("max-bank-pages")) {
										if (player.getInventory().contains(Material.GOLD_NUGGET, (int) Math.pow(2, guild.getBankSize() / 45 + 8))) {
											ItemRemover.takeItem(player, Material.GOLD_NUGGET, (int) Math.pow(2, guild.getBankSize() / 45 + 8));
											guild.setBankSize(guild.getBankSize() + 45);
											for (int i = 0; i < 45; i++) {
												guild.getBank().add(null);
											}
											guildData.queueToSave();
											refreshViewers(viewer);
										} else {
											event.setCancelled(true);
											return;
										}
									} else {
										event.setCancelled(true);
										return;
									}
								} else {
									event.setCancelled(true);
									return;
								}
							}
						} else if (event.getRawSlot() < event.getInventory().getSize()) {
							if (player.getInventory().firstEmpty() == -1) {
								player.getWorld().dropItem(player.getLocation(), event.getCurrentItem());
							} else {
								player.getInventory().addItem(event.getCurrentItem());
							}
							player.updateInventory();
							bankInventory.setItem(event.getSlot() - 9, new ItemStack(Material.AIR));
							saveToBank(bankInventory, viewer.getPage(), player.getUniqueId());
						} else {
							if (event.getCurrentItem().hasItemMeta()) {
								if (event.getCurrentItem().getItemMeta().hasLore()) {
									if (AttributeUtil.getCustomString(event.getCurrentItem(), "soulbound").equalsIgnoreCase("true")) {
										player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
										player.sendMessage(ChatColor.RED + "You cannot store soulbound items in the guild bank!");
										event.setCancelled(true);
										return;
									} else if (AttributeUtil.getCustomString(event.getCurrentItem(), "untradeable").equalsIgnoreCase("true")) {
										player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
										player.sendMessage(ChatColor.RED + "You cannot store untradeable items in the guild bank!");
										event.setCancelled(true);
										return;
									} else if (loreContainsString(event.getCurrentItem().getItemMeta().getLore(), "quest item")) {
										event.setCancelled(true);
										return;
									}
								}
							}
							if (bankInventory.firstEmpty() == -1) {
								event.setCancelled(true);
								return;
							} else {
								bankInventory.addItem(clickedItem);
							}
							player.getInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
							saveToBank(bankInventory, viewer.getPage(), player.getUniqueId());
						}
						refreshViewers(viewer);
						event.setCancelled(true);
					}
				}
			}
		}
	}

	private static boolean loreContainsString(List<String> lore, String search) {
		for (String line : lore) {
			if (Pattern.compile(Pattern.quote(search), Pattern.CASE_INSENSITIVE).matcher(line).find()) {
				return true;
			}
		}
		return false;
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (viewers.containsKey(event.getPlayer().getUniqueId())) {
			viewers.remove(event.getPlayer().getUniqueId());
		}
	}

	@EventHandler
	public void onDropItemEvent(PlayerDropItemEvent event) {
		if (viewers.containsKey(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);
		}
	}

	private static void refreshViewers(ViewerInfo viewer) {
		Map<UUID, Integer> playersToRefresh = new HashMap<>();
		for (Entry<UUID, ViewerInfo> entry : viewers.entrySet()) {
			if (entry.getValue().getGuildPrefix().equalsIgnoreCase(viewer.getGuildPrefix())) {
				playersToRefresh.put(entry.getKey(), entry.getValue().getPage());
			}
		}
		for (Entry<UUID, Integer> playerToRefresh : playersToRefresh.entrySet()) {
			Player otherPlayer = Bukkit.getPlayer(playerToRefresh.getKey());
			close(otherPlayer);
			open(otherPlayer, playerToRefresh.getValue());
		}
	}

	private static class ViewerInfo {

		private final Integer currentPage;
		private final String guildPrefix;

		public ViewerInfo(Integer currentPage, String guildPrefix) {
			this.currentPage = currentPage;
			this.guildPrefix = guildPrefix;
		}

		public Integer getPage() {
			return this.currentPage;
		}

		public String getGuildPrefix() {
			return this.guildPrefix;
		}

	}

}
