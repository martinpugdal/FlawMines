package dk.martinersej.plugin.placeholderapi;

import org.bukkit.OfflinePlayer;

import java.util.regex.Pattern;

public abstract class PAPIRequest {

    private final String identifier;
    private final Pattern pattern;

    public PAPIRequest(String identifier, String pattern) {
        this.identifier = identifier;
        this.pattern = Pattern.compile(pattern);
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean matches(String params) {
        return pattern.matcher(params).matches();
    }

    public abstract String onRequest(OfflinePlayer player, String params);
}
