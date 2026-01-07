package dk.martinersej.plugin.mine;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.mine.environment.environments.ScheduledEnvironment;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ResetScheduler {

    private final Set<ScheduledEnvironment> environments = new HashSet<>();
    private BukkitTask task;

    public ResetScheduler(FlawMines plugin) {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                for (ScheduledEnvironment env : new ArrayList<>(environments)) {
                    if (env.getTargetResetTime() <= now) {
                        env.getMine().reset();
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Check every second (20 ticks)
    }

    public void register(ScheduledEnvironment env) {
        environments.add(env);
    }

    public void unregister(ScheduledEnvironment env) {
        environments.remove(env);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }
}
