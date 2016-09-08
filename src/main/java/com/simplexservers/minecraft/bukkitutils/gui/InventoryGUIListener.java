package com.simplexservers.minecraft.bukkitutils.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The Bukkit listener to handle inventory GUI events.
 */
public class InventoryGUIListener implements Listener {

	/**
	 * The Bukkit plugin being served.
	 */
	private JavaPlugin plugin;
	/**
	 * The InventoryGUIManager the listener works for.
	 */
	private InventoryGUIManager guiManager;

	public InventoryGUIListener(JavaPlugin plugin, InventoryGUIManager guiManager) {
		this.plugin = plugin;
		this.guiManager = guiManager;
	}

	/**
	 * Handles de-registering the open GUI when the player closes the inventory.
	 *
	 * @param event The Bukkit InventoryCloseEvent.
	 */
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		guiManager.closeGUI((Player) event.getPlayer(), false);
	}

	/**
	 * Handles de-registering the open GUI when the player's inventory closes
	 * due to a teleport across worlds.
	 *
	 * @param event The Bukkit PlayerTeleportEvent.
	 */
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) {
			guiManager.closeGUI(event.getPlayer());
		}
	}

	/**
	 * Handles when a player selects an entry in the GUI.
	 *
	 * @param event The Bukkit InventoryClickEvent.
	 */
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		InventoryGUI gui = guiManager.getInventoryGUI(player);
		// Check if the player is actually in the GUI
		if (gui != null && gui.isInventory(event.getClickedInventory())) {
			event.setCancelled(true);

			InventoryGUIEntry selectedEntry = gui.getEntry(event.getSlot());
			if (selectedEntry != null) {
				// Invoke the event
				InventoryGUISelectEvent selectEvent = new InventoryGUISelectEvent(player, gui, selectedEntry);
				Bukkit.getPluginManager().callEvent(selectEvent);
			}
		}
	}

	/**
	 * Handles preventing the player from interacting with the GUI inventory.
	 *
	 * @param event The Bukkit event.
	 */
	@EventHandler
	public void onInventoryInteract(InventoryDragEvent event) {
		Player player = (Player) event.getWhoClicked();
		InventoryGUI gui = guiManager.getInventoryGUI(player);
		if (gui != null) {
			event.setCancelled(true);
			player.updateInventory();
		}
	}

	/**
	 * Handles preventing the player from interacting with the GUI inventory.
	 *
	 * @param event The Bukkit event.
	 */
	@EventHandler
	public void onInventoryMove(InventoryMoveItemEvent event) {
		if (isGUIInv(event.getSource()) || isGUIInv(event.getDestination())) {
			event.setCancelled(true);
		}
	}

	/**
	 * Gets whether or not the given inventory is a GUI inventory.
	 *
	 * @param inv The inventory to check against.
	 * @return true if the inventory is a GUI inventory, false otherwise.
	 */
	private boolean isGUIInv(Inventory inv) {
		for (HumanEntity viewer : inv.getViewers()) {
			InventoryGUI gui = guiManager.getInventoryGUI((Player) viewer);
			if (gui != null) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Handles closing player inventories when the server reloads.
	 *
	 * @param event The Bukkit PluginDisableEvent.
	 */
	@EventHandler
	public void onPluginDisable(PluginDisableEvent event) {
		if (event.getPlugin().getName().equals(plugin.getName())) {
			Bukkit.getOnlinePlayers().forEach(guiManager::closeGUI);
		}
	}

}
