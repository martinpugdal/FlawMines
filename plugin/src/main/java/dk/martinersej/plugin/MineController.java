package dk.martinersej.plugin;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.environment.Environment;
import dk.martinersej.plugin.mine.MineBlock;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class MineController {

    private final Lock lock = new ReentrantLock(true);
    private Connection connection;

    public MineController() {
        establishConnection();
        createTables();
    }

    private void establishConnection() {
        try {
            Class.forName(getDriver());
            connection = DriverManager.getConnection(getURL());
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        if (isConnected()) {
            FlawMines.get().getLogger().info("Connected to database");
        } else {
            FlawMines.get().getLogger().severe("Failed to connect to database");
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Connection getConnection() {
        return connection;
    }

    public String getDatabase() {
        return FlawMines.get().getName();
    }

    public String getURL() {
        return String.format("jdbc:sqlite:%s%s%s.db", FlawMines.get().getDataFolder(), File.separator, getDatabase());
    }

    public String getDriver() {
        return "org.sqlite.JDBC";
    }

    public void createTables() {
        List<String> tables = new ArrayList<>();

        String minesTable = "CREATE TABLE IF NOT EXISTS mines (" +
            "id TEXT NOT NULL PRIMARY KEY," + // the name of the mine
            "world TEXT NOT NULL," + // where its located
            "region TEXT NOT NULL, " + // the region name
            "fillmode INT(1) DEFAULT 0, " + // if the mine is in fillmode
            "teleportLocation TEXT" + // the location to teleport to
            ");";
        tables.add(minesTable);

        String blocksTable = "CREATE TABLE IF NOT EXISTS mineBlocks (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "mineId TEXT NOT NULL," +
            "data TEXT NOT NULL," +
            "FOREIGN KEY (mineId) REFERENCES mines(id) ON DELETE CASCADE" +
            ");";
        tables.add(blocksTable);

        String environmentsTable = "CREATE TABLE IF NOT EXISTS mineEnvironments (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "mineId TEXT NOT NULL," +
            "type TEXT NOT NULL," +
            "data TEXT NOT NULL," +
            "FOREIGN KEY (mineId) REFERENCES mines(id) ON DELETE CASCADE" +
            ");";
        tables.add(environmentsTable);

        sync((connection) -> {
            try {
                for (String table : tables) {
                    connection.createStatement().executeUpdate(table);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void createMine(String id, World world, String region, Vector maximumPoint) {
        sync((connection) -> {
            try {
                PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO mines (id, world, region, teleportLocation) VALUES (?, ?, ?, ?)"
                );
                statement.setString(1, id);
                statement.setString(2, world.getName());
                statement.setString(3, region);
                statement.setString(4, serializeTeleportLocation(maximumPoint));

                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Map<ProtectedRegion, Mine> loadMines(World world) {
        Map<ProtectedRegion, Mine> mines = new HashMap<>();
        sync((connection) -> {
            FlawMines plugin = FlawMines.get();
            String query = "SELECT * FROM mines " +
                " WHERE mines.world = ?;";
            try {
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, world.getName());
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    String mineId = resultSet.getString("id");
                    String regionName = resultSet.getString("region");
                    boolean fillmode = resultSet.getBoolean("fillmode");
                    String teleportLocation = resultSet.getString("teleportLocation");
                    Map<String, Object> map = new HashMap<>();
                    for (String entry : teleportLocation.split(",")) {
                        String[] split = entry.split(":");
                        try {
                            map.put(split[0], Double.parseDouble(split[1]));
                        } catch (NumberFormatException e) {
                            map.put(split[0], 0); // default to 0, should never happen
                        }
                    }

                    // worldguard support for getting a region
                    ProtectedRegion region = plugin.getWorldGuardInterface().getRegionManager(world).getRegion(regionName);
                    if (region == null) {
                        plugin.getLogger().warning("Mine region not found: " + regionName);
                        continue;
                    }

                    Mine mine = new Mine(mineId, region, world, fillmode, BlockVector.deserialize(map));
                    mines.put(region, mine);

                    String queryBlocks = "SELECT mB.id AS blockId, mB.data AS blockData " +
                        "FROM mineBlocks mB " +
                        "JOIN mines m ON mB.mineId = m.id " +
                        "WHERE m.id = ?";

                    PreparedStatement blockStatement = connection.prepareStatement(queryBlocks);
                    blockStatement.setString(1, mineId);
                    ResultSet blockResultSet = blockStatement.executeQuery();
                    while (blockResultSet.next()) {
                        MineBlock block = MineBlock.deserialize(blockResultSet.getString("blockData"));
                        block.setId(blockResultSet.getInt("blockId"));
                        mine.addBlock(block);
                    }
                    blockResultSet.close();

                    String queryEnvironments = "SELECT mE.id AS environmentId, mE.type AS environmentType, mE.data AS environmentData " +
                        "FROM mineEnvironments mE " +
                        "JOIN mines m ON mE.mineId = m.id " +
                        "WHERE m.id = ?";
                    PreparedStatement environmentStatement = connection.prepareStatement(queryEnvironments);
                    environmentStatement.setString(1, mineId);
                    ResultSet environmentResultSet = environmentStatement.executeQuery();
                    while (environmentResultSet.next()) {
                        String environmentType = environmentResultSet.getString("environmentType");
                        String environmentData = environmentResultSet.getString("environmentData");
                        Environment environment = Environment.deserialize(mine, environmentType, environmentData);
                        environment.setId(environmentResultSet.getInt("environmentId"));
                        mine.addEnvironment(environment);
                    }
                    environmentResultSet.close();
                }
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        return mines;
    }

    public void saveOnlyMine(Mine mine) {
        sync((connection) -> {
            try {
                PreparedStatement statement = connection.prepareStatement(
                    "UPDATE mines SET world = ?, region = ?, fillmode = ?, teleportLocation = ? WHERE id = ?"
                );
                statement.setString(1, mine.getWorld().getName());
                statement.setString(2, mine.getRegion().getProtectedRegion().getId());
                statement.setString(3, mine.getName());
                statement.setBoolean(4, mine.isFillmode());
                String teleportVectorMappedToString = serializeTeleportLocation(mine.getTeleportLocation());
                statement.setString(5, teleportVectorMappedToString);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String serializeTeleportLocation(Vector teleportLocation) {
        return teleportLocation.serialize().entrySet().stream()
            .map(entry -> entry.getKey() + ":" + entry.getValue())
            .reduce((a, b) -> a + "," + b)
            .orElse("");
    }

    public void saveMine(Mine mine) {
        sync((connection) -> {
            try {
                // Save the current autoCommit state and disable it for transaction
                connection.setAutoCommit(false);

                // Main update statements
                PreparedStatement statement = connection.prepareStatement(
                    "UPDATE mines SET world = ?, region = ?, fillmode = ?, teleportLocation = ? WHERE id = ?"
                );
                statement.setString(1, mine.getWorld().getName());
                statement.setString(2, mine.getRegion().getProtectedRegion().getId());
                statement.setString(3, mine.getName());
                statement.setBoolean(4, mine.isFillmode());
                String teleportVectorMappedToString = serializeTeleportLocation(mine.getTeleportLocation());
                statement.setString(5, teleportVectorMappedToString);

                PreparedStatement blockStatement = connection.prepareStatement(
                    "UPDATE mineBlocks SET data = ? WHERE id = ?"
                );
                for (MineBlock block : mine.getBlocks()) {
                    blockStatement.setInt(1, block.getId());
                    blockStatement.setString(2, block.serialize());
                    blockStatement.addBatch();
                }

                PreparedStatement environmentStatement = connection.prepareStatement(
                    "UPDATE mineEnvironments SET data = ? WHERE id = ?"
                );
                for (Environment environment : mine.getEnvironments()) {
                    environmentStatement.setString(1, environment.serialize());
                    environmentStatement.setInt(2, environment.getId());
                    environmentStatement.addBatch();
                }

                // Execute updates
                statement.executeUpdate();
                blockStatement.executeBatch();
                environmentStatement.executeBatch();

                // Commit transaction
                connection.commit();
            } catch (SQLException e) {
                try {
                    if (!connection.getAutoCommit())
                        connection.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
                e.printStackTrace();
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void deleteMine(String id, String region) {
        sync((connection) -> {
            try {
                PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM mines WHERE id = ? AND region = ?"
                );
                statement.setString(1, id);
                statement.setString(2, region);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void addBlock(String name, MineBlock block) {
        sync((connection) -> {
            try {
                PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO mineBlocks (mineId, data) VALUES (?, ?)"
                );
                statement.setString(1, name);
                statement.setString(2, block.serialize());
                int id = statement.executeUpdate(); // get the id of the block
                block.setId(id);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void updateBlock(MineBlock block) {
        sync((connection) -> {
            try {
                PreparedStatement statement = connection.prepareStatement(
                    "UPDATE mineBlocks SET data = ? WHERE id = ?"
                );
                statement.setString(1, block.serialize());
                statement.setInt(2, block.getId());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void removeBlock(MineBlock block) {
        sync((connection) -> {
            try {
                PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM mineBlocks WHERE id = ?"
                );
                statement.setInt(1, block.getId());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void addEnvironment(String name, Environment environment) {
        sync((connection) -> {
            try {
                PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO mineEnvironments (mineId, type, data) VALUES (?, ?, ?)"
                );
                statement.setString(1, name);
                statement.setString(2, environment.getClass().getSimpleName());
                statement.setString(3, environment.serialize());
                int id = statement.executeUpdate(); // get the id of the environment
                environment.setId(id);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void updateEnvironment(Environment environment) {
        sync((connection) -> {
            try {
                PreparedStatement statement = connection.prepareStatement(
                    "UPDATE mineEnvironments SET data = ? WHERE id = ?"
                );
                statement.setString(1, environment.serialize());
                statement.setInt(2, environment.getId());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void removeEnvironment(Environment environment) {
        sync((connection) -> {
            try {
                PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM mineEnvironments WHERE id = ?"
                );
                statement.setInt(1, environment.getId());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void async(Consumer<Connection> callback) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(FlawMines.get(), () -> {
            sync(callback);
        });
    }

    public void sync(Consumer<Connection> callback) {
        lock.lock();
        try {
            callback.accept(getConnection());
        } finally {
            lock.unlock();
        }
    }
}
