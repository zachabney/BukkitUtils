package com.simplexservers.minecraft.bukkitutils.players;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * The hook for NameResolver cache to Bukkit.
 */
public class NameCacheListener implements Listener {

	/**
	 * The cache to store players in.
	 */
	private NameCache cache;

	/**
	 * Creates a NameCacheListener that stores player in the given cache.
	 *
	 * @param cache The cache to store players in.
	 */
	public NameCacheListener(NameCache cache) {
		this.cache = cache;
	}

	/**
	 * Handles caching a player when the join the server.
	 *
	 * @param event The Bukkit PlayerJoinEvent.
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		cache.cachePlayerAsync(player.getUniqueId(), player.getName());
	}

}
