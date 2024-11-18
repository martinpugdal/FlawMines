package dk.martinersej.plugin;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.environment.Environment;
import dk.martinersej.plugin.mine.mineblock.MineBlock;
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
        return "org.sqlite.JDBC" ;
    }

    public void createTables() {
        List<String> tables = new ArrayList<>();

        //todo: combine id and region into a single primary key
        String minesTable = "CREATE TABLE IF NOT EXISTS mines (" +
            "id TEXT NOT NULL," +
            "world TEXT NOT NULL," + // where its located
            "region TEXT NOT NULL, " + // the region name
            "PRIMARY KEY (id, region)" + // primary key is id and region
            ");" ;
        tables.add(minesTable);

        String blocksTable = "CREATE TABLE IF NOT EXISTS mineBlocks (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "mineId TEXT NOT NULL," +
            "data TEXT NOT NULL," +
            "FOREIGN KEY (mineId) REFERENCES mines(id) ON DELETE CASCADE" +
            ");" ;
        tables.add(blocksTable);

        String environmentsTable = "CREATE TABLE IF NOT EXISTS mineEnvironments (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "mineId TEXT NOT NULL," +
            "type TEXT NOT NULL," +
            "data TEXT NOT NULL," +
            "FOREIGN KEY (mineId) REFERENCES mines(id) ON DELETE CASCADE" +
            ");" ;
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
                    String.format("INSERT INTO mines (id, world, region) VALUES ('%s', '%s', '%s')", id, world, region)
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
            String query = " SELECT * FROM mines WHERE world = ? " +
                " LEFT JOIN mineBlocks mB ON mines.id = mB.mineId " +
                " LEFT JOIN mineEnvironments mE ON mines.id = mE.mineId; " ;
            try {
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, world.getName());

                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    String id = resultSet.getString("id"); // mine id = name
                    String regionName = resultSet.getString("region");

                    // worldguard support for getting a region
                    ProtectedRegion region = plugin.getWorldGuardInterface().getRegionManager(world).getRegion(regionName);
                    if (region == null) {
                        plugin.getLogger().warning("Mine region not found: " + regionName);
                        continue;
                    }

                    Mine mine = new Mine(id, region, world);
                    mines.put(region, mine);

                    // load the blocks and environments for the mine
                    do {
                        // load the blocks stuff here for mine

                        // load the environment stuff here for mine too
                    } while (resultSet.next());

                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
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
