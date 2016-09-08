package com.simplexservers.minecraft.bukkitutils.commands;

import java.util.Arrays;

import com.simplexservers.minecraft.commandutils.CommandHandler;
import com.simplexservers.minecraft.commandutils.CommandMethod;
import com.simplexservers.minecraft.commandutils.CommandProperties;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Unit tests for commands.
 *
 * @author Zach Abney
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({PluginCommand.class})
public class CommandsTest {

	/**
	 * The PluginCommand being mocked.
	 */
	private PluginCommand pluginCommand;
	/**
	 * The CommandManager handling the commands.
	 */
	private BukkitCommandManager commandManager;

	/**
	 * Constructs a new CommandsTest.
	 */
	public CommandsTest() {
		pluginCommand = PowerMockito.mock(PluginCommand.class);

		JavaPlugin plugin = Mockito.mock(JavaPlugin.class);
		Mockito.when(plugin.getCommand(Matchers.anyString())).thenReturn(pluginCommand);

		commandManager = new BukkitCommandManager(plugin);
		commandManager.registerHandler(new TestCommandHandler());
	}

	/**
	 * Tests that the CommandManager registers the command with Bukkit.
	 */
	@Test
	public void testCommandRegistration() {
		Mockito.verify(pluginCommand, Mockito.atLeast(1)).setExecutor(Mockito.any());
	}

	/**
	 * Tests that the base command execution works.
	 */
	@Test
	public void testCommandExecution() {
		testCommand("test", "testCommand");
	}

	/**
	 * Tests that alias command execution works.
	 */
	@Test
	public void testCommandAliases() {
		testCommand("aliastest2", "testAliases");
	}

	/**
	 * Tests that permission validation works.
	 */
	@Test
	public void testPermission() {
		CommandSender sender = PowerMockito.mock(CommandSender.class);
		Mockito.when(sender.hasPermission(Matchers.anyString())).thenReturn(false);

		testCommand(sender, "permission", CommandMethod.NO_PERMISSION_MESSAGE);
	}

	/**
	 * Tests that player invocation requirement works.
	 */
	@Test
	public void testPlayerInvoker() {
		Player player = PowerMockito.mock(Player.class);
		testCommand(player, "player", "testPlayer");
	}

	/**
	 * Tests that grouped commands work.
	 */
	@Test
	public void testGroupCommand() {
		testCommand("test group", "testGroup");
	}

	/**
	 * Tests that argument validation works.
	 */
	@Test
	public void testArguments() {
		testCommand("arguments 5 3.14 5.12 off", "testArguments");
	}

	/**
	 * Tests that dynamic arguments work.
	 */
	@Test
	public void testDynamicArguments() {
		testCommand("dynamicarguments This is a test.", "testDynamicArguments 4");
	}

	/**
	 * Tests the given command and asserts the result.
	 *
	 * @param fullCommand The full command to test.
	 * @param assertResult The result to assert for.
	 */
	private void testCommand(String fullCommand, String assertResult) {
		CommandSender sender = PowerMockito.mock(CommandSender.class);
		testCommand(sender, fullCommand, assertResult);
	}

	/**
	 * Tests the given command executed by the CommandSender
	 * and asserts the result.
	 *
	 * @param sender The CommandSender executing the command.
	 * @param fullCommand The full command to test.
	 * @param assertResult The result to assert for.
	 */
	private void testCommand(CommandSender sender, String fullCommand, String assertResult) {
		String[] commandFragments = fullCommand.split(" ");
		String baseCommand = commandFragments[0];
		String[] commandArgs = Arrays.copyOfRange(commandFragments, 1, commandFragments.length);

		Command cmd = PowerMockito.mock(Command.class);
		Mockito.when(cmd.getName()).thenReturn(baseCommand);

		BukkitCommandInvoker invoker = new BukkitCommandInvoker(sender);
		commandManager.invokeCommand(invoker, baseCommand, commandArgs);
		Mockito.verify(sender).sendMessage(assertResult);
	}

	/**
	 * A test CommandHandler.
	 *
	 * @author Zach Abney
	 */
	public static class TestCommandHandler implements CommandHandler {

		/**
		 * A test base command.
		 *
		 * @param sender The command executor.
		 */
		@CommandProperties(
				command = "test",
				description = "Test command."
		)
		public void testCommand(CommandSender sender) {
			sender.sendMessage("testCommand");
		}

		/**
		 * A test alias command.
		 *
		 * @param sender The command executor.
		 */
		@CommandProperties(
				command = "aliastest",
				aliases = {"aliastest2"},
				description = "Tests command aliases."
		)
		public void testAliases(CommandSender sender) {
			sender.sendMessage("testAliases");
		}

		/**
		 * A test permission command.
		 *
		 * @param sender The command executor.
		 */
		@CommandProperties(
				command = "permission",
				description = "Tests a permission.",
				permission = "test"
		)
		public void testPermission(CommandSender sender) {
			sender.sendMessage("testPermission");
		}

		/**
		 * A test player command.
		 *
		 * @param player The command executor.
		 */
		@CommandProperties(
				command = "player",
				description = "Tests for a player invoker."
		)
		public void testPlayer(Player player) {
			player.sendMessage("testPlayer");
		}

		/**
		 * A test group command.
		 *
		 * @param sender The command executor.
		 */
		@CommandProperties(
				command = "test group",
				description = "Tests a group command."
		)
		public void testGroup(CommandSender sender) {
			sender.sendMessage("testGroup");
		}

		/**
		 * A test command with arguments.
		 *
		 * @param sender The command executor.
		 * @param i An integer argument.
		 * @param f A float argument.
		 * @param d A double argument.
		 * @param b A boolean argument.
		 */
		@CommandProperties(
				command = "arguments",
				description = "Tests a command with arguments."
		)
		public void testArguments(CommandSender sender, int i, float f, double d, boolean b) {
			sender.sendMessage("testArguments");
		}

		/**
		 * A test command with dynamic arguments.
		 *
		 * @param sender The command executor.
		 * @param args The array of arguments passed to the command.
		 */
		@CommandProperties(
				command = "dynamicarguments",
				description = "Tests a command with dynamic arguments."
		)
		public void testDynamicArguments(CommandSender sender, String[] args) {
			sender.sendMessage("testDynamicArguments " + args.length);
		}

	}

}
