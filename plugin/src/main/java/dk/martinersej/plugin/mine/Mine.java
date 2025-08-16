package dk.martinersej.plugin.mine;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.martinersej.api.worldedit.RandomPattern;
import dk.martinersej.api.worldedit.WorldEditInterface;
import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.events.MineResetEvent;
import dk.martinersej.plugin.mine.environment.Environment;
import dk.martinersej.plugin.utils.TaskUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.List;

public class Mine {

    private final String name;
    private final List<MineBlock> blocks = new ArrayList<>();
    private final List<Environment> environments = new ArrayList<>();
    private final MineRegion mineRegion;
    private boolean fillmode;
    private BlockVector teleportLocation;

    public Mine(String name, ProtectedRegion region, World world, boolean fillmode, BlockVector teleportLocation) {
        this.name = name;
        this.mineRegion = new MineRegion(region, world);
        this.fillmode = fillmode;
        this.teleportLocation = teleportLocation;
    }

    public Mine(String name, ProtectedRegion region, World world, boolean fillmode) {
        this(name, region, world, fillmode, new BlockVector(FlawMines.get().getWorldGuardInterface().getMinimumPoint(region).getBlockX(), FlawMines.get().getWorldGuardInterface().getMinimumPoint(region).getBlockY(), FlawMines.get().getWorldGuardInterface().getMinimumPoint(region).getBlockZ()));
    }

    public Mine(String name, ProtectedRegion region, World world) {
        this(name, region, world, false, new BlockVector(FlawMines.get().getWorldGuardInterface().getMinimumPoint(region).getBlockX(), FlawMines.get().getWorldGuardInterface().getMinimumPoint(region).getBlockY(), FlawMines.get().getWorldGuardInterface().getMinimumPoint(region).getBlockZ()));
    }

    public List<Environment> getEnvironments() {
        return environments;
    }

    public MineRegion getRegion() {
        return mineRegion;
    }

    public World getWorld() {
        return ((BukkitWorld) mineRegion.getRegion().getWorld()).getWorld();
    }

    public List<MineBlock> getBlocks() {
        return blocks;
    }

    public void reset() {
        // if the region is not loaded, we should not attempt to reset it
        if (!getWorld().isChunkLoaded(getRegion().getRegion().getMinimumPoint().getBlockX() >> 4, getRegion().getRegion().getMinimumPoint().getBlockZ() >> 4) ||
            !getWorld().isChunkLoaded(getRegion().getRegion().getMaximumPoint().getBlockX() >> 4, getRegion().getRegion().getMaximumPoint().getBlockZ() >> 4)) {
            if (!FlawMines.get().isUnloadedEdits()) {
                // add the mine to the queue to be reset later
                FlawMines.get().getMineManager(getWorld()).addMineToResetQueue(this);
                return;
            }
        }
        EditSessionFactory editSession = FlawMines.get().getWorldEdit().getWorldEdit().getEditSessionFactory();
        EditSession session = editSession.getEditSession(new BukkitWorld(getWorld()), getRegion().getVolume());
        try {
            List<Player> players = mineRegion.playersWithinRegion();
            Location teleportLocation = getTeleportLocationAsLocation();
            for (Player player : players) {
                Bukkit.getScheduler().runTask(FlawMines.get(), () -> player.teleport(teleportLocation));
            }

            WorldEditInterface worldEditInterface = FlawMines.get().getWorldEditInterface();
            RandomPattern randomPattern = new RandomPattern();

            blocks.forEach((block -> {
                AbstractPattern blockPattern = worldEditInterface.createBlockPattern(block.getBlockData());
                randomPattern.add(blockPattern, block.getWeight());
            }));

            if (blocks.isEmpty()) {
                AbstractPattern airPattern = worldEditInterface.createBlockPattern(new MaterialData(Material.AIR));
                randomPattern.add(airPattern, 100);
            }

            if (fillmode) {
                BlockMask mask = worldEditInterface.createBlockMask(session, new MaterialData(Material.AIR));
                session.replaceBlocks(mineRegion.getRegion(), mask, randomPattern);
            } else {
                worldEditInterface.setBlocks(session, mineRegion.getRegion(), randomPattern);
            }
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        } finally {
            resetFinished();
            try {
                FlawMines.get().getWorldEditInterface().closeEditSession(session);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    public void addEnvironment(Environment environment) {
        environments.add(environment);
    }

    public void removeEnvironment(Environment environment) {
        environments.remove(environment);
        environment.kill();
    }

    public int getTotalBlocks() {
        return mineRegion.getVolume();
    }

    private void resetFinished() {
        for (Environment environment : environments) {
            environment.reset();
        }

        TaskUtils.runTaskSync(() -> Bukkit.getPluginManager().callEvent(new MineResetEvent(this)));
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

    public void setBlocks(List<MineBlock> blocks) {
        this.blocks.clear();
        this.blocks.addAll(blocks);
    }

    public void clearBlocks() {
        blocks.clear();
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

    public void setTeleportLocation(BlockVector teleportLocation) {
        this.teleportLocation = teleportLocation;
    }

    public Location getTeleportLocationAsLocation() {
        return teleportLocation.toLocation(getWorld());
    }

    public boolean isFillmode() {
        return fillmode;
    }

    public void setFillmode(boolean fillmode) {
        this.fillmode = fillmode;
    }

    public Environment getEnvironment(int id) {
        for (Environment environment : environments) {
            if (environment.getId() == id) {
                return environment;
            }
        }
        return null;
    }

    public List<Environment> getEnvironments(Class<? extends Environment> clazz) {
        List<Environment> result = new ArrayList<>();
        for (Environment environment : environments) {
            if (clazz.isInstance(environment)) {
                result.add(environment);
            }
        }
        return result;
    }

    public void remove() {
        for (Environment environment : environments) {
            environment.kill();
        }
    }

    public boolean isInChunk(Chunk chunk) {
        MineRegion region = getRegion();
        org.bukkit.World world = chunk.getWorld();
        if (!getWorld().equals(world)) return false;
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        int minX = region.getRegion().getMinimumPoint().getBlockX() >> 4;
        int maxX = region.getRegion().getMaximumPoint().getBlockX() >> 4;
        int minZ = region.getRegion().getMinimumPoint().getBlockZ() >> 4;
        int maxZ = region.getRegion().getMaximumPoint().getBlockZ() >> 4;
        return chunkX >= minX && chunkX <= maxX && chunkZ >= minZ && chunkZ <= maxZ;
    }
}
