package dk.martinersej.plugin.mine;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.mine.environment.Environment;
import dk.martinersej.plugin.mine.environment.EnvironmentType;
import dk.martinersej.plugin.mine.environment.environments.DestroyedEnvironment;
import dk.martinersej.plugin.mine.environment.environments.ScheduledEnvironment;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.material.MaterialData;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MineController {

    private final File minesFolder;

    public MineController() {
        this.minesFolder = new File(FlawMines.get().getDataFolder(), "mines");
        if (!minesFolder.exists()) {
            minesFolder.mkdirs();
            // Copy example config to help users understand the format
            copyExampleConfig();
        }
    }

    private void copyExampleConfig() {
        try {
            FlawMines plugin = FlawMines.get();
            File exampleFile = new File(minesFolder, "example.yml.txt");
            if (!exampleFile.exists()) {
                plugin.saveResource("mines/example.yml.txt", false);
            }
        } catch (Exception e) {
            FlawMines.get().getLogger().warning("Could not copy example mine config: " + e.getMessage());
        }
    }

    private File getMineFile(String mineName) {
        return new File(minesFolder, mineName + ".yml");
    }

    public void createMine(String id, World world, String region, Vector teleportLocation) {
        File file = getMineFile(id);
        YamlConfiguration config = new YamlConfiguration();

        config.set("name", id);
        config.set("world", world.getName());
        config.set("region", region);
        config.set("fillmode", false);
        config.set("teleportLocation.x", teleportLocation.getX());
        config.set("teleportLocation.y", teleportLocation.getY());
        config.set("teleportLocation.z", teleportLocation.getZ());
        config.set("blocks", new ArrayList<>());
        config.set("environments", new ArrayList<>());

        try {
            config.save(file);
        } catch (IOException e) {
            FlawMines.get().getLogger().severe("Failed to create mine file: " + id);
            e.printStackTrace();
        }
    }

    public Map<ProtectedRegion, Mine> loadMines(World world) {
        Map<ProtectedRegion, Mine> mines = new HashMap<>();
        File[] mineFiles = minesFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (mineFiles == null) return mines;

        for (File file : mineFiles) {
            try {
                Mine mine = loadMineFromFile(file, world);
                if (mine != null) {
                    mines.put(mine.getRegion().getProtectedRegion(), mine);
                }
            } catch (Exception e) {
                FlawMines.get().getLogger().severe("Failed to load mine from file: " + file.getName());
                e.printStackTrace();
            }
        }

        return mines;
    }

    private Mine loadMineFromFile(File file, World world) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        String worldName = config.getString("world");
        if (!world.getName().equals(worldName)) {
            return null;
        }

        String mineName = config.getString("name");
        String regionName = config.getString("region");
        boolean fillmode = config.getBoolean("fillmode", false);

        double x = config.getDouble("teleportLocation.x");
        double y = config.getDouble("teleportLocation.y");
        double z = config.getDouble("teleportLocation.z");
        BlockVector teleportLoc = new BlockVector(x, y, z);

        ProtectedRegion region = FlawMines.get().getWorldGuardInterface()
            .getRegionManager(world).getRegion(regionName);
        if (region == null) {
            FlawMines.get().getLogger().warning("Mine region not found: " + regionName);
            return null;
        }

        Mine mine = new Mine(mineName, region, world, fillmode, teleportLoc);

        // Load blocks
        List<Map<?, ?>> blocksList = config.getMapList("blocks");
        for (Map<?, ?> blockMap : blocksList) {
            try {
                String materialName = (String) blockMap.get("material");
                Material material = Material.getMaterial(materialName);
                if (material == null) {
                    FlawMines.get().getLogger().warning("Unknown material in mine " + mineName + ": " + materialName);
                    continue;
                }

                int data = blockMap.containsKey("data") ? ((Number) blockMap.get("data")).intValue() : 0;
                float weight = ((Number) blockMap.get("weight")).floatValue();

                MaterialData materialData = FlawMines.isLegacy()
                    ? material.getNewData((byte) data)
                    : new MaterialData(material);
                MineBlock mineBlock = new MineBlock(materialData, weight);
                mine.addBlock(mineBlock);
            } catch (Exception e) {
                FlawMines.get().getLogger().warning("Failed to load block in mine " + mineName);
                e.printStackTrace();
            }
        }

        // Load environments
        List<Map<?, ?>> envList = config.getMapList("environments");
        for (Map<?, ?> envMap : envList) {
            try {
                String typeStr = (String) envMap.get("type");
                EnvironmentType envType = EnvironmentType.fromString(typeStr);
                if (envType == null) {
                    FlawMines.get().getLogger().warning("Unknown environment type in mine " + mineName + ": " + typeStr);
                    continue;
                }

                Environment environment = deserializeEnvironment(mine, envType, envMap);
                if (environment != null) {
                    mine.addEnvironment(environment);
                }
            } catch (Exception e) {
                FlawMines.get().getLogger().warning("Failed to load environment in mine " + mineName);
                e.printStackTrace();
            }
        }

        return mine;
    }

    private Environment deserializeEnvironment(Mine mine, EnvironmentType type, Map<?, ?> data) {
        switch (type) {
            case DESTROYED: {
                float ratio = ((Number) data.get("ratio")).floatValue();
                int blocksDestroyed = data.containsKey("blocksDestroyed")
                    ? ((Number) data.get("blocksDestroyed")).intValue()
                    : 0;
                // ratio is stored as decimal (0-1), but constructor expects 0-100
                return new DestroyedEnvironment(0, mine, ratio * 100, blocksDestroyed);
            }
            case SCHEDULED: {
                int intervalSeconds = ((Number) data.get("intervalSeconds")).intValue();
                long timeSinceLastRun = data.containsKey("timeSinceLastRun")
                    ? ((Number) data.get("timeSinceLastRun")).longValue()
                    : 0;
                return new ScheduledEnvironment(0, mine, intervalSeconds, timeSinceLastRun);
            }
            default:
                return null;
        }
    }

    public void saveMine(Mine mine) {
        File file = getMineFile(mine.getName());
        YamlConfiguration config = new YamlConfiguration();

        config.set("name", mine.getName());
        config.set("world", mine.getWorld().getName());
        config.set("region", mine.getRegion().getProtectedRegion().getId());
        config.set("fillmode", mine.isFillmode());
        config.set("teleportLocation.x", mine.getTeleportLocation().getX());
        config.set("teleportLocation.y", mine.getTeleportLocation().getY());
        config.set("teleportLocation.z", mine.getTeleportLocation().getZ());

        // Save blocks
        List<Map<String, Object>> blocksList = new ArrayList<>();
        for (MineBlock block : mine.getBlocks()) {
            Map<String, Object> blockMap = new LinkedHashMap<>();
            blockMap.put("material", block.getBlockData().getItemType().name());
            if (FlawMines.isLegacy()) {
                blockMap.put("data", (int) block.getBlockData().getData());
            }
            blockMap.put("weight", block.getWeight());
            blocksList.add(blockMap);
        }
        config.set("blocks", blocksList);

        // Save environments
        List<Map<String, Object>> envList = new ArrayList<>();
        for (Environment env : mine.getEnvironments()) {
            envList.add(serializeEnvironment(env));
        }
        config.set("environments", envList);

        try {
            config.save(file);
        } catch (IOException e) {
            FlawMines.get().getLogger().severe("Failed to save mine: " + mine.getName());
            e.printStackTrace();
        }
    }

    private Map<String, Object> serializeEnvironment(Environment env) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", env.getType().name().toLowerCase());

        if (env instanceof DestroyedEnvironment) {
            DestroyedEnvironment destroyed = (DestroyedEnvironment) env;
            // Parse ratio from serialized string
            String serialized = destroyed.serialize();
            Map<String, String> map = parseSerializedData(serialized);
            data.put("ratio", Float.parseFloat(map.get("ratio")));
            data.put("blocksDestroyed", Integer.parseInt(map.get("blocksDestroyed")));
        } else if (env instanceof ScheduledEnvironment) {
            ScheduledEnvironment scheduled = (ScheduledEnvironment) env;
            data.put("intervalSeconds", scheduled.getIntervalSeconds());
            String serialized = scheduled.serialize();
            Map<String, String> map = parseSerializedData(serialized);
            data.put("timeSinceLastRun", Long.parseLong(map.get("timeSinceLastRun")));
        }

        return data;
    }

    private Map<String, String> parseSerializedData(String data) {
        Map<String, String> map = new HashMap<>();
        for (String entry : data.split(",")) {
            String[] split = entry.split(":");
            if (split.length == 2) {
                map.put(split[0], split[1]);
            }
        }
        return map;
    }

    public void saveOnlyMine(Mine mine) {
        saveMine(mine);
    }

    public void deleteMine(String id, String region) {
        File file = getMineFile(id);
        if (file.exists()) {
            file.delete();
        }
    }

    public void addBlock(String name, MineBlock block) {
        Mine mine = findMineByName(name);
        if (mine != null) {
            saveMine(mine);
        }
    }

    public void setBlocks(String name, List<MineBlock> blocks) {
        Mine mine = findMineByName(name);
        if (mine != null) {
            saveMine(mine);
        }
    }

    public void addEnvironment(String name, Environment environment) {
        Mine mine = findMineByName(name);
        if (mine != null) {
            saveMine(mine);
        }
    }

    private Mine findMineByName(String name) {
        for (World world : org.bukkit.Bukkit.getWorlds()) {
            MineManager manager = FlawMines.get().getMineManager(world);
            if (manager != null) {
                Mine mine = manager.getMine(name);
                if (mine != null) {
                    return mine;
                }
            }
        }
        return null;
    }

    public File getMinesFolder() {
        return minesFolder;
    }
}
