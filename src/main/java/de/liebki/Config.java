package de.liebki;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class Config<T> {

	private File file;
	private FileConfiguration fileConfig;

	public Config(String path, String fileName, Runnable callback, Plugin plugin) {
		if (!fileName.contains(".yml")) {
			fileName = fileName + ".yml";
		}
		file = new File(path, fileName);
		fileConfig = YamlConfiguration.loadConfiguration(file);

		if (!file.exists()) {
			fileConfig.options().copyDefaults(true);
			callback.run();
			try {
				fileConfig.save(file);
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}

	public Config(String path, String fileName, Plugin plugin) {
		if (!fileName.contains(".yml")) {
			fileName = fileName + ".yml";
		}
		file = new File(path, fileName);
		fileConfig = YamlConfiguration.loadConfiguration(file);

		if (!file.exists()) {
			fileConfig.options().copyDefaults(true);
			try {
				fileConfig.save(file);
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}

	public FileConfiguration getConfig() {
		return fileConfig;
	}

	public void saveConfig() {
		try {
			fileConfig.save(file);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	public Boolean check(String path) {
		if (fileConfig.get(path) == null) {
			return false;
		}
		return true;
	}

	public void set(String path, Object wert) {
		fileConfig.set(path, wert);
		saveConfig();
	}

	public T get(String path) {
		if (fileConfig.getString(path) == null) {
			return null;
		}

		return (T) fileConfig.get(path);
	}
}
