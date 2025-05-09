package dk.martinersej.plugin.config;

public interface Configurable {

    String getKey();

    String getDefaultMessage();

    String get();

    String getWithPlaceholders(Object... placeholders);

}
