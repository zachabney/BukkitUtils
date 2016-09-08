package com.simplexservers.minecraft.bukkitutils.prompts;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.simplexservers.minecraft.commandutils.CommandInvoker;
import com.simplexservers.minecraft.promptutils.AnswerPrompt;
import com.simplexservers.minecraft.promptutils.ChatListener;

/**
 * The listener to handle players responding to AnswerPrompts.
 *
 * @author Zach Abney
 */
public class PromptListener implements Listener, ChatListener {

	/**
	 * The list of players answering prompts.
	 */
	private HashMap<Player, AnswerPrompt> conversing = new HashMap<>();

	@Override
	public void registerParticipant(CommandInvoker participant, AnswerPrompt prompt) {
		conversing.put((Player) participant.getNativeInvoker(), prompt);
	}

	/**
	 * Handles accepting the player's chat as an answer
	 * to an AnswerPrompt.
	 *
	 * @param event The player chat event.
	 */
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		AnswerPrompt prompt;
		if ((prompt = conversing.remove(event.getPlayer())) != null) {
			if (prompt.cancelInputChat()) {
				event.setCancelled(true);
			}

			String message = event.getMessage();
			if (message.equalsIgnoreCase("cancel")) {
				event.getPlayer().sendMessage(ChatColor.RED + "Canceled.");
			} else {
				prompt.onInput(message);
			}
		}
	}

	/**
	 * Handles ending the conversation with the player
	 * when they leave the server.
	 *
	 * @param event The player leave event.
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		conversing.remove(event.getPlayer());
	}

}
