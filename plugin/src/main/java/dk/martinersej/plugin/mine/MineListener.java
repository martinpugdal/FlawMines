package dk.martinersej.plugin.mine;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.MineManager;
import dk.martinersej.plugin.mine.environment.Environment;
import dk.martinersej.plugin.mine.environment.environments.DestroyedEnvironment;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockExplodeEvent;

public class MineListener implements Listener {

    void checkEvent(BlockEvent event) {
        World world = event.getBlock().getWorld();
        MineManager mineManager = FlawMines.get().getMineManager(world);

        ApplicableRegionSet applicableRegions = FlawMines.get().getWorldGuardInterface().getRegionManager(world).getApplicableRegions(event.getBlock().getLocation());
        ProtectedRegion region = applicableRegions.getRegions().stream().findFirst().orElse(null);
        Mine mine = mineManager.getMine(region);

        if (mine != null) {
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
    public void onBlockDamage(BlockDamageEvent event) {
        checkEvent(event);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        checkEvent(event);
    }
}
