package dk.martinersej.plugin.mine.environment;

import dk.martinersej.plugin.mine.Mine;

import java.util.HashMap;
import java.util.Map;

public abstract class Environment {

    private final int id; // its for database
    protected final Mine mine;
    protected final int priority;
    protected boolean finished = false;

    public Environment(int id, Mine mine, int priority) {
        this.id = id;
        this.mine = mine;
        this.priority = priority;
    }

    public void reset() {
        finished = false;
    }

    public abstract double getProgress();

    public abstract String serialize();

    public static Environment deserialize(String data) {
        // Deserialize the data
        Map<String, String> map = new HashMap<>();
        for (String entry : data.split(",")) {
            String[] split = entry.split(":");
            map.put(split[0], split[1]);
        }

        if (map.get("type").equals("destroyed")) {
            return DestroyedEnvironment.deserialize(data);
        }

        return null;
    }

    public int getId() {
        return id;
    }
}
