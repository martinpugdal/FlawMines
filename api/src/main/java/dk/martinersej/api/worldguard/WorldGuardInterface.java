package dk.martinersej.api.worldguard;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.martinersej.api.FlawMinesInterface;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Set;

public abstract class WorldGuardInterface {

    protected final FlawMinesInterface pluginInterface;

    public WorldGuardInterface(FlawMinesInterface pluginInterface) {
        this.pluginInterface = pluginInterface;
    }

    public abstract RegionManager getRegionManager(World world);

    public abstract Set<ProtectedRegion> getApplicableRegionsSet(Location location);

    public abstract boolean canBuild(Player player, Location location);

    public abstract org.bukkit.util.Vector getMinimumPoint(ProtectedRegion region);

    public abstract org.bukkit.util.Vector getMaximumPoint(ProtectedRegion region);

    public abstract List<Vector> getRegionPoints(ProtectedRegion region);

    public abstract ProtectedCuboidRegion createProtectedCuboidRegion(String name, org.bukkit.util.Vector min, org.bukkit.util.Vector max);

}
