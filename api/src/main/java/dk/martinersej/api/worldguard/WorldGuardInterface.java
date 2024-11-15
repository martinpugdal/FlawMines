package dk.martinersej.api.worldguard;

import com.sk89q.worldguard.protection.managers.RegionManager;
import dk.martinersej.api.FlawMinesInterface;
import org.bukkit.World;

public abstract class WorldGuardInterface {

    protected final FlawMinesInterface pluginInterface;

    public WorldGuardInterface(FlawMinesInterface pluginInterface) {
        this.pluginInterface = pluginInterface;
    }


    public abstract RegionManager getRegionManager(World world);
}
