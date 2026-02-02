package dk.martinersej.plugin.mine;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.mine.environment.Environment;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.BlockVector;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MineDataMigrator {

    public static boolean needsMigration(File dataFolder) {
        File sqliteDb = new File(dataFolder, "FlawMines.db");
        File minesFolder = new File(dataFolder, "mines");

        // Need migration if DB exists and mines folder is empty or missing
        if (!sqliteDb.exists()) {
            return false;
        }

        if (!minesFolder.exists()) {
            return true;
        }

        File[] mineFiles = minesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        return mineFiles == null || mineFiles.length == 0;
    }

    public static void migrate(File dataFolder) {
        FlawMines plugin = FlawMines.get();
        plugin.getLogger().info("Migrating mines from SQLite to YAML...");

        File sqliteDb = new File(dataFolder, "FlawMines.db");
        File minesFolder = new File(dataFolder, "mines");

        if (!minesFolder.exists()) {
            minesFolder.mkdirs();
        }

        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteDb.getAbsolutePath());

            // Load all mines from SQLite
            String query = "SELECT * FROM mines";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            int migratedCount = 0;
            while (resultSet.next()) {
                String mineId = resultSet.getString("id");
                String worldName = resultSet.getString("world");
                String regionName = resultSet.getString("region");
                boolean fillmode = resultSet.getBoolean("fillmode");
                String teleportLocationStr = resultSet.getString("teleportLocation");

                // Parse teleport location
                Map<String, Double> teleportMap = new HashMap<>();
                if (teleportLocationStr != null) {
                    for (String entry : teleportLocationStr.split(",")) {
                        String[] split = entry.split(":");
                        if (split.length == 2) {
                            try {
                                teleportMap.put(split[0], Double.parseDouble(split[1]));
                            } catch (NumberFormatException e) {
                                teleportMap.put(split[0], 0.0);
                            }
                        }
                    }
                }

                // Load blocks for this mine
                List<Map<String, Object>> blocks = new ArrayList<>();
                String blocksQuery = "SELECT data FROM mineBlocks WHERE mineId = ?";
                PreparedStatement blocksStatement = connection.prepareStatement(blocksQuery);
                blocksStatement.setString(1, mineId);
                ResultSet blocksResultSet = blocksStatement.executeQuery();

                while (blocksResultSet.next()) {
                    String blockData = blocksResultSet.getString("data");
                    Map<String, String> blockMap = parseData(blockData);

                    Map<String, Object> yamlBlock = new LinkedHashMap<>();
                    yamlBlock.put("material", blockMap.get("block"));
                    if (blockMap.containsKey("data")) {
                        yamlBlock.put("data", Integer.parseInt(blockMap.get("data")));
                    }
                    yamlBlock.put("weight", Float.parseFloat(blockMap.get("weight")));
                    blocks.add(yamlBlock);
                }
                blocksResultSet.close();

                // Load environments for this mine
                List<Map<String, Object>> environments = new ArrayList<>();
                String envsQuery = "SELECT type, data FROM mineEnvironments WHERE mineId = ?";
                PreparedStatement envsStatement = connection.prepareStatement(envsQuery);
                envsStatement.setString(1, mineId);
                ResultSet envsResultSet = envsStatement.executeQuery();

                while (envsResultSet.next()) {
                    String envType = envsResultSet.getString("type");
                    String envData = envsResultSet.getString("data");
                    Map<String, String> envMap = parseData(envData);

                    Map<String, Object> yamlEnv = new LinkedHashMap<>();

                    if ("DestroyedEnvironment".equals(envType)) {
                        yamlEnv.put("type", "destroyed");
                        yamlEnv.put("ratio", Float.parseFloat(envMap.get("ratio")));
                        if (envMap.containsKey("blocksDestroyed")) {
                            yamlEnv.put("blocksDestroyed", Integer.parseInt(envMap.get("blocksDestroyed")));
                        }
                    } else if ("ScheduledEnvironment".equals(envType)) {
                        yamlEnv.put("type", "scheduled");
                        yamlEnv.put("intervalSeconds", Integer.parseInt(envMap.get("intervalSeconds")));
                        if (envMap.containsKey("timeSinceLastRun")) {
                            yamlEnv.put("timeSinceLastRun", Long.parseLong(envMap.get("timeSinceLastRun")));
                        }
                    }

                    if (!yamlEnv.isEmpty()) {
                        environments.add(yamlEnv);
                    }
                }
                envsResultSet.close();

                // Write YAML file
                File mineFile = new File(minesFolder, mineId + ".yml");
                YamlConfiguration config = new YamlConfiguration();

                config.set("name", mineId);
                config.set("world", worldName);
                config.set("region", regionName);
                config.set("fillmode", fillmode);
                config.set("teleportLocation.x", teleportMap.getOrDefault("x", 0.0));
                config.set("teleportLocation.y", teleportMap.getOrDefault("y", 0.0));
                config.set("teleportLocation.z", teleportMap.getOrDefault("z", 0.0));
                config.set("blocks", blocks);
                config.set("environments", environments);

                config.save(mineFile);
                plugin.getLogger().info("Migrated mine: " + mineId);
                migratedCount++;
            }
            resultSet.close();
            connection.close();

            // Rename old database to backup
            File backupDb = new File(dataFolder, "FlawMines.db.backup");
            if (backupDb.exists()) {
                backupDb.delete();
            }
            sqliteDb.renameTo(backupDb);

            plugin.getLogger().info("Migration complete! Migrated " + migratedCount + " mines.");
            plugin.getLogger().info("Old database backed up to FlawMines.db.backup");

        } catch (ClassNotFoundException | SQLException | IOException e) {
            plugin.getLogger().severe("Failed to migrate mines from SQLite to YAML!");
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Map<String, String> parseData(String data) {
        Map<String, String> map = new HashMap<>();
        if (data == null) return map;

        for (String entry : data.split(",")) {
            String[] split = entry.split(":");
            if (split.length == 2) {
                map.put(split[0], split[1]);
            }
        }
        return map;
    }
}
