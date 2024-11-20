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

    public static Environment deserialize(Mine mine, String data) {
        // Deserialize the data
        Map<String, String> map = new HashMap<>();
        for (String entry : data.split(",")) {
            String[] split = entry.split(":");
            map.put(split[0], split[1]);
        }

        Environment environment;
        int id = Integer.parseInt(map.get("id"));
        if (map.get("type").equals("destroyed")) {
            environment = DestroyedEnvironment.deserialize(mine, data);
        } else if (map.get("type").equals("scheduled")) {
            environment = ScheduledEnvironment.deserialize(mine, data);
        } else {
            throw new IllegalArgumentException("Unknown environment type: " + map.get("type"));
        }
        environment.setId(id);

        return environment;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
