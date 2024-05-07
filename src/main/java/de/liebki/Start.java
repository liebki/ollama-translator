package de.liebki;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.amithkoujalgi.ollama4j.core.OllamaAPI;
import io.github.amithkoujalgi.ollama4j.core.models.OllamaResult;
import io.github.amithkoujalgi.ollama4j.core.utils.OptionsBuilder;
import io.github.amithkoujalgi.ollama4j.core.utils.PromptBuilder;

public class Start extends JavaPlugin implements Listener {

	private final String TRANSLATOR_PROMPT = "Translate the original user message you get from any language to %TARGETLANGUAGE% without commenting or mentioning the source of translation. You can correct grammatical errors but dont alter the text too much and dont tell if you changed it. Avoid speaking with the user besides the translation, as everything is for someone else and not you, you focus on translating.";

	private Config config;
	private OllamaAPI ollamaAPI;
	private PromptBuilder promptBuilder;

	private Map<UUID, Long> playersOnCooldown = new HashMap<>();
	private long cooldown;

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String msgContent = event.getMessage();
		UUID playerId = player.getUniqueId();

		if ((Boolean) config.get("cooldown.enabled")) {
			if (hasCooldown(playerId)) {
				event.setCancelled(true);
				player.sendMessage("§cPlease wait..");
				return;
			}

			updateCooldown(playerId);
		}

		try {
			CompletableFuture.supplyAsync(() -> {
				try {

					String prompt = (TRANSLATOR_PROMPT).replace("%TARGETLANGUAGE%",
							(String) config.get("translation.targetlanguage"));

					promptBuilder = new PromptBuilder().addLine(prompt).addLine("``````").addSeparator()
							.add(msgContent);

					String model = (String) config.get("ollama.modelname");
					OllamaResult response = ollamaAPI.generate(model, promptBuilder.build(),
							new OptionsBuilder().build());

					Pair<String, Player> playerAndResponse = new Pair<String, Player>(response.getResponse(), player);
					return playerAndResponse;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}).thenAccept(this::handleResponse).exceptionally(this::handleError);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if ((Boolean) config.get("translation.canceloriginalmessage")) {
			event.setCancelled(true);

			if ((Boolean) config.get("translation.notify")) {
				player.sendMessage((String) config.get("translation.cancelmessage"));
			}
		}
	}

	private Void handleError(Throwable throwable) {
		throwable.printStackTrace();
		return null;
	}

	private void handleResponse(Pair<String, Player> response) {
		Pair<String, Player> playerAndResponse = response;

		Player receiver = playerAndResponse.getTwo();
		if (receiver.isOnline()) {
			String message = playerAndResponse.getOne();
			String rawAnswerString = (String) config.get("translation.broadcastmessage");

			rawAnswerString = rawAnswerString.replace("%TRANSLATION%", message);
			rawAnswerString = rawAnswerString.replace("%PLAYER%", receiver.getDisplayName());

			Bukkit.broadcastMessage(rawAnswerString);
		}

	}

	private boolean hasCooldown(UUID playerId) {
		return playersOnCooldown.containsKey(playerId);
	}

	private void updateCooldown(UUID playerId) {
		playersOnCooldown.put(playerId, System.currentTimeMillis() + cooldown);
	}

	private void cleanExpiredCooldowns() {
		long currentTime = System.currentTimeMillis();
		playersOnCooldown.entrySet().removeIf(entry -> entry.getValue() <= currentTime);
	}

	@Override
	public void onEnable() {
		config = new Config("plugins/ollamatranslator", "options.yml", this);

		if (!config.check("configexists")) {

			config.set("donottouch.configexists", true);

			config.set("ollama.secondstimeout", 20);
			config.set("ollama.modelname", "mistral:instruct");
			config.set("ollama.apiaddress", "http://localhost:11434/");

			config.set("cooldown.enabled", true);
			config.set("cooldown.miliseconds", 1000);
			config.set("cooldown.message", "§cPlease wait..");

			config.set("translation.canceloriginalmessage", true);
			config.set("translation.notify", true);
			config.set("translation.cancelmessage", "§7Your message is in translation, please wait..");

			config.set("translation.targetlanguage", "english");
			config.set("translation.broadcastmessage", "§r%PLAYER% §r: §r§b%TRANSLATION%");

			config.saveConfig();
		}

		ollamaAPI = new OllamaAPI((String) config.get("ollama.apiaddress"));
		ollamaAPI.setRequestTimeoutSeconds((Integer) config.get("ollama.secondstimeout"));

		getServer().getPluginManager().registerEvents(this, this);
		getServer().getScheduler().runTaskTimer(this, this::cleanExpiredCooldowns, 0, 20 * 60);

		cooldown = (Integer) config.get("cooldown.miliseconds");
		Bukkit.getConsoleSender().sendMessage("§4ollama-translator powering on");
	}

	@Override
	public void onDisable() {
		Bukkit.getConsoleSender().sendMessage("§4ollama-translator powering off");
	}

}