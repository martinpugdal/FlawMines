package dk.martinersej.plugin.mine;

import dk.martinersej.plugin.FlawMines;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

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
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Mine mine : minesToReset) {
                    if (mine.isInChunk(event.getChunk()) && mine.areAllChunksLoaded()) {
                        mine.reset();
                        minesToReset.remove(mine);
                    }
                }
            }
        }.runTask(FlawMines.get());
    }
}

