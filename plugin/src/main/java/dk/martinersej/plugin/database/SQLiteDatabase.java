package dk.martinersej.plugin.database;

import dk.martinersej.plugin.FlawMines;
import lombok.Getter;
import lombok.SneakyThrows;

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
            FlawMines.getInstance().getLogger().info("Connected to database");
        } else {
            FlawMines.getInstance().getLogger().severe("Failed to connect to database");
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
        return FlawMines.getInstance().getName();
    }

    public String getPrefix() {
        return FlawMines.getInstance().getDescription().getName();
    }

    public String getURL() {
        return String.format("jdbc:sqlite:%s%s%s.db", FlawMines.getInstance().getDataFolder(), File.separator, getDatabase());
    }

    public String getDriver() {
        return "org.sqlite.JDBC";
    }

    public void createTables() {
        List<String> tables = new ArrayList<>();

        String walletTable = QueryBuilder.QueryTableBuilder.createTable("mines").
            values("uuid", "VARCHAR(36)", Constraint.PRIMARY_KEY, Constraint.NOT_NULL).
            values("balance", "REAL", Constraint.NOT_NULL).
            build();
        tables.add(walletTable);

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
        FlawMines.getInstance().getServer().getScheduler().runTaskAsynchronously(FlawMines.getInstance(), () -> {
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
