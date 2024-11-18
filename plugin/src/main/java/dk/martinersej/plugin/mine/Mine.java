package dk.martinersej.plugin.mine;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.patterns.BlockChance;
import com.sk89q.worldedit.patterns.RandomFillPattern;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.mine.environment.Environment;
import dk.martinersej.plugin.mine.mineblock.MineBlock;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class Mine {

    private final String name;
    private final List<MineBlock> blocks = new ArrayList<>();
    private final List<Environment> environments = new ArrayList<>();
    private final MineRegion mineRegion;

    public Mine(String name, ProtectedRegion region, World world) {
        this.name = name;
        this.mineRegion = new MineRegion(region, world);
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

        //TODO: if its not legacy, we can make it async, in legacy we can not change the blocks async.
        EditSessionFactory editSession = FlawMines.get().getWorldEdit().getWorldEdit().getEditSessionFactory();

        EditSession session = editSession.getEditSession(new BukkitWorld(getWorld()), getRegion().getVolume());
        // should work, unless we could change it to -1 for a hard fix. I hope it's not necessary.

        try {
            List<BlockChance> blockChances = new ArrayList<>();
            blocks.forEach((block -> {
                BaseBlock baseBlock = new BaseBlock(block.getBlock().getType().getId(), block.getBlock().getData().getData());
                blockChances.add(new BlockChance(baseBlock, block.getPercentage()));
            }));

            RandomFillPattern pattern = new RandomFillPattern(blockChances);

            session.setBlocks(mineRegion.getRegion(), pattern);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        } finally {
            session.flushQueue();
        }
    }

    public void addEnvironment(Environment environment) {
        environments.add(environment);
    }

    public long getTotalBlocks() {
        return mineRegion.getVolume();
    }

    private void resetFinished() {
        //todo: implement a way to reset conditions then we have implemented the conditions.
        //todo: call the api? Event? Idk yet.
    }

    public void addBlock(MineBlock block) {
        blocks.add(block);
    }

    public void removeBlock(MineBlock block) {
        blocks.remove(block);
    }

    public void clearBlocks() {
        blocks.clear();
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        // generate hashcode from name and region
        return (name + getWorld().getName()).hashCode();
    }
}
