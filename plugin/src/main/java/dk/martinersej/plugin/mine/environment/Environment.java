package dk.martinersej.plugin.mine.environment;

import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.environment.environments.DestroyedEnvironment;
import dk.martinersej.plugin.mine.environment.environments.ScheduledEnvironment;

import java.util.HashMap;
import java.util.Map;

public abstract class Environment {

    private int id; // its for database
    protected Mine mine;

    public Environment(int id, Mine mine) {
        this.id = id;
        this.mine = mine;
    }

    public abstract void reset();

    // progress in 0-100 (percentage)
    public abstract double getProgress();

    public abstract String serialize();

    public abstract EnvironmentType getType();

    public static Environment deserialize(Mine mine, String type, String data) {
        // Try to parse type using EnvironmentType (supports aliases)
        EnvironmentType envType = EnvironmentType.fromString(type);

        // Fallback to class name matching for backwards compatibility
        if (envType == null) {
            if ("DestroyedEnvironment".equals(type)) {
                envType = EnvironmentType.DESTROYED;
            } else if ("ScheduledEnvironment".equals(type)) {
                envType = EnvironmentType.SCHEDULED;
            }
        }

        if (envType == null) {
            throw new IllegalArgumentException("Unknown environment type: " + type);
        }

        Environment environment;
        switch (envType) {
            case DESTROYED:
                environment = DestroyedEnvironment.deserialize(mine, data);
                break;
            case SCHEDULED:
                environment = ScheduledEnvironment.deserialize(mine, data);
                break;
            default:
                throw new IllegalArgumentException("Unknown environment type: " + type);
        }

        return environment;
    }

    public Mine getMine() {
        return mine;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public abstract void kill();
}
