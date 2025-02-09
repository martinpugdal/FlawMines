package dk.martinersej.plugin.placeholderapi;

import org.bukkit.OfflinePlayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PAPIRequest {

    private final Pattern pattern;

    public PAPIRequest(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public boolean matches(String params) {
        return pattern.matcher(params).matches();
    }

    public String getRegexGroup(String params, int group) {
        Matcher matcher = pattern.matcher(params);
        if (matcher.find()) {
            return matcher.group(group);
        }
        return null;
    }


    public abstract String onRequest(OfflinePlayer player, String params);
}
