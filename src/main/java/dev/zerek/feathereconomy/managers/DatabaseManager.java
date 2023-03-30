package dev.zerek.feathereconomy.managers;

import dev.zerek.feathereconomy.FeatherEconomy;
import dev.zerek.feathereconomy.config.FeatherEconomyConfig;

import javax.xml.crypto.Data;
import java.io.File;
import java.sql.*;
import java.util.Properties;

public class DatabaseManager {

    private static Connection connection;
    private final FeatherEconomy plugin;
    private final FeatherEconomyConfig config;
    private final boolean isMySQLEnabled;

    public DatabaseManager(FeatherEconomy plugin) {
        this.plugin = plugin;
        this.config = plugin.getFeatherEconomyConfig();
        this.isMySQLEnabled = this.config.isMysqlEnabled();
        this.initConnection();
        if(connection != null) {
            this.initTables();
        }
    }

    private void initConnection() {
        if(this.isMySQLEnabled) {
            this.initMySQLConnection();
        } else {
            this.initSQLiteConnection();
        }
    }

    private void initMySQLConnection(){
        FeatherEconomyConfig config = this.plugin.getFeatherEconomyConfig();

        String host = config.getMysqlHost();
        int port = config.getMysqlPort();
        String database = config.getMysqlDatabase();

        String url = String.format("jdbc:mysql://%s:%d/%s", host, port, database);

        String username = config.getMysqlUsername();
        String password = config.getMysqlPassword();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            DatabaseManager.connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException | ClassNotFoundException exception) {
            plugin.getLogger().severe("Unable to initialize connection.");
            plugin.getLogger().severe("Ensure connection can be made with provided MySQL strings.");
            plugin.getLogger().severe("Connection URL: " + url);
            plugin.getLogger().severe("Defaulting to local SQLite storage.");
            this.initSQLiteConnection();
        }

    }

    private void initSQLiteConnection(){
        File folder = new File(this.plugin.getDataFolder().getPath());
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder.getAbsolutePath() + File.separator + "feather_economy.db");
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        } catch (SQLException sQLException) {
            plugin.getLogger().severe("Unable to initialize connection.");
        }
    }

    public void close() {
        try {
            if(DatabaseManager.connection != null) {
                if(!DatabaseManager.connection.isClosed()){
                    DatabaseManager.connection.close();
                }
            }
        } catch (SQLException exception) {
            plugin.getLogger().severe("Unable to close connection.");
        }
    }

    private boolean existsTable(String table) {
        try {
            if(!connection.isClosed()) {
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
        if(!this.existsTable("economy_accounts")) {
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
                if(!connection.isClosed()) {
                    connection.createStatement().execute(query);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Unable to create feather_clans table.");
            }
        }
    }

    public boolean executeUpdate(String update) {
        if(DatabaseManager.connection == null) {
            return false;
        }
        Statement statement;
        try {
            statement = DatabaseManager.connection.createStatement();
            statement.executeUpdate(update);
            statement.close();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to execute update: ||| " + update + " |||");
            return false;
        }
    }

    public ResultSet executeQuery(String query) {
        if(DatabaseManager.connection == null) {
            return null;
        }
        try {
            return DatabaseManager.connection.createStatement().executeQuery(query);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to execute query: ||| " + query + " |||");
            return null;
        }
    }
}
