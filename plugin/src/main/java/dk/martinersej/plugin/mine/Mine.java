package dk.martinersej.plugin.mine;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.patterns.BlockChance;
import com.sk89q.worldedit.patterns.RandomFillPattern;
import com.sk89q.worldedit.regions.Region;
import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.mine.environment.Environment;
import dk.martinersej.plugin.mine.mineblock.MineBlock;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

@Getter
public class Mine {

    private final String id;
    private final List<MineBlock> blocks = new ArrayList<>();
    @Setter
    private Region region;

    private final PriorityQueue<Environment> environments = new PriorityQueue<>();

    public Mine(String id) {
        this.id = id;
    }

    public void reset() {
        //TODO: this should refactor because some methods is not available then we upgrade worldedit version, f.e.
        // RandomPattern is the new class name for RandomFillPattern.
        // Same for BlockChance.
        Bukkit.getScheduler().runTaskAsynchronously(FlawMines.getInstance(), () -> {

            EditSessionFactory editSession = FlawMines.getInstance().getWorldEdit().getWorldEdit().getEditSessionFactory();
            EditSession session = editSession.getEditSession(region.getWorld(), region.getArea()); // region.getArea()
            // should work, unless we could change it to -1 for a hard fix. I hope it's not necessary.

            try {
                List<BlockChance> blockChances = new ArrayList<>();
                blocks.forEach((block -> {
                    BaseBlock baseBlock = new BaseBlock(block.getBlock().getType().getId(), block.getBlock().getData().getData());
                    blockChances.add(new BlockChance(baseBlock, block.getPercentage()));
                }));

                RandomFillPattern pattern = new RandomFillPattern(blockChances);

                session.setBlocks(region, pattern);

                //
            } catch (MaxChangedBlocksException e) {
                e.printStackTrace();
            } finally {
                session.flushQueue();
            }


        });
    }

    public void addEnvironment(Environment environment) {
        environments.add(environment);
    }

    public long getTotalBlocks() {
        return (long) region.getArea() * region.getHeight(); // area * height = total blocks
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
}
