package com.simplexservers.minecraft.bukkitutils.gui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

/**
 * Represents a inventory based GUI.
 */
public class InventoryGUI {

	/**
	 * The InventoryGUIManager the GUI is a child of.
	 */
	private InventoryGUIManager manager;
	/**
	 * The Bukkit inventory being shown to the player.
	 */
	private Inventory inv;
	/**
	 * The inventory slot entries for the GUI.
	 */
	private TreeMap<Integer, InventoryGUIEntry> slotValues;

	private InventoryGUI(InventoryGUIManager manager, Inventory inv, TreeMap<Integer, InventoryGUIEntry> slotValues) {
		this.manager = manager;
		this.inv = inv;
		this.slotValues = slotValues;
	}

	/**
	 * Gets the Bukkit inventory for the GUI.
	 *
	 * @return The Bukkit inventory being shown to the player.
	 */
	public Inventory getInventory() {
		return inv;
	}

	/**
	 * Gets the InventoryGUIEntry for the given inventory slot.
	 * Returns null if there is not an entry for the slot.
	 *
	 * @param slot The slot index to get the inventory entry for.
	 * @return The GUI entry for the given slot.
	 */
	public InventoryGUIEntry getEntry(int slot) {
		return slotValues.get(slot);
	}

	/**
	 * Gets the custom value for the slot.
	 *
	 * @param slot The slot to get the custom value for.
	 * @return The custom value for the entry.
	 */
	public Object getSlotValue(int slot) {
		return slotValues.get(slot).getValue();
	}

	/**
	 * Checks if the inventory is that for the InventoryGUI.
	 *
	 * @param inv The inventory to check against.
	 * @return true if the inventory provided is the GUI's.
	 */
	public boolean isInventory(Inventory inv) {
		return inv != null
				&& this.inv.getType() == inv.getType()
				&& this.inv.getTitle().equals(inv.getTitle());
	}

	/**
	 * Updates the entry at the given slot.
	 *
	 * @param slot The slot to update the entry at.
	 * @param newEntry The new entry to put in the slot.
	 */
	public void updateEntry(int slot, InventoryGUIEntry newEntry) {
		slotValues.put(slot, newEntry);
		if (slot > inv.getSize()) { // Item would be outside the inventory, we need to re-create a new one
			// Calculate the new size of the inventory
			int highestSlotIndex = slotValues.lastKey();
			int invSlots = (int) Math.ceil(highestSlotIndex / 9D) * 9;
			inv = Bukkit.createInventory(null, invSlots, inv.getTitle());
			// Populate the inventory
			slotValues.forEach((slotIndex, entry) -> inv.setItem(slotIndex, entry.getItemStack()));

			// Have the players re-open the GUI
			manager.reopenGUI(this);
		} else {
			inv.setItem(slot, newEntry.getItemStack());
		}
	}

	/**
	 * Removes the entry at the given slot.
	 *
	 * @param slot The slot to remove the entry at.
	 */
	public void removeEntry(int slot) {
		slotValues.remove(slot);
		inv.setItem(slot, null);
	}

	public Collection<Integer> getSlotsWithValue(Object val) {
		ArrayList<Integer> matchingSlots = new ArrayList<>();
		slotValues.entrySet().stream()
				.filter(entry -> entry.getValue().getValue().equals(val))
				.forEach(entry -> matchingSlots.add(entry.getKey()));
		return matchingSlots;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() == InventoryGUI.class) {
			InventoryGUI otherGUI = (InventoryGUI) obj;
			return otherGUI.isInventory(inv);
		}

		return false;
	}

	/**
	 * Builder for an InventoryGUI
	 */
	public static class InventoryGUIBuilder {

		/**
		 * The InventoryGUIManager the GUI is a child of.
		 */
		private InventoryGUIManager manager;
		/**
		 * The title of the inventory.
		 */
		private String title;
		/**
		 * The inventory slot entries for the GUI.
		 */
		private TreeMap<Integer, InventoryGUIEntry> slotValues = new TreeMap<>(Integer::compare);

		public InventoryGUIBuilder(InventoryGUIManager manager, String title) {
			this.manager = manager;
			this.title = title;
		}

		/**
		 * Adds the entry to the next slot for the GUI inventory.
		 *
		 * @param entry The entry to add to the inventory.
		 */
		public void addEntry(InventoryGUIEntry entry) {
			addEntry(getNextSlotID(), entry);
		}

		/**
		 * Adds the entry at the given slot for the GUI inventory.
		 *
		 * @param slot The slot in which to place the entry.
		 * @param entry The entry to add to the inventory.
		 */
		public void addEntry(int slot, InventoryGUIEntry entry) {
			slotValues.put(slot, entry);
		}

		/**
		 * Builds the InventoryGUI.
		 *
		 * @return The constructed inventory GUI.
		 */
		public InventoryGUI build() {
			// Get the highest slot index
			int highestSlotIndex = slotValues.lastKey();
			// Round to the highest 9th
			int invSlots = (int) Math.ceil((highestSlotIndex + 1) / 9D) * 9;

			Inventory inv = Bukkit.createInventory(null, invSlots, title);
			slotValues.forEach((slot, entry) -> inv.setItem(slot, entry.getItemStack())); // Populate the inventory
			return new InventoryGUI(manager, inv, slotValues);
		}

		/**
		 * Gets the next available slot id.
		 *
		 * @return The next available slot id.
		 */
		private int getNextSlotID() {
			if (slotValues.isEmpty()) {
				return 0;
			}

			return slotValues.lastKey() + 1;
		}

	}

}
