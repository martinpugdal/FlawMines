package dk.martinersej.plugin.mine;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.martinersej.plugin.FlawMines;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MineRegion {

    private final Region region;
    private final ProtectedRegion protectedRegion;
    private int volume = -1;

    public MineRegion(ProtectedRegion protectedRegion, World world) {
        this.protectedRegion = protectedRegion;

        // get the region from worldedit, so we can use it for reset of the mine or similar
        BukkitWorld bukkitWorld = new BukkitWorld(world);
        switch (protectedRegion.getType()) {
            case CUBOID:
                region = new CuboidRegion(bukkitWorld, protectedRegion.getMaximumPoint(), protectedRegion.getMinimumPoint());
                break;
            case POLYGON:
                List<BlockVector2D> points = new ArrayList<>();
                for (BlockVector2D blockVector : protectedRegion.getPoints()) {
                    points.add(new BlockVector2D(blockVector.getX(), blockVector.getZ()));
                }
                int yDiff = protectedRegion.getMaximumPoint().getBlockY() - protectedRegion.getMinimumPoint().getBlockY();
                region = new Polygonal2DRegion(bukkitWorld, points, 0, yDiff); // idk about the minY and minY work
                break;
            default:
                region = null;
        }

        // calculate the volume of the region
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
        // get all players within a region
        World world = ((BukkitWorld) region.getWorld()).getWorld();
        List<Player> players = new ArrayList<>(world.getPlayers());
        RegionManager regionManager = FlawMines.get().getWorldGuardInterface().getRegionManager(world);
        return players.stream()
            .filter(player -> {
                ApplicableRegionSet regionSet = regionManager.getApplicableRegions(player.getLocation());
                for (ProtectedRegion region : regionSet) {
                    if (protectedRegion.equals(region)) return true;
                }
                return false;
            })
            .collect(Collectors.toList());
    }
}
