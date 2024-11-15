package dk.martinersej.handlers;

import com.sk89q.worldguard.protection.managers.RegionManager;
import dk.martinersej.api.FlawMinesInterface;
import dk.martinersej.api.worldguard.WorldGuardInterface;
import org.bukkit.World;

public class WorldGuard6 extends WorldGuardInterface {

    public WorldGuard6(FlawMinesInterface pluginInterface) {
        super(pluginInterface);
    }

    @Override
    public RegionManager getRegionManager(World world) {
        return pluginInterface.getWorldGuard().getRegionManager(world);
    }
}
