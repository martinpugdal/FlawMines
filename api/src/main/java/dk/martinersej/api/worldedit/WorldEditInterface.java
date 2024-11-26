package dk.martinersej.api.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import dk.martinersej.api.FlawMinesInterface;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.util.List;

public abstract class WorldEditInterface {

    protected final FlawMinesInterface pluginInterface;

    public WorldEditInterface(FlawMinesInterface pluginInterface) {
        this.pluginInterface = pluginInterface;
    }

    public abstract WorldEditSelection getPlayerSelection(Player player);

    public abstract BukkitWorld getBukkitWorld(World world);

    public abstract AbstractPattern createBlockPattern(MaterialData materialData);

    public abstract CuboidRegion createCuboidRegion(World world, Vector min, Vector max);

    public abstract Polygonal2DRegion createPolygonalRegion(World world, List<Vector> regionPoints, int minY, int maxY);

    public abstract void closeEditSession(EditSession editSession);

    public abstract void setBlocks(EditSession editSession, Region region, RandomPattern pattern) throws MaxChangedBlocksException;

    public abstract void setBlocks(EditSession editSession, Region region, MaterialData... materialDatas) throws MaxChangedBlocksException;

    public abstract void setWalls(EditSession editSession, Region region, MaterialData materialData) throws MaxChangedBlocksException;

    public abstract Vector getMinimumPoint(Region region);

    public abstract Vector getMaximumPoint(Region region);
}
