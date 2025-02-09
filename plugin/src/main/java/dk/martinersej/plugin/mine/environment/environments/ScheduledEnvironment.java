package dk.martinersej.plugin.mine.environment.environments;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.environment.Environment;
import dk.martinersej.plugin.mine.environment.EnvironmentType;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class ScheduledEnvironment extends Environment {

    private final int intervalSeconds; // The interval in seconds between each reset
    private int taskId = -1; // The task id of the scheduled task
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

        scheduleResetTask();
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
        int timeSinceLastRun = (int) (System.currentTimeMillis() - lastRun) / 1000; // in seconds
        return intervalSeconds - timeSinceLastRun;
    }

    @Override
    public void reset() {
        lastRun = System.currentTimeMillis();
        if (taskId != -1) { // if the task is still running, cancel it
            Bukkit.getServer().getScheduler().cancelTask(taskId);
        }
        scheduleResetTask();
    }

    private void scheduleResetTask() {
        int delay = intervalSeconds * 20;
        if (lastRun != 0) {
            long timeSinceLastRun = System.currentTimeMillis() - lastRun;
            delay = (int) (intervalSeconds * 20 - timeSinceLastRun / 50);
        }
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                mine.reset();
            }
        }.runTaskLater(FlawMines.get(), delay);
        taskId = task.getTaskId();
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
        Bukkit.getServer().getScheduler().cancelTask(taskId);
    }
}
