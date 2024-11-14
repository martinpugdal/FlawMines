package dk.martinersej.plugin.mine.mineblock;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class MineBlock {

    @Setter
    private float percentage;

    public MineBlock(float percentage) {
        this.percentage = percentage;
    }

    public abstract ItemStack getBlock();

    public abstract String serialize();

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
}
