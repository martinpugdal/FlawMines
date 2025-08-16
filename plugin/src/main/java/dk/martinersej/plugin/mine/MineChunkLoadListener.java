package dk.martinersej.plugin.mine;

import dk.martinersej.plugin.FlawMines;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.List;

public class MineChunkLoadListener implements Listener {
    private final MineManager mineManager;

    public MineChunkLoadListener(MineManager mineManager) {
        this.mineManager = mineManager;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.getWorld().equals(mineManager.getWorld())) return; // Not the same world, ignore

        List<Mine> minesToReset = mineManager.getMinesToReset();
        if (minesToReset.isEmpty()) {
            return; // No mines to reset
        }
        Iterator<Mine> iterator = minesToReset.iterator(); // Use an iterator to safely remove mines while iterating
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Mine mine : minesToReset) {
                    if (mine.isInChunk(event.getChunk())) {
                        mine.reset();
                        iterator.remove();
                    }
                }
            }
        }.runTaskAsynchronously(FlawMines.get());
    }
}

