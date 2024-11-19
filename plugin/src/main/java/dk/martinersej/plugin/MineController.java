package dk.martinersej.plugin;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.environment.Environment;
import dk.martinersej.plugin.mine.MineBlock;
import org.bukkit.Bukkit;
import org.bukkit.World;

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
            "fillmode INT(1) DEFAULT 0" + // if the mine is in fillmode
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

    public void createMine(String id, World world, String region) {
        sync((connection) -> {
            try {
                connection.createStatement().executeUpdate(
                    String.format("INSERT INTO mines (id, world, region) VALUES ('%s', '%s', '%s')", id, world.getName(), region)
                );
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

                    // worldguard support for getting a region
                    ProtectedRegion region = plugin.getWorldGuardInterface().getRegionManager(world).getRegion(regionName);
                    if (region == null) {
                        plugin.getLogger().warning("Mine region not found: " + regionName);
                        continue;
                    }

                    Mine mine = new Mine(mineId, region, world, false);
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
                        Environment environment = Environment.deserialize(environmentResultSet.getString("environmentData"));
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

    public void saveMine(Mine mine) {
        sync((connection) -> {
            try {
                PreparedStatement statement = connection.prepareStatement(
                    "UPDATE mines SET world = ?, region = ? WHERE id = ?"
                );
                statement.setString(1, mine.getWorld().getName());
                statement.setString(2, mine.getRegion().getProtectedRegion().getId());
                statement.setString(3, mine.getName());

                PreparedStatement blockStatement = connection.prepareStatement(
                    "UPDATE mineBlocks SET data = ? WHERE id = ?" // mineId is not needed for update, only insert
                );
                for (MineBlock block : mine.getBlocks()) {
                    blockStatement.setInt(1, block.getId());
                    blockStatement.setString(2, block.serialize());
                    blockStatement.addBatch();
                }

                PreparedStatement environmentStatement = connection.prepareStatement(
                    "UPDATE mineEnvironments SET data = ? WHERE id = ?" // mineId is not needed for update, only insert
                );
                for (Environment environment : mine.getEnvironments()) {
                    environmentStatement.setString(1, environment.serialize());
                    environmentStatement.setInt(2, environment.getId());
                    environmentStatement.addBatch();
                }

                connection.setAutoCommit(false);
                statement.executeUpdate();
                blockStatement.executeBatch();
                environmentStatement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
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
