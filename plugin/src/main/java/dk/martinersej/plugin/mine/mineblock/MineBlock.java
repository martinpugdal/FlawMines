package dk.martinersej.plugin.mine.mineblock;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter
public abstract class MineBlock {

    @Setter
    private float percentage;

    public MineBlock(float percentage) {
        this.percentage = percentage;
    }

    public abstract ItemStack getBlock();
}
