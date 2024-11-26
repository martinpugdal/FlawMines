package dk.martinersej.handlers;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.martinersej.api.FlawMinesInterface;
import dk.martinersej.api.worldguard.WorldGuardInterface;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.*;

public class WorldGuard6 extends WorldGuardInterface {

    public WorldGuard6(FlawMinesInterface pluginInterface) {
        super(pluginInterface);
    }

    @Override
    public RegionManager getRegionManager(World world) {
        return pluginInterface.getWorldGuard().getRegionManager(world);
    }

    @Override
    public Set<ProtectedRegion> getApplicableRegionsSet(Location location) {
        Set<ProtectedRegion> result = new HashSet<>();
        com.sk89q.worldedit.Vector vector = new com.sk89q.worldedit.Vector(location.getX(), location.getY(), location.getZ());
        for(ProtectedRegion region : getRegionManager(location.getWorld()).getRegions().values()) {
            if(region.contains(vector)) {
                result.add(region);
            }
        }
        return result;
    }

    @Override
    public boolean canBuild(org.bukkit.entity.Player player, Location location) {
        return pluginInterface.getWorldGuard().canBuild(player, location);
    }

    @Override
    public Vector getMinimumPoint(ProtectedRegion region) {
        BlockVector min = region.getMinimumPoint();
        return new org.bukkit.util.Vector(min.getX(), min.getY(), min.getZ());
    }

    @Override
    public Vector getMaximumPoint(ProtectedRegion region) {
        BlockVector min = region.getMaximumPoint();
        return new org.bukkit.util.Vector(min.getX(), min.getY(), min.getZ());
    }

    @Override
    public List<Vector> getRegionPoints(ProtectedRegion region) {
        List<org.bukkit.util.Vector> result = new ArrayList<>();
        for (BlockVector2D point : region.getPoints()) {
            result.add(new org.bukkit.util.Vector(point.getX(), 0,point.getZ()));
        }
        return result;
    }

    @Override
    public ProtectedCuboidRegion createProtectedCuboidRegion(String name, Vector min, Vector max) {
        return new ProtectedCuboidRegion(
            name,
            new BlockVector(min.getBlockX(), min.getBlockY(), min.getBlockZ()),
            new BlockVector(max.getBlockX(), max.getBlockY(), max.getBlockZ())
        );
    }
}
