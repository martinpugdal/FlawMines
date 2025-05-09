package dk.martinersej.plugin.config;

public enum Configs implements Configurable {

    TIME_FORMAT("resettimer.timeleft.format.default", "HH:mm:ss"),
    TIME_FORMAT_WITHOUT_HOURS("resettimer.timeleft.format.without_hours", "mm:ss"),
    TIME_FORMAT_WITHOUT_MINUTES("resettimer.timeleft.format.without_minutes", "s");

    private final String key;
    private final String defaultMessage;

    Configs(String key, String defaultMessage) {
        this.key = key;
        this.defaultMessage = defaultMessage;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public boolean isTrue() {
        return Boolean.parseBoolean(get());
    }

    public String get() {
        return SettingsManager.get(this);
    }

    public String getWithPlaceholders(Object... placeholders) {
        return replacePlaceholders(SettingsManager.get(this), placeholders);
    }

    private String replacePlaceholders(String message, Object... placeholders) {
        for (int i = 0; i < placeholders.length; i++) {
            String value = String.valueOf(placeholders[i]);
            message = message.replace("{" + i + "}", value);
        }
        return message;
    }
}
