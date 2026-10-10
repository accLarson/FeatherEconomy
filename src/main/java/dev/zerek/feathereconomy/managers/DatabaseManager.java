package dev.zerek.feathereconomy.managers;

import dev.zerek.feathereconomy.FeatherEconomy;
import dev.zerek.feathereconomy.config.FeatherEconomyConfig;

import java.io.File;
import java.sql.*;

public class DatabaseManager {

    private Connection connection;
    private long lastUsed;
    private final FeatherEconomy plugin;
    private final FeatherEconomyConfig config;
    private final boolean isMySQLEnabled;

    public DatabaseManager(FeatherEconomy plugin) {
        this.plugin = plugin;
        this.config = plugin.getFeatherEconomyConfig();
        this.isMySQLEnabled = this.config.isMysqlEnabled();
        this.initConnection();
        if (connection != null) {
            this.initTables();
        }
    }

    private void initConnection() {
        if (this.isMySQLEnabled) {
            this.initMySQLConnection();
        } else {
            this.initSQLiteConnection();
        }
    }

    private String getMySQLUrl() {
        return String.format("jdbc:mysql://%s:%d/%s?connectTimeout=10000&socketTimeout=30000", this.config.getMysqlHost(), this.config.getMysqlPort(), this.config.getMysqlDatabase());
    }

    private Connection connectMySQL() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException(exception);
        }
        return DriverManager.getConnection(this.getMySQLUrl(), this.config.getMysqlUsername(), this.config.getMysqlPassword());
    }

    private Connection connectSQLite() throws SQLException {
        File folder = new File(this.plugin.getDataFolder().getPath());
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder.getAbsolutePath() + File.separator + "feather_economy.db");
        return DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
    }

    private void initMySQLConnection(){
        try {
            this.connection = this.connectMySQL();
        } catch (SQLException exception) {
            plugin.getLogger().severe("Unable to initialize connection.");
            plugin.getLogger().severe("Ensure connection can be made with provided MySQL strings.");
            plugin.getLogger().severe("Connection URL: " + this.getMySQLUrl());
        }

    }

    private void initSQLiteConnection(){
        try {
            this.connection = this.connectSQLite();
        } catch (SQLException sQLException) {
            plugin.getLogger().severe("Unable to initialize connection.");
        }
    }

    public synchronized void reconnect() throws SQLException {
        this.close();
        if (this.isMySQLEnabled) {
            this.connection = this.connectMySQL();
        } else {
            this.connection = this.connectSQLite();
        }
    }

    public synchronized void close() {
        try {
            if (this.connection != null) {
                if (!this.connection.isClosed()){
                    this.connection.close();
                }
            }
        } catch (SQLException exception) {
            plugin.getLogger().severe("Unable to close connection.");
        }
    }

    private boolean existsTable(String table) {
        try {
            if (!connection.isClosed()) {
                ResultSet rs = connection.getMetaData().getTables(null, null, table, null);
                return rs.next();
            } else {
                return false;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Unable to query table metadata.");
            return false;
        }
    }

    private void initTables() {
        if (!this.existsTable("economy_accounts")) {
            plugin.getLogger().info("Creating economy_accounts table.");
            String query = "CREATE TABLE IF NOT EXISTS `economy_accounts` ("
                    + " `mojang_uuid` VARCHAR(255) PRIMARY KEY, "
                    + " `balance` double(64,2) NOT NULL DEFAULT 0, "
                    + " `date_created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + " `last_deposit_value` double(64,2) NOT NULL DEFAULT 0, "
                    + " `last_deposit_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + " `last_withdraw_value` double(64,2) NOT NULL DEFAULT 0, "
                    + " `last_withdraw_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);";
            try {
                if (!connection.isClosed()) {
                    connection.createStatement().execute(query);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Unable to create feather_clans table.");
            }
        }

        if (!this.existsTable("economy_transactions")) {
            plugin.getLogger().info("Creating economy_transactions table.");
            String idColumn = this.isMySQLEnabled
                    ? "`id` INT AUTO_INCREMENT PRIMARY KEY, "
                    : "`id` INTEGER PRIMARY KEY AUTOINCREMENT, ";
            String query = "CREATE TABLE IF NOT EXISTS `economy_transactions` ("
                    + idColumn
                    + " `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + " `type` VARCHAR(32) NOT NULL, "
                    + " `direction` VARCHAR(16) NOT NULL, "
                    + " `player_uuid` VARCHAR(36) NOT NULL, "
                    + " `counterparty_uuid` VARCHAR(36) NULL, "
                    + " `amount` DOUBLE NOT NULL, "
                    + " `balance_after` DOUBLE NOT NULL);";
            try {
                if (!connection.isClosed()) {
                    connection.createStatement().execute(query);
                    connection.createStatement().execute(
                            "CREATE INDEX IF NOT EXISTS idx_economy_transactions_player_ts "
                                    + "ON economy_transactions (player_uuid, timestamp);");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Unable to create economy_transactions table.");
            }
        }
    }

    public boolean isConnected() {
        return this.connection != null;
    }

    public synchronized boolean isValid() {
        try {
            return this.connection != null && this.connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    public synchronized Connection getConnection() throws SQLException {
        long now = System.currentTimeMillis();
        if (this.connection == null || this.connection.isClosed()
                || (now - this.lastUsed > 30000 && !this.connection.isValid(2))) {
            this.reconnect();
        }
        this.lastUsed = now;
        return this.connection;
    }

    public boolean executeUpdate(String update) {
        try {
            Statement statement = this.getConnection().createStatement();
            statement.executeUpdate(update);
            statement.close();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to execute update: ||| " + update + " |||");
            return false;
        }
    }

    public ResultSet executeQuery(String query) {
        try {
            Statement statement = this.getConnection().createStatement();
            statement.closeOnCompletion();
            return statement.executeQuery(query);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to execute query: ||| " + query + " |||");
            return null;
        }
    }
}
