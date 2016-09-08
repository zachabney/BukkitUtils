package com.simplexservers.minecraft.bukkitutils.players;

import com.simplexservers.minecraft.mojangapi.MinecraftUtil;
import com.simplexservers.minecraft.mojangapi.MojangAPI;
import com.simplexservers.minecraft.mojangapi.MojangAPIException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.logging.Level;

/**
 * Connects to the Mojang API to resolve Minecraft username/UUID conversions.
 */
public class NameResolver {

	/**
	 * The cache of names.
	 */
	private NameCache cache;

	/**
	 * Creates a NameResolver with cache storage.
	 *
	 * @param cache The storage for name cache.
	 */
	public NameResolver(NameCache cache) {
		this.cache = cache;
	}

	/**
	 * Initializes the resolver cache.
	 * This should only be called when the plugin is enabled.
	 */
	public void initializeCache() {
		if (!cache.isDBInitialized()) {
			cache.initializeDatabase();
		}

		if (!cache.hasBukkitListener()) {
			cache.registerBukkitListener();
		}

		cache.startCacheFlushTask();
	}

	/**
	 * Closes all resources active with the cache.
	 */
	public void closeCache() {
		cache.stopCacheFlushTask();
		cache.closeDBConnection();
	}

	/**
	 * Gets the UUID of the player with the given username.
	 * Returns null if the UUID could not be found.
	 *
	 * Checks online players, player cache, and Mojang API for UUID.
	 *
	 * @param username The username of the player to get the UUID for.
	 * @return The UUID of the player.
	 */
	public UUID getUUID(String username) {
		if (!MinecraftUtil.isValidUsername(username)) {
			return null;
		}

		// Check if the player is currently online
		Player onlinePlayer = Bukkit.getPlayerExact(username);
		if (onlinePlayer != null) {
			return onlinePlayer.getUniqueId();
		}

		// Check if the player is in the cache
		UUID cachedUUID = cache.getCachedUUID(username);
		if (cachedUUID != null) {
			return cachedUUID;
		}

		// Contact the Mojang API
		try {
			UUID mojangUUID = MojangAPI.requestUUIDForUsername(username);

			if (mojangUUID != null) {
				// Cache the found UUID
				cache.cachePlayerAsync(mojangUUID, username);

				return mojangUUID;
			}
		} catch (MojangAPIException e) {
			Bukkit.getLogger().log(Level.WARNING, "Could not make a request to the MojangAPI.", e);
		}

		// The player UUID could not be found
		return null;
	}

	/**
	 * Gets the latest username of the player with the given UUID.
	 * Returns null if the username could not be found.
	 *
	 * Checks online players, player cache, and Mojang API for the username.
	 *
	 * @param uuid The UUID of the player to get the username for.
	 * @return The username of the player.
	 */
	public String getUsername(UUID uuid) {
		// Check if the player is currently online
		Player onlinePlayer = Bukkit.getPlayer(uuid);
		if (onlinePlayer != null) {
			return onlinePlayer.getName();
		}

		// Check if the player is in the cache
		String cachedUsername = cache.getCachedUsername(uuid);
		if (cachedUsername != null) {
			return cachedUsername;
		}

		// Contact the Mojang API
		try {
			String mojangUsername = MojangAPI.requestUsernameForUUID(uuid);

			if (mojangUsername != null) {
				// Cache the found username
				cache.cachePlayerAsync(uuid, mojangUsername);

				return mojangUsername;
			}
		} catch (MojangAPIException e) {
			Bukkit.getLogger().log(Level.WARNING, "Could not make a request to the MojangAPI.", e);
		}

		// The player username could not be found
		return null;
	}

	/**
	 * Gets the Bukkit OfflinePlayer that has the given username without checking
	 * the servers player data files.
	 * Returns null if the OfflinePlayer could not be found.
	 *
	 * @param username The username of the OfflinePlayer to get.
	 * @return The Bukkit OfflinePlayer that has the given username.
	 */
	public OfflinePlayer getOfflinePlayer(String username) {
		return getOfflinePlayer(username, false);
	}

	/**
	 * Gets the Bukkit OfflinePlayer that has the given username.
	 * Returns null if the OfflinePlayer could not be found.
	 *
	 * @param username The username of the OfflinePlayer to get.
	 * @param checkPlayerdata If it should check the server's player data files for the player if the UUID could not be found.
	 *                        If true, the operation could take a long time if many players have joined the server.
	 * @return The Bukkit OfflinePlayer that has the given username.
	 */
	public OfflinePlayer getOfflinePlayer(String username, boolean checkPlayerdata) {
		// Try to get the player's UUID
		UUID uuid = getUUID(username);
		if (uuid != null) {
			return Bukkit.getOfflinePlayer(uuid);
		}

		if (checkPlayerdata) {
			// Check the OfflinePlayer cache
			for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
				if (offlinePlayer.getName().equals(username)) {
					return offlinePlayer;
				}
			}
		}

		// The OfflinePlayer could not be found
		return null;
	}

}
