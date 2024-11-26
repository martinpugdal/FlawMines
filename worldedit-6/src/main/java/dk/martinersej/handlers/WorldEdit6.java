package dk.martinersej.handlers;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.patterns.BlockChance;
import com.sk89q.worldedit.patterns.RandomFillPattern;
import com.sk89q.worldedit.patterns.SingleBlockPattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import dk.martinersej.api.FlawMinesInterface;
import dk.martinersej.api.worldedit.RandomPattern;
import dk.martinersej.api.worldedit.WorldEditInterface;
import dk.martinersej.api.worldedit.WorldEditSelection;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorldEdit6 extends WorldEditInterface {

    public WorldEdit6(FlawMinesInterface pluginInterface) {
        super(pluginInterface);
    }

    @Override
    public WorldEditSelection getPlayerSelection(Player player) {
        Selection selection = pluginInterface.getWorldEdit().getSelection(player);
        if (selection == null) {
            return null;
        }
        return new WorldEditSelection(
            player.getWorld(),
            selection.getMinimumPoint(),
            selection.getMaximumPoint()
        );
    }

    @Override
    public BukkitWorld getBukkitWorld(World world) {
        return new BukkitWorld(world);
    }

    @Override
    public AbstractPattern createBlockPattern(MaterialData materialData) {
        return new BlockPattern(new BaseBlock(materialData.getItemType().getId(), materialData.getData()));
    }

    @Override
    public CuboidRegion createCuboidRegion(World world, Vector min, Vector max) {
        com.sk89q.worldedit.Vector minVector = new com.sk89q.worldedit.Vector(min.getBlockX(), min.getBlockY(), min.getBlockZ());
        com.sk89q.worldedit.Vector maxVector = new com.sk89q.worldedit.Vector(max.getBlockX(), max.getBlockY(), max.getBlockZ());
        return new CuboidRegion(
            getBukkitWorld(world),
            minVector,
            maxVector
        );
    }

    @Override
    public Polygonal2DRegion createPolygonalRegion(World world, List<Vector> regionPoints, int minY, int maxY) {
        List<BlockVector2D> blockVector2Ds = new ArrayList<>();
        for (Vector vector : regionPoints) {
            blockVector2Ds.add(new BlockVector2D(vector.getBlockX(), vector.getBlockZ()));
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
        editSession.flushQueue();
    }

    @Override
    public void setBlocks(EditSession editSession, com.sk89q.worldedit.regions.Region region, RandomPattern pattern) {
        try {
            Map<Pattern, Double> blockChance = pattern.getPatternChances();

            List<BlockChance> blockChances = new ArrayList<>();
            for (Map.Entry<Pattern, Double> entry : blockChance.entrySet()) {
                blockChances.add(new BlockChance(entry.getKey().apply(null), entry.getValue()));
            }
            RandomFillPattern randomFillPattern = new RandomFillPattern(blockChances);

            editSession.setBlocks(region, randomFillPattern);
        } catch (com.sk89q.worldedit.MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setBlocks(EditSession editSession, com.sk89q.worldedit.regions.Region region, MaterialData... materialDatas) {
        try {
            if (materialDatas.length == 0) {
                return;
            }
            if (materialDatas.length == 1) {
                SingleBlockPattern blockPattern = new SingleBlockPattern(new BaseBlock(materialDatas[0].getItemType().getId(), materialDatas[0].getData()));
                editSession.setBlocks(region, blockPattern);
            } else {
                List<BlockChance> blockChances = new ArrayList<>();
                for (MaterialData materialData : materialDatas) {
                    blockChances.add(new BlockChance(new BaseBlock(materialData.getItemType().getId(), materialData.getData()), 1.0));
                }
                RandomFillPattern randomFillPattern = new RandomFillPattern(blockChances);
                editSession.setBlocks(region, randomFillPattern);
            }
        } catch (com.sk89q.worldedit.MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setWalls(EditSession editSession, Region region, MaterialData materialData) {
        try {

            SingleBlockPattern blockPattern = new SingleBlockPattern(new BaseBlock(materialData.getItemType().getId(), materialData.getData()));

            editSession.makeWalls(region, blockPattern);
        } catch (com.sk89q.worldedit.MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Vector getMinimumPoint(Region region) {
        return new Vector(region.getMinimumPoint().getBlockX(), region.getMinimumPoint().getBlockY(), region.getMinimumPoint().getBlockZ());
    }

    @Override
    public Vector getMaximumPoint(Region region) {
        return new Vector(region.getMaximumPoint().getBlockX(), region.getMaximumPoint().getBlockY(), region.getMaximumPoint().getBlockZ());
    }
}