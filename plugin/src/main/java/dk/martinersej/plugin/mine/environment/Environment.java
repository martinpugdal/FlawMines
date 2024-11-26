package dk.martinersej.plugin.mine.environment;

import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.environment.environments.DestroyedEnvironment;
import dk.martinersej.plugin.mine.environment.environments.ScheduledEnvironment;

import java.util.HashMap;
import java.util.Map;

public abstract class Environment {

    private int id; // its for database
    protected Mine mine;
    protected boolean finished = false;

    public Environment(int id, Mine mine) {
        this.id = id;
        this.mine = mine;
    }

    public void reset() {
        finished = false;
    }

    // progress in 0-100 (percentage)
    public abstract double getProgress();

    public abstract String serialize();

    public abstract EnvironmentType getType();

    public static Environment deserialize(Mine mine, String type, String data) {
        // Deserialize the data
        Map<String, String> map = new HashMap<>();
        for (String entry : data.split(",")) {
            String[] split = entry.split(":");
            if (split.length == 2) {
                map.put(split[0], split[1]);
            }
        }

        Environment environment;
        switch (type) {
            case "DestroyedEnvironment":
                environment = DestroyedEnvironment.deserialize(mine, data);
                break;
            case "ScheduledEnvironment":
                environment = ScheduledEnvironment.deserialize(mine, data);
                break;
            default:
                throw new IllegalArgumentException("Unknown environment type: " + type);
        }

        return environment;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void kill() {
        finished = true;
    }
}
