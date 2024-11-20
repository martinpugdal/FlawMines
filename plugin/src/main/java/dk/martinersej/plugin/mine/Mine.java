package dk.martinersej.plugin.mine;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.patterns.BlockChance;
import com.sk89q.worldedit.patterns.RandomFillPattern;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.events.MineResetEvent;
import dk.martinersej.plugin.mine.environment.Environment;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.List;

public class Mine {

    private final String name;
    private boolean fillmode;
    private BlockVector teleportLocation;
    private final List<MineBlock> blocks = new ArrayList<>();
    private final List<Environment> environments = new ArrayList<>();
    private final MineRegion mineRegion;

    public Mine(String name, ProtectedRegion region, World world, boolean fillmode, BlockVector teleportLocation) {
        this.name = name;
        this.mineRegion = new MineRegion(region, world);
        this.fillmode = fillmode;
        this.teleportLocation = teleportLocation;
    }

    public Mine(String name, ProtectedRegion region, World world, boolean fillmode) {
        this(name, region, world, fillmode, new BlockVector(region.getMaximumPoint().getBlockX(), region.getMaximumPoint().getBlockY(), region.getMaximumPoint().getBlockZ()));
    }

    public Mine(String name, ProtectedRegion region, World world) {
        this(name, region, world, false, new BlockVector(region.getMaximumPoint().getBlockX(), region.getMaximumPoint().getBlockY(), region.getMaximumPoint().getBlockZ()));
    }

    public List<Environment> getEnvironments() {
        return environments;
    }

    public MineRegion getRegion() {
        return mineRegion;
    }

    public World getWorld() {
        // get world from region
        return ((BukkitWorld) mineRegion.getRegion().getWorld()).getWorld();
    }

    public List<MineBlock> getBlocks() {
        return blocks;
    }

    public void reset() {
        //TODO: this should refactor because some methods is not available then we upgrade worldedit version, f.e.
        // RandomPattern is the new class name for RandomFillPattern.
        // Same for BlockChance.

        Runnable runnable = () -> {
            EditSessionFactory editSession = FlawMines.get().getWorldEdit().getWorldEdit().getEditSessionFactory();
            EditSession session = editSession.getEditSession(new BukkitWorld(getWorld()), getRegion().getVolume());
            // should work, unless we could change it to -1 for a hard fix. I hope it's not necessary.

            try {
                // get players inside the mine and teleport them to the teleport location
                List<Player> players = mineRegion.playersWithinRegion();
                Location teleportLocation = getTeleportLocationAsLocation();
                for (Player player : players) {
                    player.teleport(teleportLocation);
                }

                // add the blocks to the pattern
                List<BlockChance> blockChances = new ArrayList<>();
                if (blocks.isEmpty())
                    blockChances.add(new BlockChance(new BaseBlock(Material.AIR.getId()), 100));
                blocks.forEach((block -> {
                    BaseBlock baseBlock = new BaseBlock(block.getBlockData().getItemType().getId(), block.getBlockData().getData());
                    blockChances.add(new BlockChance(baseBlock, block.getPercentage()));
                }));

                // create the pattern
                RandomFillPattern pattern = new RandomFillPattern(blockChances);

                if (fillmode) {
                    // Create a mask with air blocks, so we can replace air blocks with the pattern.
                    BlockMask mask = new BlockMask(session, new BaseBlock(Material.AIR.getId()));
                    session.replaceBlocks(mineRegion.getRegion(), mask, pattern);
                } else {
                    session.setBlocks(mineRegion.getRegion(), pattern);
                }
            } catch (MaxChangedBlocksException e) {
                e.printStackTrace();
            } finally {
                session.flushQueue();
                resetFinished();
            }
        };

        if (!FlawMines.isLegacy()) {
            FlawMines.get().getServer().getScheduler().runTaskAsynchronously(FlawMines.get(), runnable);
        } else {
            runnable.run();
        }
    }

    public void addEnvironment(Environment environment) {
        environments.add(environment);
    }

    public long getTotalBlocks() {
        return mineRegion.getVolume();
    }

    private void resetFinished() {
        for (Environment environment : environments) {
            environment.reset();
        }

        Bukkit.getPluginManager().callEvent(new MineResetEvent(this));
    }

    public void addBlock(MineBlock block) {
        blocks.add(block);
    }

    public MineBlock getBlock(MaterialData materialData) {
        for (MineBlock block : blocks) {
            if (block.getBlockData().equals(materialData)) {
                return block;
            }
        }
        return null;
    }

    public void updateBlock(MineBlock block) {
        for (int i = 0; i < blocks.size(); i++) {
            if (blocks.get(i).getId() == block.getId()) {
                blocks.set(i, block);
                return;
            }
        }
    }

    public void removeBlock(MineBlock block) {
        blocks.remove(block);
    }

    public String getName() {
        return name;
    }

    public BlockVector getTeleportLocation() {
        return teleportLocation;
    }

    public Location getTeleportLocationAsLocation() {
        return teleportLocation.toLocation(getWorld());
    }

    public void setTeleportLocation(BlockVector teleportLocation) {
        this.teleportLocation = teleportLocation;
    }

    public boolean isFillmode() {
        return fillmode;
    }

    public void setFillmode(boolean fillmode) {
        this.fillmode = fillmode;
    }
}
