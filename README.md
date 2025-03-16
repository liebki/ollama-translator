# Ollama Translator Plugin

## Description
This Spigot Minecraft plugin, built with the currently newest Minecraft version 1.20.6, uses Ollama and Ollama4j to break language barriers on your Minecraft server. Whether players speak English, Spanish, or any other language, they can interact effortlessly, creating a better community and environment.


## Java version
From now on with minecraft 1.20.6 minecraft/spigot etc. use Java 21!


## Features
- "Real-time" translation of all player messages (all messages longer than five characters).
- All translated messages show the original message when hovering over them.
- Configurable settings.
- Easy integration with Ollama API.
- (ONLY) Support for local hosting of translation models (for now).
- Quick setup and minimal configuration.


## Setup
To set up the Ollama Translator plugin, follow these steps:

1. **Install Ollama**: Ollama is required for translation functionality. Visit [Ollama GitHub page](https://github.com/ollama/ollama?tab=readme-ov-file#ollama) to download and install Ollama on your server.

2. **Pull a Model**: After installing Ollama, pull a translation model such as Mistral or LLAMA2/3. Make sure to follow the instructions provided with Ollama to download and configure the desired model.

3. **Start Ollama Application**: Ensure that the Ollama application is running on your server. Check that the API is reachable under the address specified in the `options.yml` file configured for the plugin.

4. **Configure Plugin**: Configure the `options.yml` file for the Ollama Translator plugin according to your preferences and server setup.

5. **Restart/Reload Server**: Restart or reload your Spigot Minecraft server to apply the changes.

6. **Start Translating**: Once the setup is complete, the plugin will automatically translate player messages as per the configured settings.


## Plugin Configuration

Before deploying the plugin, ensure you configure the following parameters in the `options.yml` file:

```yaml
donottouch:
  configexists: true

ollama:
  secondstimeout: 20
  modelname: llama3:8b-instruct-q6_K
  apiaddress: http://localhost:11434/

cooldown:
  enabled: true
  milliseconds: 1000
  message: §cPlease wait...

translation:
  canceloriginalmessage: true
  notify: true
  cancelmessage: §7Your message is being translated, please wait...
  targetlanguage: english
  broadcastmessage: '§r%PLAYER% §r: §r§b%TRANSLATION%'
```

## Usage
0. Start ollama and download the model of choice
1. Install the plugin in your Spigot Minecraft server's plugins directory.
2. Configure the `options.yml` file according to your preferences.
3. Restart/Reload the server to apply the changes.
4. Players' messages will now be automatically translated as per the configured settings.


## Note
- ALL messages (longer than five characters) are translated, also native ones, so this plugin is really only for servers with a mixed-language player-base.
- The bigger the model the better the outcome, mistral showed to be very good but sometimes it is acting weird, llama3:8b-instruct-q6_K was very good but still not perfect.
- Please note that LLM/SLM require (a significant amount of) memory, with a minimum of 5-8 GB for small and 15-30 GB for middle-sized models or even more.
- You don't need a 30gb (file size) model if e.g. llama3:8b-instruct-q6_K produces a good outcome then it is alright, I tested mistral and llama3 so test it yourself.

## Todo
- Implement structured outputs for better translations!

## Disclaimer
This plugin is provided as is without any warranty. The developer holds no responsibility for any issues or damages arising from its usage.

#### Enjoy breaking down language barriers and creating a more inclusive gaming environment with the Spigot Minecraft Ollama Translator plugin!
