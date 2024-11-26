package dk.martinersej.plugin;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.martinersej.api.worldguard.WorldGuardInterface;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.MineBlock;
import dk.martinersej.plugin.mine.environment.Environment;
import dk.martinersej.plugin.mine.environment.EnvironmentType;
import dk.martinersej.plugin.mine.environment.environments.DestroyedEnvironment;
import dk.martinersej.plugin.mine.environment.environments.ScheduledEnvironment;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MineManager {

    private final FlawMines plugin = FlawMines.get();
    private final MineController mineController = plugin.getMineController();

    private final World world;
    private final Map<ProtectedRegion, Mine> mines = new HashMap<>();

    public MineManager(World world) {
        this.world = world;
    }

    void enable() {
        plugin.getLogger().info("Loading mines from world: " + world.getName());

        mines.putAll(mineController.loadMines(world));
        for (Mine mine : mines.values()) {
            plugin.getLogger().info("Loaded mine: " + mine.getName());
        }
    }


    public void disable() {
        for (Mine mine : mines.values()) {
            mineController.saveMine(mine);
        }
        mines.clear();
    }

    public Mine getMine(String name) {
        for (Mine mine : mines.values()) {
            if (mine.getName().equalsIgnoreCase(name)) {
                return mine;
            }
        }
        return null;
    }

    public Mine getMine(ProtectedRegion region) {
        return mines.get(region);
    }

    public Mine createMine(ProtectedRegion region, String name) {
        Mine mineLookup = mines.get(region);
        if (mineLookup != null && mineLookup.getRegion().getProtectedRegion().equals(region)) { // mine already exists
            plugin.getLogger().warning("Mine already exists for region: " + region.getId());
            return null;
        }
        if (mineLookup != null) { // mine exists but region is different
            mineLookup.remove();
        }

        // continue creating mine
        Mine mine = new Mine(name, region, world);
        mines.put(region, mine);
        WorldGuardInterface worldGuardInterface = plugin.getWorldGuardInterface();
        mineController.createMine(mine.getName(), mine.getWorld(), region.getId(), worldGuardInterface.getMaximumPoint(region));
        return mine;
    }

    public boolean deleteMine(Mine mine) {
        if (mine == null) {
            return false;
        }
        mines.remove(mine.getRegion().getProtectedRegion());
        mine.remove();
        plugin.getWorldGuardInterface().getRegionManager(world).removeRegion(mine.getRegion().getId());
        mineController.deleteMine(mine.getName(), mine.getRegion().getId());
        return true;
    }

    public MineBlock addBlock(Mine mine, MineBlock block) {
        // if a block already exists in mine, get the object and update it
        MineBlock existingBlock = mine.getBlock(block.getBlockData());
        if (existingBlock != null) {
            existingBlock.setWeight(block.getWeight());
            mineController.updateBlock(existingBlock);
            return existingBlock;
        }
        mine.addBlock(block);
        mineController.addBlock(mine.getName(), block);
        return null;
    }

    public void setBlocks(Mine mine, List<MineBlock> blocks) {
        for (MineBlock block : mine.getBlocks()) {
            mineController.removeBlock(block);
        }
        mine.clearBlocks();
        for (MineBlock block : blocks) {
            mine.addBlock(block);
            mineController.addBlock(mine.getName(), block);
        }
    }

    public void removeBlock(Mine mine, MineBlock block) {
        if (block.getId() == -1) {
            plugin.getLogger().warning("Block not found in mine: " + mine.getName());
            return;
        }

        mine.removeBlock(block);
        mineController.removeBlock(block);
    }

    public Environment addEnvironment(Mine mine, EnvironmentType environmentType, Object[] values) {
        Environment environment = null;
        switch (environmentType) {
            case DESTROYED: {
                try {
                    float ratio = Float.parseFloat(String.valueOf(values[0]));
                    environment = new DestroyedEnvironment(mine, ratio);
                } catch (NumberFormatException e) {
                    return null;
                }
                break;
            }
            case SCHEDULED: {
                try {
                    int interval = Integer.parseInt(String.valueOf(values[0]));
                    environment = new ScheduledEnvironment(mine, interval);
                } catch (NumberFormatException e) {
                    return null;
                }
                break;
            }
        }
        mine.addEnvironment(environment);
        mineController.addEnvironment(mine.getName(), environment);
        return environment;
    }

    public void removeEnvironment(Mine mine, Environment environment) {
        mine.removeEnvironment(environment);
        mineController.removeEnvironment(environment);
    }

    public void editMine(Mine mine, Consumer<Mine> function) {
        function.andThen((v) -> mineController.saveOnlyMine(mine)).accept(mine);
    }

    public List<String> getMineNames() {
        List<String> mineNames = new ArrayList<>();
        for (Mine mine : mines.values()) {
            mineNames.add(mine.getName());
        }
        return mineNames;
    }
}
