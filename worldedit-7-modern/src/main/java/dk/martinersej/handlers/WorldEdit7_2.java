package dk.martinersej.handlers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.world.block.BlockState;
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
}