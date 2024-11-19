package dk.martinersej.plugin;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.MineBlock;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

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

    public Mine createMine(ProtectedRegion region, String name) {
        Mine mineLookup = mines.get(region);
        if (mineLookup != null && mineLookup.getRegion().getProtectedRegion().equals(region)) { // mine already exists
            plugin.getLogger().warning("Mine already exists for region: " + region.getId());
            return null;
        }
        // continue creating mine
        Mine mine = new Mine(name, region, world);
        mines.put(region, mine);
        mineController.createMine(mine.getName(), mine.getWorld(), region.getId());

        plugin.getLogger().info("Mine created: " + name + " with region: " + region.getId());
        return mine;
    }

    public boolean deleteMine(Mine mine) {
        if (mine == null) {
            return false;
        }

        mineController.deleteMine(mine.getName(), mine.getRegion().getId());
        mines.remove(mine.getRegion().getProtectedRegion());
        plugin.getLogger().info("Mine deleted: " + mine.getName() + " with region: " + mine.getRegion().getId());
        return true;
    }

    public MineBlock addBlock(Mine mine, MineBlock block) {
        // if a block already exists in mine, get the object and update it
        MineBlock existingBlock = mine.getBlock(block.getBlockData());
        if (existingBlock != null) {
            existingBlock.setPercentage(block.getPercentage());
            mine.updateBlock(existingBlock);
            return existingBlock;
        }
        mine.addBlock(block);
        mineController.addBlock(mine.getName(), block);
        return null;
    }

    public void removeBlock(Mine mine, MineBlock block) {
        if (block.getId() == -1) {
            plugin.getLogger().warning("Block not found in mine: " + mine.getName());
            return;
        }

        mine.removeBlock(block);
        mineController.removeBlock(block);
    }
}
