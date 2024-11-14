package dk.martinersej.plugin.mine.mineblock;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

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

    @Override
    public String toString() {
        return "[block=" + block.toString() + ", " +
            "percentage=" + getPercentage() + "]";
    }

    @Override
    public String serialize() {
        Map<String, String> data = new HashMap<>();
        data.put("type", "legacy");
        data.put("block", block.getType().name());
        data.put("data", String.valueOf(block.getData().getData()));
        data.put("percentage", String.valueOf(getPercentage()));

        // Serialize the data map
        return data.entrySet().stream()
            .map(entry -> entry.getKey() + ":" + entry.getValue())
            .reduce((a, b) -> a + "," + b)
            .orElse("");
    }

    public static LegacyMineBlock deserialize(String data) {
        Map<String, String> map = new HashMap<>();
        for (String entry : data.split(",")) {
            String[] split = entry.split(":");
            map.put(split[0], split[1]);
        }

        Material material = Material.getMaterial(map.get("block"));
        byte dataByte = Byte.parseByte(map.get("data"));
        float percentage = Float.parseFloat(map.get("percentage"));

        return new LegacyMineBlock(new ItemStack(material, 1, dataByte), percentage);
    }
}
