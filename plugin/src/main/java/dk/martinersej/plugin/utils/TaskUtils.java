package dk.martinersej.plugin.utils;

import dk.martinersej.plugin.FlawMines;
import org.bukkit.Bukkit;

public class TaskUtils {

    public static void runTaskAsync(Runnable runnable) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(FlawMines.get(), runnable);
    }

    public static void runTaskSync(Runnable runnable) {
        Bukkit.getServer().getScheduler().runTask(FlawMines.get(), runnable);
    }
}
