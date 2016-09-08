package com.simplexservers.minecraft.bukkitutils.nms;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class PlayerUtils {

	private static final long ACTIONBAR_CLIENT_DISPLAY_DURATION = 60L;
	private static final long ACTIONBAR_CLIENT_FADEOUT_DURATION = 20L;

	/**
	 * Sends an action bar message to the player.
	 *
	 * @param plugin The Bukkit plugin sending the message.
	 * @param player The player to send the action bar message to.
	 * @param message The message to send to the player.
	 * @param duration How long, in ticks, the message should be shown to the player.
	 * @throws Exception If an error occurred sending the action bar message.
	 */
	public static void sendActionBarMessage(JavaPlugin plugin, Player player, String message, long duration) throws Exception {
		if (duration <= 0) {
			throw new IllegalArgumentException("Invalid duration '" + duration + "'. Must be positive");
		}

		// Build the IChatBaseComponent
		Class<?> chatComponentClass = NMSUtil.getNMSClass("IChatBaseComponent");
		Class<?> chatSerializerClass = null;
		for (Class<?> clazz : chatComponentClass.getDeclaredClasses()) {
			if (clazz.getSimpleName().equals("ChatSerializer")) {
				chatSerializerClass = clazz;
				break;
			}
		}
		if (chatSerializerClass == null) {
			throw new ClassNotFoundException("ChatSerializer class could not be found.");
		}

		Method a = chatSerializerClass.getMethod("a", String.class);
		Object nmsChatComponent = a.invoke(null, "{\"text\": \"" + message + "\"}");
		Object nmsEmptyStringChatComponent = a.invoke(null, "{\"text\": \"\"}");

		// Build the packet
		Class<?> chatPacketClass = NMSUtil.getNMSClass("PacketPlayOutChat");
		Constructor<?> chatPacketConstructor = chatPacketClass.getConstructor(chatComponentClass, byte.class);
		Object nmsChatPacket = chatPacketConstructor.newInstance(nmsChatComponent, (byte) 2);
		Object nmsEmptyChatPacket = chatPacketConstructor.newInstance(nmsEmptyStringChatComponent, (byte) 2);

		Object nmsPlayerConnection = NMSUtil.getConnection(player);
		Method sendPacketMethod = nmsPlayerConnection.getClass().getMethod("sendPacket", NMSUtil.getNMSClass("Packet"));

		// Continuously send the packet so it doesn't get cleared from the player's screen
		BukkitTask packetTask = new BukkitRunnable() {
			@Override
			public void run() {
				try {
					sendPacketMethod.invoke(nmsPlayerConnection, nmsChatPacket);
				} catch (Exception e) {
					plugin.getLogger().warning("An error occurred sending the action bar packet to the player.");
				}
			}
		}.runTaskTimer(plugin, 0L, ACTIONBAR_CLIENT_DISPLAY_DURATION - ACTIONBAR_CLIENT_FADEOUT_DURATION);

		// Stop the packetTask
		if (duration < ACTIONBAR_CLIENT_DISPLAY_DURATION) {
			new BukkitRunnable() {
				@Override
				public void run() {
					packetTask.cancel();
					// Send an empty string to clear out the action bar for precise timing
					try {
						sendPacketMethod.invoke(nmsPlayerConnection, nmsEmptyChatPacket);
					} catch (Exception e) {
						plugin.getLogger().warning("Error clearing player action bar.");
					}
				}
			}.runTaskLater(plugin, duration);
		} else {
			// Stop the scheduled task and send a packet so it'll fade out right at the duration time
			new BukkitRunnable() {
				@Override
				public void run() {
					packetTask.cancel();
					try {
						sendPacketMethod.invoke(nmsPlayerConnection, nmsChatPacket);
					} catch (Exception e) {
						plugin.getLogger().warning("Error clearing player action bar.");
					}
				}
			}.runTaskLater(plugin, duration - ACTIONBAR_CLIENT_DISPLAY_DURATION);
		}
	}

}
