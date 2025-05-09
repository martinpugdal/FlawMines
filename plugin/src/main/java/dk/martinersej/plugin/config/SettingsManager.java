package dk.martinersej.plugin.config;

import java.io.File;

public class SettingsManager {

    private static ConfigVersion<Messages> messagesConfigVersion;
    private static ConfigVersion<Configs> configsConfigVersion;

    public static void init(File dataFolder) {
        messagesConfigVersion = new ConfigVersion<>("messages.yml");
        configsConfigVersion = new ConfigVersion<>("config.yml");

        reload(dataFolder);
    }

    public static String get(Configurable item) {
        if (item instanceof Messages) {
            return messagesConfigVersion.get((Messages) item);
        } else if (item instanceof Configs) {
            return configsConfigVersion.get((Configs) item);
        }
        return item.getWithPlaceholders();
    }

    public static void reload(File dataFolder) {
        messagesConfigVersion.load(dataFolder, Messages.values());
        configsConfigVersion.load(dataFolder, Configs.values());
    }

    public static void save(File dataFolder) {
        messagesConfigVersion.save(new File(dataFolder, "messages.yml"));
        configsConfigVersion.save(new File(dataFolder, "config.yml"));
    }
}
