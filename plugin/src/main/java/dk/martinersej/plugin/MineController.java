package dk.martinersej.plugin;

import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.environment.Environment;
import dk.martinersej.plugin.mine.mineblock.MineBlock;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

        String minesTable = "" +
            "CREATE TABLE IF NOT EXISTS mines (" +
            "id TEXT PRIMARY KEY NOT NULL," +
            "world TEXT NOT NULL," + // where its located
            "region TEXT NOT NULL" + // region id or something
            ");" ;
        tables.add(minesTable);

        String blocksTable = "" +
            "CREATE TABLE IF NOT EXISTS mineBlocks (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "mineId TEXT NOT NULL," +
            "data TEXT NOT NULL," +
            "FOREIGN KEY (mineId) REFERENCES mines(id)" +
            ");" ;
        tables.add(blocksTable);

        String environmentsTable = "" +
            "CREATE TABLE IF NOT EXISTS mineEnvironments (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "mineId TEXT NOT NULL," +
            "type TEXT NOT NULL," +
            "data TEXT NOT NULL," +
            "FOREIGN KEY (mineId) REFERENCES mines(id)" +
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
                    "UPDATE mineBlocks SET data = ? WHERE mineId = ?"
                );
                for (MineBlock block : mine.getBlocks()) {
                    blockStatement.setString(1, block.serialize());
                    blockStatement.setString(2, mine.getName());
                    blockStatement.addBatch();
                }

                PreparedStatement environmentStatement = connection.prepareStatement(
                    "UPDATE mineEnvironments SET data = ? WHERE id = ? AND mineId = ?"
                );
                for (Environment environment : mine.getEnvironments()) {
                    environmentStatement.setString(1, environment.serialize());
                    environmentStatement.setInt(2, environment.getId());
                    environmentStatement.setString(3, mine.getName());
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

    public void deleteMine(String id) {
        sync((connection) -> {
            try {
                connection.createStatement().executeUpdate(
                    String.format("DELETE FROM mines WHERE id = '%s'", id)
                );
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
