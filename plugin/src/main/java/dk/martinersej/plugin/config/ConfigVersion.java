package dk.martinersej.plugin.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigVersion<T extends Configurable> {

    private final String fileName;
    private final Map<String, String> values = new HashMap<>();
    private FileConfiguration fileConfig;

    public ConfigVersion(String fileName) {
        this.fileName = fileName;
    }

    public void load(File dataFolder, T[] enumValues) {
        File file = new File(dataFolder, fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fileConfig = YamlConfiguration.loadConfiguration(file);
        boolean changed = loadConfigData(enumValues);
        if (changed) {
            save(file);
        }
    }

    private boolean loadConfigData(T[] enumValues) {
        boolean isChanged = false;
        for (T value : enumValues) {
            if (!fileConfig.contains(value.getKey())) {
                fileConfig.set(value.getKey(), value.getDefaultMessage());
                isChanged = true;
            }
            values.put(value.getKey(), fileConfig.getString(value.getKey()));
        }
        return isChanged;
    }

    public String get(T value) {
        return values.getOrDefault(value.getKey(), value.getDefaultMessage());
    }

    public void save(File file) {
        try {
            fileConfig.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

