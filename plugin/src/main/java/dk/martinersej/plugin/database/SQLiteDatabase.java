package dk.martinersej.plugin.database;

import dk.martinersej.plugin.FlawMines;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class SQLiteDatabase {

    private final Lock lock = new ReentrantLock(true);
    @Getter
    private Connection connection;

    public SQLiteDatabase() {
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

    @SneakyThrows
    public boolean isConnected() {
        return connection != null && !connection.isClosed();
    }

    public String getHost() {
        return "localhost";
    }

    public String getPort() {
        return "3306";
    }

    public String getDatabase() {
        return FlawMines.get().getName();
    }

    public String getPrefix() {
        return FlawMines.get().getDescription().getName();
    }

    public String getURL() {
        return String.format("jdbc:sqlite:%s%s%s.db", FlawMines.get().getDataFolder(), File.separator, getDatabase());
    }

    public String getDriver() {
        return "org.sqlite.JDBC";
    }

    public void createTables() {
        List<String> tables = new ArrayList<>();

        String minesTable = QueryBuilder.QueryTableBuilder.createTable("mines").
            values("id", "TEXT PRIMARY KEY NOT NULL").
            values("location", "TEXT NOT NULL").
                build();
        tables.add(minesTable);
        String blocksTable = QueryBuilder.QueryTableBuilder.createTable("mineBlocks").
            values("id", "AUTOINCREMENT PRIMARY KEY NOT NULL").
            values("mineId", "FOREIGN KEY REFERENCES mines(id) NOT NULL").
            values("data", "TEXT NOT NULL").
                build();
        tables.add(blocksTable);
        String environmentsTable = QueryBuilder.QueryTableBuilder.createTable("mineEnvironments").
                values("id", "AUTOINCREMENT PRIMARY KEY NOT NULL").
                values("mineId", "FOREIGN KEY REFERENCES mines(id) NOT NULL").
                values("type", "TEXT NOT NULL"). // class name
                values("data", "TEXT NOT NULL"). // serialized data
                build();
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

    public void async(Runnable task) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(FlawMines.get(), () -> {
            sync((connection) -> task.run());
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
