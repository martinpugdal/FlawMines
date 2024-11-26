package dk.martinersej.handlers;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockState;
import dk.martinersej.api.FlawMinesInterface;
import dk.martinersej.api.worldedit.RandomPattern;
import dk.martinersej.api.worldedit.WorldEditInterface;
import dk.martinersej.api.worldedit.WorldEditSelection;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class WorldEdit7_0 extends WorldEditInterface {

    public WorldEdit7_0(FlawMinesInterface pluginInterface) {
        super(pluginInterface);
    }

    @Override
    public WorldEditSelection getPlayerSelection(Player player) {
        try {
            Region region = pluginInterface.getWorldEdit().getSession(player).getSelection(BukkitAdapter.adapt(player.getWorld()));
            return new WorldEditSelection(
                player.getWorld(),
                BukkitAdapter.adapt(player.getWorld(), region.getMinimumPoint()),
                BukkitAdapter.adapt(player.getWorld(), region.getMaximumPoint())
            );
        } catch (IncompleteRegionException e) {
            return null;
        }
    }

    @Override
    public BukkitWorld getBukkitWorld(World world) {
        return new BukkitWorld(world);
    }

    @Override
    public AbstractPattern createBlockPattern(MaterialData materialData) {
        BlockData blockData = materialData.getItemType().createBlockData();
        BlockState blockState = BukkitAdapter.adapt(blockData);
        return new BlockPattern(blockState);
    }

    @Override
    public CuboidRegion createCuboidRegion(World world, Vector min, Vector max) {
        BlockVector3 minVector = BlockVector3.at(min.getBlockX(), min.getBlockY(), min.getBlockZ());
        BlockVector3 maxVector = BlockVector3.at(max.getBlockX(), max.getBlockY(), max.getBlockZ());
        return new CuboidRegion(
            getBukkitWorld(world),
            minVector,
            maxVector
        );
    }

    @Override
    public Polygonal2DRegion createPolygonalRegion(World world, List<Vector> regionPoints, int minY, int maxY) {
        List<BlockVector2> blockVector2Ds = new ArrayList<>();
        for (Vector vector : regionPoints) {
            blockVector2Ds.add(BlockVector2.at(vector.getBlockX(), vector.getBlockZ()));
        }
        return new Polygonal2DRegion(
            getBukkitWorld(world),
            blockVector2Ds,
            minY,
            maxY
        );
    }

    @Override
    public void closeEditSession(EditSession editSession) {
        editSession.close();
    }

    @Override
    public void setBlocks(EditSession editSession, Region region, RandomPattern pattern) throws MaxChangedBlocksException {
        editSession.setBlocks(region, pattern);
    }

    @Override
    public void setBlocks(EditSession editSession, Region region, MaterialData... materialDatas) throws MaxChangedBlocksException {
        if (materialDatas.length == 0) {
            return;
        }
        if (materialDatas.length == 1) {
            BlockData blockData = materialDatas[0].getItemType().createBlockData();
            BlockState blockState = BukkitAdapter.adapt(blockData);
            BlockPattern blockPattern = new BlockPattern(blockState);
            editSession.setBlocks(region, blockPattern);
        } else {
            RandomPattern randomPattern = new RandomPattern();
            for (MaterialData materialData : materialDatas) {
                BlockData blockData = materialData.getItemType().createBlockData();
                BlockState blockState = BukkitAdapter.adapt(blockData);
                BlockPattern blockPattern = new BlockPattern(blockState);
                randomPattern.add(blockPattern, 1);
            }
            editSession.setBlocks(region, randomPattern);
        }
    }

    @Override
    public void setWalls(EditSession editSession, Region region, MaterialData materialData) throws MaxChangedBlocksException {
        BlockData blockData = materialData.getItemType().createBlockData();
        BlockState blockState = BukkitAdapter.adapt(blockData);
        BlockPattern blockPattern = new BlockPattern(blockState);
        editSession.makeWalls(region, blockPattern);
    }

    @Override
    public Vector getMinimumPoint(Region region) {
        return new Vector(region.getMinimumPoint().getX(), region.getMinimumPoint().getY(), region.getMinimumPoint().getZ());
    }

    @Override
    public Vector getMaximumPoint(Region region) {
        return new Vector(region.getMaximumPoint().getX(), region.getMaximumPoint().getY(), region.getMaximumPoint().getZ());
    }
}