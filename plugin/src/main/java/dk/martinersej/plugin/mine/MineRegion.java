package dk.martinersej.plugin.mine;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class MineRegion {

    private final Region region;
    private final ProtectedRegion protectedRegion;

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

    public ProtectedRegion getProtectedRegion() {
        return protectedRegion;
    }

    private int volume = -1;
    public int getVolume() {
        return volume;
    }
}
