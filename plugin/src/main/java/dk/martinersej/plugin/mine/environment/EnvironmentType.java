package dk.martinersej.plugin.mine.environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum EnvironmentType {

    DESTROYED(new ArrayList<String>() {{
        add("destroy");
    }}, Float.class),
    SCHEDULED(new ArrayList<String>() {{
        add("timer");
        add("time");
        add("schedule");
    }}, Integer.class);

    private final List<String> aliases;
    private final List<Class<?>> values;

    EnvironmentType(List<String> aliases, Class<?>... values) {
        this.aliases = aliases;
        this.values = Arrays.asList(values);
    }

    public static EnvironmentType fromString(String string) {
        try {
            return EnvironmentType.valueOf(string.toUpperCase());
        } catch (IllegalArgumentException e) {
            for (EnvironmentType type : EnvironmentType.values()) {
                if (type.aliases.contains(string.toLowerCase())) {
                    return type;
                }
            }
        }
        return null;
    }

    public List<Class<?>> getValues() {
        return values;
    }
}
