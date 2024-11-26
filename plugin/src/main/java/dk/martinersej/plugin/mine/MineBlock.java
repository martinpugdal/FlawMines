package dk.martinersej.plugin.mine;

import dk.martinersej.plugin.FlawMines;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.Map;

public class MineBlock {

    private int id = -1;
    private MaterialData materialData;
    private float weight;

    public MineBlock(Material material, float weight) {
        this(new MaterialData(material), weight);
    }

    public MineBlock(MaterialData materialData, float weight) {
        this.materialData = materialData;
        this.weight = weight;
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
        float weight = Float.parseFloat(map.get("weight"));

        return new MineBlock(materialData, weight);
    }

    public String serialize() {
        Map<String, String> data = new HashMap<>();
        data.put("block", materialData.getItemType().name());
        if (FlawMines.isLegacy()) {
            data.put("data", String.valueOf(materialData.getData()));
        }
        data.put("weight", String.valueOf(getWeight()));

        // Serialize the data map
        return data.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).reduce((a, b) -> a + "," + b).orElse("");
    }

    public MaterialData getBlockData() {
        return materialData.clone();
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
