package com.simplexservers.minecraft.bukkitutils.gui;

import org.bukkit.inventory.ItemStack;

/**
 * Represents an entry in an IInventoryGUI.
 */
public class InventoryGUIEntry {

	/**
	 * The Bukkit ItemStack in the inventory.
	 */
	private ItemStack stack;
	/**
	 * The custom value attached to the entry.
	 */
	private Object value;

	public InventoryGUIEntry(ItemStack stack, Object value) {
		this.stack = stack;
		this.value = value;
	}

	/**
	 * Gets the Bukkit ItemStack in the inventory.
	 *
	 * @return The shown item in the GUI.
	 */
	public ItemStack getItemStack() {
		return stack;
	}

	/**
	 * Gets the custom value attached to the entry.
	 *
	 * @return The custom entry value.
	 */
	public Object getValue() {
		return value;
	}

}
