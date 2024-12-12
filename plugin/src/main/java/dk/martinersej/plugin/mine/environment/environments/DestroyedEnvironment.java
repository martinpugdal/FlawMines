package dk.martinersej.plugin.mine.environment.environments;

import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.environment.Environment;
import dk.martinersej.plugin.mine.environment.EnvironmentType;

import java.util.HashMap;
import java.util.Map;

public class DestroyedEnvironment extends Environment {

    private final float ratio; // The ratio of blocks that should be destroyed (defined by %, 0-100, where we convert it to 0-1)
    private final int blocksNeeded; // The amount of blocks needed to be destroyed
    private int blocksDestroyed; // The amount of blocks destroyed

    // this is used for creating the environment, and the id will be placed by the database then queried
    public DestroyedEnvironment(Mine mine, float ratio) {
        this(0, mine, ratio);
    }

    public DestroyedEnvironment(int id, Mine mine, float ratio) {
        this(id, mine, ratio, 0);
    }

    public DestroyedEnvironment(int id, Mine mine, float ratio, int blocksDestroyed) {
        super(id, mine);

        if (ratio < 0 || ratio > 100) {
            throw new IllegalArgumentException("The ratio must be between 0 and 100");
        }
        this.ratio = ratio / 100f; // Convert the ratio to 0-1
        this.blocksNeeded = (int) (mine.getTotalBlocks() * ratio); // Calculate the amount of blocks needed to be destroyed
        this.blocksDestroyed = blocksDestroyed;
    }

    @Override
    public void reset() {
        // Reset the environment
        blocksDestroyed = 0;
    }

    @Override
    public double getProgress() {
        int blocksLeft = blocksNeeded - blocksDestroyed; // Calculate the blocks left
        return blocksLeft <= 0 ? 100 : 100 - (blocksLeft * 100.0 / blocksNeeded); // Calculate the progress
    }

    public void increaseBlocksDestroyed() {
        blocksDestroyed++;
        if (blocksDestroyed >= blocksNeeded) {
            mine.reset();
        }
    }

    @Override
    public String serialize() {
        // just the extra data we need to serialize
        Map<String, String> data = new HashMap<>();
        data.put("ratio", String.valueOf(ratio));
        data.put("blocksDestroyed", String.valueOf(blocksDestroyed));

        // Serialize the data map
        return data.entrySet().stream()
            .map(entry -> entry.getKey() + ":" + entry.getValue())
            .reduce((a, b) -> a + "," + b)
            .orElse("");
    }

    @Override
    public EnvironmentType getType() {
        return EnvironmentType.DESTROYED;
    }

    @Override
    public void kill() {
        // Nothing to do here
    }

    public static DestroyedEnvironment deserialize(Mine mine, String data) {
        Map<String, String> map = new HashMap<>();
        for (String entry : data.split(",")) {
            String[] split = entry.split(":");
            map.put(split[0], split[1]);
        }

        float ratio = Float.parseFloat(map.get("ratio"));
        int blocksDestroyed = Integer.parseInt(map.get("blocksDestroyed"));
        return new DestroyedEnvironment(0, mine, ratio, blocksDestroyed);
    }
}
