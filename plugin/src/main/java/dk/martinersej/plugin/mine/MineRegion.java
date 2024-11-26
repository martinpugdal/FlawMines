package dk.martinersej.plugin.mine;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.martinersej.api.worldedit.WorldEditInterface;
import dk.martinersej.api.worldguard.WorldGuardInterface;
import dk.martinersej.plugin.FlawMines;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MineRegion {

    private final Region region;
    private final ProtectedRegion protectedRegion;
    private int volume = -1;

    public MineRegion(ProtectedRegion protectedRegion, World world) {
        this.protectedRegion = protectedRegion;

        WorldEditInterface worldEditInterface = FlawMines.get().getWorldEditInterface();
        WorldGuardInterface worldGuardInterface = FlawMines.get().getWorldGuardInterface();
        switch (protectedRegion.getType()) {
            case CUBOID:
                Vector maxP = worldGuardInterface.getMaximumPoint(protectedRegion);
                Vector minP = worldGuardInterface.getMinimumPoint(protectedRegion);
                region = worldEditInterface.createCuboidRegion(
                    world,
                    minP,
                    maxP
                );
                break;
            case POLYGON:
                List<Vector> regionPoints = worldGuardInterface.getRegionPoints(protectedRegion);
                int yMax = worldGuardInterface.getMaximumPoint(protectedRegion).getBlockY();
                int yMin = worldGuardInterface.getMinimumPoint(protectedRegion).getBlockY();
                region = worldEditInterface.createPolygonalRegion(world, regionPoints, yMin, yMax);
                break;
            default:
                region = null;
        }

        if (region != null) {
            volume = region.getArea() * region.getHeight();
        }
    }

    public Region getRegion() {
        return region;
    }

    public String getId() {
        return protectedRegion.getId();
    }

    public ProtectedRegion getProtectedRegion() {
        return protectedRegion;
    }

    public int getVolume() {
        return volume;
    }

    public List<Player> playersWithinRegion() {
        World world = ((BukkitWorld) region.getWorld()).getWorld();
        List<Player> players = new ArrayList<>(world.getPlayers());
        return players.stream()
            .filter(player -> {
                Set<ProtectedRegion> regionSet = FlawMines.get().getWorldGuardInterface().getApplicableRegionsSet(player.getLocation());
                return regionSet.contains(protectedRegion);
            })
            .collect(Collectors.toList());
    }
}
