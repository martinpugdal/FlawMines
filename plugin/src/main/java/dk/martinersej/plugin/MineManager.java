package dk.martinersej.plugin;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.mineblock.MineBlock;
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
    }


    public void disable() {
        for (Mine mine : mines.values()) {
            mineController.saveMine(mine);
        }
        mines.clear();
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

    public boolean deleteMine(ProtectedRegion region) {
        Mine mine = mines.get(region);
        if (mine == null) {
            plugin.getLogger().warning("No mine found for region: " + region.getId());
            return false;
        }

        mineController.deleteMine(mine.getName(), mine.getRegion().getId());
        mines.remove(region);
        plugin.getLogger().info("Mine deleted: " + mine.getName() + " with region: " + region.getId());
        return true;
    }

    public boolean addBlock(Mine mine, MineBlock block) {
        if (!block.getBlock().getType().isBlock()) return false;

        mine.addBlock(block);
        mineController.addBlock(mine.getName(), block);
        return true;
    }

    public boolean removeBlock(Mine mine, MineBlock block) {
        if (block.getId() == -1) {
            plugin.getLogger().warning("Block not found in mine: " + mine.getName());
            return false;
        }

        mine.removeBlock(block);
        mineController.removeBlock(block);
        return true;
    }
}
