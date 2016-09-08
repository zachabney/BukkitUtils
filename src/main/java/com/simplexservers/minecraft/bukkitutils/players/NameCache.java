package com.simplexservers.minecraft.bukkitutils.players;

import com.simplexservers.minecraft.fileutils.db.DBConnection;
import com.simplexservers.minecraft.mojangapi.MinecraftUtil;
import com.simplexservers.minecraft.promptutils.Time;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * A temporary storage for player UUID to name mappings.
 */
public class NameCache {

	/**
	 * The duration for player names to be cached.
	 */
	private static final Time CACHE_DURATION = new Time(5, Time.TimeUnit.MINUTE);
	/**
	 * The number of cached player names to keep in memory.
	 */
	private static final int RAM_CACHE_LIMIT = 10;

	/**
	 * The JavaPlugin to register tasks under.
	 */
	private JavaPlugin plugin;
	/**
	 * The connection to the database with the player cache.
	 */
	private DBConnection dbConn;
	/**
	 * If the database has been initialized.
	 */
	private boolean dbInitialized = false;
	/**
	 * If a listener is registered with Bukkit.
	 */
	private boolean hasBukkitListener = false;
	/**
	 * If the database has cached players.
	 */
	private AtomicBoolean hasCachedPlayers = new AtomicBoolean(false);
	/**
	 * The repeating task to flush the player cache.
	 */
	private BukkitTask flushCacheTask = null;

	/**
	 * The cache of player names stored in memory.
	 */
	private Map<UUID, String> ramNameCache = Collections.synchronizedMap(new LinkedHashMap<UUID, String>(RAM_CACHE_LIMIT + 1, 1F) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<UUID, String> eldest) {
			return size() > RAM_CACHE_LIMIT;
		}
	});

	/**
	 * Creates a NameCache.
	 *
	 * @param plugin The JavaPlugin to register Bukkit calls under.
	 * @param dbConn The connection to the database with the player cache.
	 */
	public NameCache(JavaPlugin plugin, DBConnection dbConn) {
		this.plugin = plugin;
		this.dbConn = dbConn;
	}

	/**
	 * Initializes the database for player cache storage.
	 *
	 * @return true if the database was initialized, false if an error occurred.
	 */
	public boolean initializeDatabase() {
		try {
			String sql =    "CREATE TABLE IF NOT EXISTS player_uuid_cache " +
					"(uuid     CHAR(32)         PRIMARY KEY  NOT NULL," +
					" username VARCHAR(16)                   NOT NULL)";
			Statement stmt = dbConn.getConnection().createStatement();
			stmt.execute(sql);

			return dbInitialized = true;
		} catch (SQLException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Could not initialize the player cache database!", e);
		}

		return false;
	}

	/**
	 * Closes the connection to the database.
	 */
	public void closeDBConnection() {
		dbConn.close();
	}

	/**
	 * Registers a listener for Bukkit events that handles populating the player cache.
	 */
	public void registerBukkitListener() {
		Bukkit.getPluginManager().registerEvents(new NameCacheListener(this), plugin);
		hasBukkitListener = true;
	}

	/**
	 * Caches the UUID/Username combo in the database asynchronously.
	 *
	 * @param uuid The UUID of the player.
	 * @param username The username of the player.
	 */
	public void cachePlayerAsync(UUID uuid, String username) {
		new BukkitRunnable() {
			@Override
			public void run() {
				cachePlayer(uuid, username);
			}
		}.runTaskAsynchronously(plugin);
	}

	/**
	 * Caches the UUID/Username combo in the database.
	 * This method is thread-safe.
	 *
	 * @param uuid The UUID of the player.
	 * @param username The username of the player.
	 */
	public void cachePlayer(UUID uuid, String username) {
		// Cache the player in the RAM cache
		synchronized (ramNameCache) {
			ramNameCache.put(uuid, username);
		}

		// Cache the player in the database
		try {
			String sql = "INSERT OR IGNORE INTO player_uuid_cache VALUES(?, ?); UPDATE player_uuid_cache SET username = ? WHERE uuid = ?";
			synchronized (dbConn) {
				PreparedStatement stmt = dbConn.getConnection().prepareStatement(sql);
				String uuidNoHyphens = uuid.toString().replace("-", "");
				stmt.setString(1, uuidNoHyphens);
				stmt.setString(2, username);
				stmt.executeUpdate();

				hasCachedPlayers.set(true);
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "Could not cache the player in the database.", e);
		}
	}

	/**
	 * Starts the repeating task to flush the cache database of
	 * players that aren't online.
	 */
	public void startCacheFlushTask() {
		flushCacheTask = new BukkitRunnable() {
			@Override
			public void run() {
				synchronized (ramNameCache) {
					ramNameCache.clear();
				}

				if (hasCachedPlayers.get()) {
					StringBuilder where = new StringBuilder();
					Bukkit.getOnlinePlayers().forEach(player -> {
						where.append(where.length() == 0 ? " WHERE" : " AND");
						where.append(" uuid != '" + player.getUniqueId().toString().replace("-", "") + "'");
					});

					try {
						String sql = "DELETE FROM player_uuid_cache" + where;
						synchronized (dbConn) {
							Statement stmt = dbConn.getConnection().createStatement();
							stmt.execute(sql);
						}

						if (where.length() == 0) { // Everything would be clear
							hasCachedPlayers.set(false);
						}
					} catch (SQLException e) {
						plugin.getLogger().log(Level.SEVERE, "Could not flush the player cache in the database.", e);
					}
				}
			}
		}.runTaskTimerAsynchronously(plugin, 0L, CACHE_DURATION.getSeconds() * 20L);
	}

	/**
	 * Stops the repeating cache flush task from running.
	 */
	public void stopCacheFlushTask() {
		if (flushCacheTask != null) {
			flushCacheTask.cancel();
		}
	}

	/**
	 * Gets if the player cache database has been initialized.
	 *
	 * @return true if the database is initialized, false if it is not.
	 */
	public boolean isDBInitialized() {
		return dbInitialized;
	}

	/**
	 * Gets if the player cache has a registered Bukkit listener.
	 *
	 * @return true if a listener is registered, false if one is not.
	 */
	public boolean hasBukkitListener() {
		return hasBukkitListener;
	}

	/**
	 * Gets the UUID stored in cache that is attached to the given username.
	 * Returns null if the UUID could not be retrieved from the cache.
	 *
	 * @param username The username of the player to get the UUID for.
	 * @return The UUID of the player with the given username.
	 */
	public UUID getCachedUUID(String username) {
		// Check the RAM cache first
		synchronized (ramNameCache) {
			byte ramEntryCount = 0;
			UUID ramUUID = null;
			for (Map.Entry<UUID, String> entry : ramNameCache.entrySet()) {
				if (entry.getValue().equals(username)) {
					if (ramEntryCount++ == 0) {
						ramUUID = entry.getKey();
					} else {
						break; // We have multiple entries
					}
				}
			}

			if (ramEntryCount == 1) {
				// We found a entry in the RAM cache
				return ramUUID;
			}
		}

		// Check the database cache
		if (dbInitialized) {
			try {
				String sql = "SELECT uuid FROM player_uuid_cache WHERE username = ?";
				synchronized (dbConn) {
					PreparedStatement stmt = dbConn.getConnection().prepareStatement(sql);
					stmt.setString(1, username);
					ResultSet res = stmt.executeQuery();

					byte entryCount = 0;
					String uuidString = null;
					while (res.next()) {
						if (entryCount++ == 0) {
							uuidString = res.getString("uuid");
						} else {
							break; // We have multiple entries
						}
					}

					if (entryCount == 1) {
						UUID uuid = MinecraftUtil.uuidFromString(uuidString, false);
						// Save the UUID in RAM cache
						synchronized (ramNameCache) {
							ramNameCache.put(uuid, username);
						}

						return uuid;
					}
				}
			} catch (SQLException e) {
				Bukkit.getLogger().log(Level.SEVERE, "Could not query the player cache database!", e);
			}
		}

		// UUID could not be found
		return null;
	}

	/**
	 * Gets the username stored in cache that is attached to the given UUID.
	 * Returns null if the username could not be retrieved from the cache.
	 *
	 * @param uuid The UUID of the player to get the username for.
	 * @return The username of the player with the given UUID.
	 */
	public String getCachedUsername(UUID uuid) {
		// Check the RAM cache first
		synchronized (ramNameCache) {
			String ramName = ramNameCache.get(uuid);
			if (ramName != null) {
				return ramName;
			}
		}

		// Check the database cache
		if (dbInitialized) {
			try {
				String sql = "SELECT username FROM player_uuid_cache WHERE uuid = ?";
				synchronized (dbConn) {
					PreparedStatement stmt = dbConn.getConnection().prepareStatement(sql);
					stmt.setString(1, uuid.toString().replace("-", ""));
					ResultSet res = stmt.executeQuery();

					if (res.next()) { // There will never be multiple entries since uuid is the primary key
						String username = res.getString("username");
						// Save the username in RAM cache
						synchronized (ramNameCache) {
							ramNameCache.put(uuid, username);
						}

						return username;
					}
				}
			} catch (SQLException e) {
				Bukkit.getLogger().log(Level.SEVERE, "Could not query the player cache database!", e);
			}
		}

		// Username could not be found
		return null;
	}

}
