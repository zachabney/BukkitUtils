package com.simplexservers.minecraft.bukkitutils.gui;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class InventoryGUIManager {

	private HashMap<Player, InventoryGUI> openGUIs = new HashMap<>();

	public void openGUI(Player player, InventoryGUI gui) {
		openGUIs.put(player, gui);
		player.openInventory(gui.getInventory());
	}

	public void closeGUI(Player player) {
		closeGUI(player, true);
	}

	public void closeGUI(Player player, boolean closeInventory) {
		openGUIs.remove(player);
		if (closeInventory) {
			player.closeInventory();
		}
	}

	public boolean isInGUI(Player player) {
		return openGUIs.containsKey(player);
	}

	public InventoryGUI getInventoryGUI(Player player) {
		return openGUIs.get(player);
	}

	public Set<Map.Entry<Player, InventoryGUI>> getOpenGUIs() {
		return openGUIs.entrySet();
	}

	public void reopenGUI(InventoryGUI gui) {
		openGUIs.entrySet().stream()
				.filter(entry -> entry.getValue().equals(gui))
				.forEach(entry -> {
			Player player = entry.getKey();
			player.closeInventory();
			player.openInventory(gui.getInventory());
		});
	}

}
