package dk.martinersej.handlers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dk.martinersej.api.FlawMinesInterface;
import dk.martinersej.api.worldguard.WorldGuardInterface;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorldGuard7 extends WorldGuardInterface {

    public WorldGuard7(FlawMinesInterface pluginInterface) {
        super(pluginInterface);
    }

    @Override
    public RegionManager getRegionManager(World world) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
    }

    @Override
    public Set<ProtectedRegion> getApplicableRegionsSet(Location location) {
        Set<ProtectedRegion> result = new HashSet<>();
        for (ProtectedRegion region : getRegionManager(location.getWorld()).getRegions().values()) {
            if (regionContains(region, location)) {
                result.add(region);
            }
        }
        return result;
    }

    @Override
    public boolean regionContains(ProtectedRegion region, Location location) {
        BlockVector3 vector = BlockVector3.at(location.getX(), location.getY(), location.getZ());
        return region.contains(vector);
    }

    @Override
    public boolean canBuild(org.bukkit.entity.Player player, Location location) {
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(location.getWorld());
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        if (!WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, world)) {
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(location);
            return query.testState(loc, localPlayer, Flags.BUILD);
        }
        return true;
    }

    @Override
    public Vector getMinimumPoint(ProtectedRegion region) {
        BlockVector3 min = region.getMinimumPoint();
        return new Vector(min.getX(), min.getY(), min.getZ());
    }

    @Override
    public Vector getMaximumPoint(ProtectedRegion region) {
        BlockVector3 min = region.getMaximumPoint();
        return new Vector(min.getX(), min.getY(), min.getZ());
    }

    @Override
    public List<Vector> getRegionPoints(ProtectedRegion region) {
        List<Vector> result = new ArrayList<>();
        for (BlockVector2 point : region.getPoints()) {
            result.add(new Vector(point.getX(), 0, point.getZ()));
        }
        return result;
    }

    @Override
    public ProtectedCuboidRegion createProtectedCuboidRegion(String name, Vector min, Vector max) {
        return new ProtectedCuboidRegion(name, BlockVector3.at(min.getBlockX(), min.getBlockY(), min.getBlockZ()), BlockVector3.at(max.getBlockX(), max.getBlockY(), max.getBlockZ()));
    }
}