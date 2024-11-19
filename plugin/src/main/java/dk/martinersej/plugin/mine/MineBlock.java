package dk.martinersej.plugin.mine;

import dk.martinersej.plugin.FlawMines;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.Map;

public class MineBlock {

    private int id = -1;
    private MaterialData materialData;
    private float percentage;

    public MineBlock(Material material, float percentage) {
        this(new MaterialData(material), percentage);
    }

    public MineBlock(MaterialData materialData, float percentage) {
        this.materialData = materialData;
        this.percentage = percentage;
    }

    public static MineBlock deserialize(String data) {
        Map<String, String> map = new HashMap<>();
        for (String entry : data.split(",")) {
            String[] split = entry.split(":");
            if (split.length == 2) {
                map.put(split[0], split[1]);
            }
        }

        Material material = Material.getMaterial(map.get("block"));
        MaterialData materialData = FlawMines.isLegacy() ? material.getNewData(Byte.parseByte(map.get("data"))): new MaterialData(material);
        float percentage = Float.parseFloat(map.get("percentage"));

        return new MineBlock(materialData, percentage);
    }

    public MaterialData getBlockData() {
        return materialData.clone();
    }

    public String serialize() {
        Map<String, String> data = new HashMap<>();
        data.put("block", materialData.getItemType().name());
        if (FlawMines.isLegacy()) {
            data.put("data", String.valueOf(materialData.getData()));
        }
        data.put("percentage", String.valueOf(getPercentage()));

        // Serialize the data map
        return data.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).reduce((a, b) -> a + "," + b).orElse("");
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
