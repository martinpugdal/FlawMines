package dk.martinersej.plugin.mine.mineblock;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class LegacyMineBlock extends MineBlock {

    private ItemStack block;

    public LegacyMineBlock(Material material, float percentage) {
        this(new ItemStack(material), percentage);
    }

    public LegacyMineBlock(ItemStack block, float percentage) {
        super(percentage);
        this.block = block;
    }

    public ItemStack getBlock() {
        return this.block.clone();
    }

    public void setBlock(ItemStack block) {
        this.block = block.clone();
        this.block.setAmount(1);
    }

    public String toString() {
        return "[block=" + block.toString() + ", " +
            "percentage=" + getPercentage() + "]";
    }
}
