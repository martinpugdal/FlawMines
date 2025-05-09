package dk.martinersej.plugin.config;

import org.bukkit.ChatColor;

public enum Messages implements Configurable {

    SECOND("second.singular", "sekund"),
    SECONDS("second.plural", "sekunder"),
    RESETTING("timeleft.resetting", "Resetter.."),
    TIME_FORMAT_FOR_SECONDS("timeleft.secondformat", "{0} {1}");

    private final String key;
    private final String defaultMessage;

    Messages(String key, String defaultMessage) {
        this.key = key;
        this.defaultMessage = defaultMessage;
    }

    public String getKey() {
        return key;
    }

    public String get() {
        return SettingsManager.get(this);
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public String getWithPlaceholders(Object... placeholders) {
        return replacePlaceholders(SettingsManager.get(this), placeholders);
    }

    private String replacePlaceholders(String message, Object... placeholders) {
        for (int i = 0; i < placeholders.length; i++) {
            String value = String.valueOf(placeholders[i]);
            value = ChatColor.translateAlternateColorCodes('&', value); // Farver
            message = message.replace("{" + i + "}", value);
        }
        return message;
    }
}
