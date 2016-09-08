package com.simplexservers.minecraft.bukkitutils.commands;

import com.simplexservers.minecraft.commandutils.CommandManager;
import com.simplexservers.minecraft.commandutils.CommandRegistrantAdapter;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;

/**
 * Handler that's responsible for registering Bukkit commands with the CommandManager.
 *
 * @author Zach Abney
 */
public class BukkitCommandRegistrant extends CommandRegistrantAdapter {

	/**
	 * The Bukkit plugin commands are registered under.
	 */
	private final JavaPlugin plugin;
	/**
	 * The list of base commands we've already nagged about not being registered in the plugin.yml.
	 */
	private HashSet<String> naggedUnregisteredCommands = new HashSet<>();

	/**
	 * Creates a new BukkitCommandRegistrant that registers Bukkit commands for the given plugin.
	 *
	 * @param plugin The Bukkit plugin commands are registered under.
	 */
	public BukkitCommandRegistrant(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Handles setting the Bukkit PluginCommand to invoke the CommandManager.
	 *
	 * {@inheritDoc}
	 */
	@Override
	public void baseCommandRegistered(CommandManager manager, String baseCommand) {
		PluginCommand bukkitCommand = plugin.getCommand(baseCommand);
		if (bukkitCommand != null) {
			bukkitCommand.setExecutor((sender, cmd, aliasUsed, args) -> {
				BukkitCommandInvoker invoker = new BukkitCommandInvoker(sender);
				manager.invokeCommand(invoker, cmd.getName(), args);
				return true;
			});
		} else if (naggedUnregisteredCommands.add(baseCommand)) {
			Bukkit.getLogger().warning("Could not register base command '" + baseCommand + "'. Is it registered in plugin.yml?");
		}
	}

}
