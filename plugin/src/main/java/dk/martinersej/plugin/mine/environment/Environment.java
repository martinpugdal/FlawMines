package dk.martinersej.plugin.mine.environment;

import dk.martinersej.plugin.mine.Mine;

import java.util.HashMap;
import java.util.Map;

public abstract class Environment {

    private int id; // its for database
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

        Environment environment = null;
        if (map.get("type").equals("destroyed")) {
            environment = DestroyedEnvironment.deserialize(data);
        } else if (map.get("type").equals("time")) {
            environment = null;
        }

        return environment;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
