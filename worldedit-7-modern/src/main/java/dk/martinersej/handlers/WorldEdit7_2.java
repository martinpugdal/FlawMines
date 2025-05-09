package dk.martinersej.handlers;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import dk.martinersej.api.FlawMinesInterface;
import org.bukkit.block.data.BlockData;
import org.bukkit.material.MaterialData;

public class WorldEdit7_2 extends WorldEdit7_0 {

    public WorldEdit7_2(FlawMinesInterface pluginInterface) {
        super(pluginInterface);
    }

    @Override
    public AbstractPattern createBlockPattern(MaterialData materialData) {
        BlockData blockData = materialData.getItemType().createBlockData();
        BlockState blockState = BukkitAdapter.adapt(blockData);
        return new BlockPattern(blockState);
    }

    public BlockMask createBlockMask(EditSession editSession, MaterialData... materialDatas) {
        BlockMask blockMask = new BlockMask(editSession);
        for (MaterialData materialData : materialDatas) {
            BlockData blockData = materialData.getItemType().createBlockData();
            BlockState blockState = BukkitAdapter.adapt(blockData);
            blockMask.add(blockState.toBaseBlock());
        }
        return blockMask;
    }
}