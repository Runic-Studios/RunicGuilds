package com.runicrealms.runicguilds.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.runicrealms.runicguilds.config.GuildUtil;
import com.runicrealms.runicguilds.guilds.Guild;

import net.md_5.bungee.api.ChatColor;

public class GuildBankUtil implements Listener {

	private static Map<UUID, ViewerInfo> viewers = new HashMap<UUID, ViewerInfo>();

	public static void open(Player player, Integer page) {
		open(player, page, GuildUtil.getGuild(player.getUniqueId()).getGuildPrefix());
	}

	public static void open(Player player, Integer page, String prefix) {
		Guild guild = GuildUtil.getGuild(prefix);
		Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', "Guild Bank"));
		if (guild.getBankSize() > 46 && page != guild.getBankSize() / 46) {
			inventory.setItem(8, new ItemBuilder(Material.ARROW, 1, "&6Next Page").getItem());
		}
		if (page > 1) {
			inventory.setItem(0, new ItemBuilder(Material.ARROW, 1, "&6Previous Page").getItem());
		}
		for (int i = (page - 1) * 45; i < page * 45; i++) {
			inventory.setItem(i + 9, guild.getBank().get(i));
		}
		player.openInventory(inventory);
		viewers.put(player.getUniqueId(), new ViewerInfo(page, guild.getGuildPrefix()));
	}

	public static void close(Player player) {
		player.closeInventory();
		viewers.remove(player.getUniqueId());
	}

	private static void saveToBank(Inventory inventory, Integer page, UUID uuid) {
		List<ItemStack> bank = new ArrayList<ItemStack>(GuildUtil.getGuild(uuid).getBank());
		for (int i = (page - 1) * 45; i < page * 45; i++) {
			bank.set(i, inventory.getItem(i - ((page - 1) * 45)));
		}
		Guild guild = GuildUtil.getGuild(uuid);
		guild.setBank(bank);
		GuildUtil.getGuildFiles().get(GuildUtil.getPlayerCache().get(uuid)).save(guild);
	}

	public static boolean isViewingBank(UUID uuid) {
		return viewers.containsKey(uuid);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (viewers.containsKey(event.getPlayer().getUniqueId())) {
			viewers.remove(event.getPlayer().getUniqueId());
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getCurrentItem() != null) {
			if (event.getCurrentItem().getType() != Material.AIR) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					if (viewers.containsKey(player.getUniqueId())) {
						ItemStack clickedItem = event.getCurrentItem().clone();
						Guild guild = GuildUtil.getGuild(player.getUniqueId());
						Inventory bankInventory = Bukkit.createInventory(null, 54, "");
						Integer currentPage = viewers.get(player.getUniqueId()).getPage();
						for (int i = 0; i < 45; i++) {
							if (guild.getBank().get((currentPage - 1) * 54 + i) != null) {
								bankInventory.setItem(i, guild.getBank().get((currentPage - 1) * 54 + i));
							}
						}
						ViewerInfo viewer = viewers.get(player.getUniqueId());
						if (event.getRawSlot() < event.getInventory().getSize() && event.getRawSlot() < 9) {
							if (event.getRawSlot() == 0 && event.getCurrentItem().getType() == Material.ARROW) {
								close(player);
								open(player, viewer.getPage() - 1);
							} else if (event.getRawSlot() == 8 && event.getCurrentItem().getType() == Material.ARROW) {
								close(player);
								open(player, viewer.getPage() + 1);
							}
						} else if (event.getRawSlot() < event.getInventory().getSize()) {
							player.getInventory().addItem(event.getCurrentItem());
							bankInventory.setItem(event.getSlot(), new ItemStack(Material.AIR));
							//bankInventory.remove(event.getCurrentItem());
							saveToBank(bankInventory, viewer.getPage(), player.getUniqueId());
						} else {
							player.getInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
							//player.getInventory().remove(event.getCurrentItem());
							for (int i = 0; i < 45; i++) {
								if (bankInventory.getItem(i) == null) {
									bankInventory.setItem(i, clickedItem);
									break;
								}
							}
							saveToBank(bankInventory, viewer.getPage(), player.getUniqueId());
						}
						for (Entry<UUID, ViewerInfo> entry : viewers.entrySet()) {
							if (entry.getValue().getGuildPrefix().equalsIgnoreCase(viewer.getGuildPrefix())) {
								Player otherPlayer = Bukkit.getPlayer(entry.getKey());
								close(otherPlayer);
								open(otherPlayer, viewer.getPage());
							}
						}
					}
				}
			}
		}
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

	private static class ViewerInfo {

		private Integer currentPage;
		private String guildPrefix;

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
