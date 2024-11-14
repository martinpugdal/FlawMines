package dk.martinersej.plugin.mine.mineblock;

//import org.bukkit.block.data.BlockData;

import org.bukkit.inventory.ItemStack;

//TODO: implement this later, then we adding multiple version support - for now we only support 1.8.8 - 1.12.2
public class ModernMineBlock extends MineBlock {

    public ModernMineBlock(float percentage) {
        super(percentage);
    }

    @Override
    public ItemStack getBlock() {
        return null;
    }

//    private BlockData block;
//
//    public ModernMineBlock(Material material, float percentage) {
//        this(material.createBlockData(), percentage);
//    }
//
//    public ModernMineBlock(BlockData block, float percentage) {
//        super(percentage);
//        this.block = block;
//    }
//
//    public BlockData getBlock() {
//        return this.block.clone();
//    }
//
//    public void setBlock(BlockData block) {
//        this.block = block.clone();
//    }
//
//    public String toString() {
//        return "[block=" + block.getAsString() + ", " +
//            "percentage=" + getPercentage() + "]";
//    }
}
