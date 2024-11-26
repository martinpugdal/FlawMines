package dk.martinersej.plugin.mine.environment.environments;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.environment.Environment;
import dk.martinersej.plugin.mine.environment.EnvironmentType;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class ScheduledEnvironment extends Environment {

    private final int intervalSeconds;
    private int taskId; // The task id of the scheduled task
    private long lastRun = 0;

    // this is used for creating the environment, and the id will be placed by the database then queried
    public ScheduledEnvironment(Mine mine, int intervalSeconds) {
        this(0, mine, intervalSeconds);
    }

    public ScheduledEnvironment(int id, Mine mine, int intervalSeconds) {
        super(id, mine);
        this.intervalSeconds = intervalSeconds;
        if (intervalSeconds <= 0) {
            throw new IllegalArgumentException("The interval must be greater than 0");
        }

        scheduleResetTask();
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    @Override
    public double getProgress() {
        int timeSinceLastRun = (int) (System.currentTimeMillis() - lastRun) / 1000; // in seconds
        return Math.min(timeSinceLastRun / intervalSeconds, 0) * 100; // Return the progress as percentage
    }

    public int getTimeLeft() {
        int timeSinceLastRun = (int) (System.currentTimeMillis() - lastRun) / 1000; // in seconds
        return intervalSeconds - timeSinceLastRun;
    }

    @Override
    public void reset() {
        super.reset();
        // Reset the environment
        Bukkit.getServer().getScheduler().cancelTask(taskId);
        scheduleResetTask();
    }

    private void scheduleResetTask() {
        lastRun = System.currentTimeMillis();
        taskId = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
            FlawMines.get(),
            mine::reset,
            intervalSeconds * 20L
        );
    }

    @Override
    public String serialize() {
        // just the extra data we need to serialize
        Map<String, String> data = new HashMap<>();
        data.put("intervalSeconds", String.valueOf(intervalSeconds));

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
        return new ScheduledEnvironment(0, mine, intervalSeconds);
    }

    @Override
    public void kill() {
        super.kill();
        Bukkit.getServer().getScheduler().cancelTask(taskId);
    }
}
