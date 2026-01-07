package dk.martinersej.plugin.mine.environment.environments;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.environment.Environment;
import dk.martinersej.plugin.mine.environment.EnvironmentType;

import java.util.HashMap;
import java.util.Map;

public class ScheduledEnvironment extends Environment {

    private final int intervalSeconds; // The interval in seconds between each reset
    private long targetResetTime; // The absolute time in milliseconds when the next reset should occur
    private long lastRun; // The time in milliseconds since the last run

    // this is used for creating the environment, and the id will be placed by the database then queried
    public ScheduledEnvironment(Mine mine, int intervalSeconds) {
        this(0, mine, intervalSeconds, 0);
    }

    public ScheduledEnvironment(int id, Mine mine, int intervalSeconds, long timeSinceLastRun) {
        super(id, mine);
        this.intervalSeconds = intervalSeconds;
        if (intervalSeconds <= 0) {
            throw new IllegalArgumentException("The interval must be greater than 0");
        }

        // Set the last run time to the current time minus the time since last run
        this.lastRun = System.currentTimeMillis() - timeSinceLastRun;

        // Calculate the target reset time based on real-world time
        this.targetResetTime = System.currentTimeMillis() + (intervalSeconds * 1000L) - timeSinceLastRun;

        // Register with the centralized scheduler
        FlawMines.get().getResetScheduler().register(this);
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    @Override
    public double getProgress() {
        int timeLeft = getTimeLeft();
        return timeLeft <= 0 ? 100 : 100 - (timeLeft * 100.0 / intervalSeconds);
    }

    public int getTimeLeft() {
        return (int) ((targetResetTime - System.currentTimeMillis()) / 1000);
    }

    public long getTargetResetTime() {
        return targetResetTime;
    }

    @Override
    public void reset() {
        lastRun = System.currentTimeMillis();
        targetResetTime = lastRun + (intervalSeconds * 1000L);
    }

    @Override
    public String serialize() {
        // just the extra data we need to serialize
        Map<String, String> data = new HashMap<>();
        data.put("intervalSeconds", String.valueOf(intervalSeconds));
        long timeSinceLastRun = System.currentTimeMillis() - lastRun;
        data.put("timeSinceLastRun", String.valueOf(timeSinceLastRun));

        // Serialize the data map
        return data.entrySet().stream()
            .map(entry -> entry.getKey() + ":" + entry.getValue())
            .reduce((a, b) -> a + "," + b)
            .orElse("");
    }

    @Override
    public EnvironmentType getType() {
        return EnvironmentType.SCHEDULED;
    }

    public static ScheduledEnvironment deserialize(Mine mine, String data) {
        Map<String, String> map = new HashMap<>();
        for (String entry : data.split(",")) {
            String[] split = entry.split(":");
            map.put(split[0], split[1]);
        }

        int intervalSeconds = Integer.parseInt(map.get("intervalSeconds"));
        if (map.get("timeSinceLastRun") == null) {
            return new ScheduledEnvironment(mine, intervalSeconds);
        }
        long timeSinceLastRun = Long.parseLong(map.get("timeSinceLastRun"));
        return new ScheduledEnvironment(0, mine, intervalSeconds, timeSinceLastRun);
    }

    @Override
    public void kill() {
        FlawMines.get().getResetScheduler().unregister(this);
    }
}
