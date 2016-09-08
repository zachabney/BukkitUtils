package com.simplexservers.minecraft.bukkitutils.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player selects an entry in the inventory GUI.
 */
public class InventoryGUISelectEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	/**
	 * The player that selected the entry.
	 */
	private Player player;
	/**
	 * The InventoryGUI the player interacted with.
	 */
	private InventoryGUI gui;
	/**
	 * The entry the player selected.
	 */
	private InventoryGUIEntry selectedEntry;

	public InventoryGUISelectEvent(Player player, InventoryGUI gui, InventoryGUIEntry selectedEntry) {
		this.player = player;
		this.gui = gui;
		this.selectedEntry = selectedEntry;
	}

	/**
	 * Gets the player that selected the entry.
	 *
	 * @return The player that made the selection.
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Gets the InventoryGUI the player interacted with.
	 *
	 * @return The GUI the player interacted with.
	 */
	public InventoryGUI getGUI() {
		return gui;
	}

	/**
	 * Gets the entry the player selected.
	 *
	 * @return The entry the player selected.
	 */
	public InventoryGUIEntry getSelectedEntry() {
		return selectedEntry;
	}

	/**
	 * Gets the custom value attached to the selected entry.
	 *
	 * @return The custom value with the entry.
	 */
	public Object getSelectedValue() {
		return selectedEntry.getValue();
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
