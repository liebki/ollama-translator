package de.liebki;

import de.liebki.utils.*;
import io.github.amithkoujalgi.ollama4j.core.OllamaAPI;
import io.github.amithkoujalgi.ollama4j.core.models.OllamaResult;
import io.github.amithkoujalgi.ollama4j.core.utils.OptionsBuilder;
import io.github.amithkoujalgi.ollama4j.core.utils.PromptBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class OllamaTranslator extends JavaPlugin implements Listener, CommandExecutor {

    private final String PREFIX = "&f[&9OT&f]&r ";
    private final String TRANSLATOR_PROMPT = "Translate the user message you get from it's language to %TARGETLANGUAGE% without commenting or mentioning the source of translation. You can correct grammatical errors but dont alter the text too much and dont tell if you changed it. Avoid speaking with the user besides the translation, as everything is for someone else and not you, you focus on translating. Just translate the message, no comment, no code, no formatting just the translation.";

    private Config config;
    private OllamaAPI ollamaAPI;
    private PromptBuilder promptBuilder;

    private final Map<UUID, Long> playersOnCooldown = new HashMap<>();
    private long cooldown;

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String msgContent = event.getMessage();
        UUID playerId = player.getUniqueId();

        if (msgContent.length() < 5) {
            String rawAnswerString = (String) config.get("translation.broadcastmessage");
            rawAnswerString = rawAnswerString.replace("%TRANSLATION%", msgContent);

            rawAnswerString = rawAnswerString.replace("%PLAYER%", player.getDisplayName());
            Bukkit.broadcastMessage(MessageUtils.ColorConvert(rawAnswerString));

            event.setCancelled(true);
            return;
        }

        if ((Boolean) config.get("cooldown.enabled")) {
            if (hasCooldown(playerId)) {
                event.setCancelled(true);
                player.sendMessage(MessageUtils.ColorConvert(PREFIX + "&cPlease wait before chatting.."));
                return;
            }

            updateCooldown(playerId);
        }

        try {
            CompletableFuture.supplyAsync(() -> {
                try {
                    String prompt = (TRANSLATOR_PROMPT).replace("%TARGETLANGUAGE%",
                            (String) config.get("translation.targetlanguage"));

                    promptBuilder = new PromptBuilder().addLine(prompt).addLine("``````").addSeparator().add(msgContent);

                    String model = (String) config.get("ollama.modelname");
                    OllamaResult response = ollamaAPI.generate(model, promptBuilder.build(), new OptionsBuilder().build());

                    Triple<String, Player, String> playerResponseAndOriginal = new Triple<>(response.getResponse(), player, msgContent);
                    return playerResponseAndOriginal;
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
                String message = (String) config.get("translation.cancelmessage");
                player.sendMessage(MessageUtils.ColorConvert(PREFIX + message));
            }
        }
    }

    private Void handleError(Throwable throwable) {
        throwable.printStackTrace();
        return null;
    }

    private void handleResponse(Triple<String, Player, String> response) {
        if (response != null) {
            Player receiver = response.getTwo();
            if (receiver.isOnline()) {
                String translatedMessage = response.getOne();
                String originalMessage = response.getThree();
                
                TextComponent message = new TextComponent(MessageUtils.ColorConvert(receiver.getDisplayName() + " : "));
                TextComponent translatedText = new TextComponent(MessageUtils.ColorConvert("§b" + translatedMessage));
                translatedText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Original: " + originalMessage).create()));
                
                message.addExtra(translatedText);
                Bukkit.spigot().broadcast(message);
            }
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
        File configFile = new File("plugins/ollamatranslator/options.yml");

        if (!configFile.exists()) {
            config = new Config("plugins/ollamatranslator", "options.yml", this);

            if (!config.check("configexists")) {
                config.set("donottouch.configexists", true);

                config.set("ollama.secondstimeout", 20);
                config.set("ollama.modelname", "llama3:8b-instruct-q6_K");
                config.set("ollama.apiaddress", "http://localhost:11434/");

                config.set("cooldown.enabled", true);
                config.set("cooldown.miliseconds", 1000);
                config.set("cooldown.message", "&cPlease wait..");

                config.set("translation.canceloriginalmessage", true);
                config.set("translation.notify", true);
                config.set("translation.cancelmessage", "&7Your message is going to be translated, please wait..");

                config.set("translation.targetlanguage", "english");
                config.set("translation.broadcastmessage", "&r%PLAYER% &r: &r&b%TRANSLATION%");

                config.saveConfig();
            }
        } else {
            config = new Config("plugins/ollamatranslator", "options.yml", this);
        }

        ollamaAPI = new OllamaAPI((String) config.get("ollama.apiaddress"));
        ollamaAPI.setRequestTimeoutSeconds((Integer) config.get("ollama.secondstimeout"));

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().runTaskTimer(this, this::cleanExpiredCooldowns, 0, 20 * 5);

        cooldown = (Integer) config.get("cooldown.miliseconds");
        Bukkit.getConsoleSender().sendMessage("§4OT: ollama-translator powering on");

        this.getCommand("ollamatranslator").setExecutor(new InfoCommand());
        new Metrics(this, 24175);
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("§4OT: ollama-translator powering off");
    }

}