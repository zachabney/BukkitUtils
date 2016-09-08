package com.simplexservers.minecraft.bukkitutils.nms;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class NMSUtil {

	public static Class<?> getNMSClass(String nmsClassName) throws ClassNotFoundException {
		String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		String name = "net.minecraft.server." + version + "." + nmsClassName;
		Class<?> nmsClass = Class.forName(name);
		return nmsClass;
	}

	public static Object getConnection(Player player) throws Exception {
		Method getHandle = player.getClass().getMethod("getHandle");
		Object nmsPlayer = getHandle.invoke(player);
		Field connField = nmsPlayer.getClass().getField("playerConnection");
		Object conn = connField.get(nmsPlayer);
		return conn;
	}

}
