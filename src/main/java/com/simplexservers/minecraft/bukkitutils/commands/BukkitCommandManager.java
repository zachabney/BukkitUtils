package com.simplexservers.minecraft.bukkitutils.commands;

import com.simplexservers.minecraft.commandutils.CommandManager;
import com.simplexservers.minecraft.commandutils.ParameterType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A CommandManager that handles native integration with the Bukkit API.
 *
 * @author Zach Abney
 * @see CommandManager
 */
public class BukkitCommandManager extends CommandManager {

	/**
	 * Handles registering the Bukkit related parameters for commands.
	 */
	static {
		ParameterType.registerParameterType(Player.class, Bukkit::getPlayerExact);
	}

	/**
	 * Constructs a new BukkitCommandManager that handles the commands for the given Bukkit plugin.
	 *
	 * @param plugin The Bukkit plugin the BukkitCommandManager handles commands for.
	 */
	public BukkitCommandManager(JavaPlugin plugin) {
		super(new BukkitCommandRegistrant(plugin));
	}

}
