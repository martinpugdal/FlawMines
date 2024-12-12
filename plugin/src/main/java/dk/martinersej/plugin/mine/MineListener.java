package dk.martinersej.plugin.mine;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.MineManager;
import dk.martinersej.plugin.mine.environment.Environment;
import dk.martinersej.plugin.mine.environment.environments.DestroyedEnvironment;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFormEvent;

import java.util.HashSet;
import java.util.Set;

public class MineListener implements Listener {

    void checkEvent(BlockEvent event) {
        World world = event.getBlock().getWorld();
        MineManager mineManager = FlawMines.get().getMineManager(world);

        Set<Mine> mines = new HashSet<>();
        for (Mine mine : mineManager.getMines()) {
            if (FlawMines.get().getWorldGuardInterface().regionContains(mine.getRegion().getProtectedRegion(), event.getBlock().getLocation())) {
                mines.add(mine);
            }
        }

        if (mines.isEmpty()) {
            return;
        }

        for (Mine mine : mines) {
            for (Environment environment : mine.getEnvironments()) {
                if (environment instanceof DestroyedEnvironment) {
                    DestroyedEnvironment destroyedEnvironment = (DestroyedEnvironment) environment;
                    destroyedEnvironment.increaseBlocksDestroyed();
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        checkEvent(event);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        checkEvent(event);
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        if (event.getNewState().getType().isAir()) {
            checkEvent(event);
        }
    }
}
