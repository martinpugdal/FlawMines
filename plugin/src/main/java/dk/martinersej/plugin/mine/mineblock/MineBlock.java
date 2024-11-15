package dk.martinersej.plugin.mine.mineblock;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public abstract class MineBlock {

    private float percentage;

    public MineBlock(float percentage) {
        this.percentage = percentage;
    }

    public static MineBlock deserialize(String data) {
        Map<String, String> map = new HashMap<>();
        for (String entry : data.split(",")) {
            String[] split = entry.split(":");
            map.put(split[0], split[1]);
        }


        if (map.get("type").equals("legacy")) {
            return LegacyMineBlock.deserialize(data);
        } else if (map.get("type").equals("modern")) {
            return ModernMineBlock.deserialize(data);
        }

        return null;
    }

    public abstract ItemStack getBlock();

    public abstract String serialize();

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }
}
