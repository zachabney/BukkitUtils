package com.simplexservers.minecraft.bukkitutils.commands;

import com.simplexservers.minecraft.commandutils.CommandInvoker;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The Bukkit instance of a CommandInvoker in the form of a CommandSender.
 *
 * @author Zach Abney
 */
public class BukkitCommandInvoker extends CommandInvoker<CommandSender> {

	/**
	 * The Bukkit instance of the CommandSender that is being represented by the invoker.
	 */
	private final CommandSender sender;

	/**
	 * Creates a new BukkitCommandInvoker that represents the given Bukkit CommandSender.
	 *
	 * @param sender The Bukkit CommandSender that is being represented.
	 */
	public BukkitCommandInvoker(CommandSender sender) {
		super(sender);
		this.sender = sender;
	}

	/**
	 * Sends the given messages to the Bukkit CommandSender.
	 *
	 * @param message The message to send.
	 */
	@Override
	public void sendMessage(String message) {
		sender.sendMessage(message);
	}

	/**
	 * Checks if the Bukkit CommandSender has the given permission node.
	 *
	 * @param perm The permission node to check for.
	 * @return true if the CommandSender has the permission node, false otherwise.
	 */
	@Override
	public boolean hasPermission(String perm) {
		return sender.hasPermission(perm);
	}

	/**
	 * Checks if the Bukkit CommandSender is a Player instance.
	 *
	 * @return true if the CommandSender is an instance of a Bukkit Player.
	 */
	@Override
	public boolean isPlayer() {
		return sender instanceof Player;
	}

}
