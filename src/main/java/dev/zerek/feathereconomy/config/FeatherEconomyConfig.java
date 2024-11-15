package dev.zerek.feathereconomy.config;

import dev.zerek.feathereconomy.FeatherEconomy;
import org.bukkit.configuration.file.FileConfiguration;

public class FeatherEconomyConfig {

    private final FeatherEconomy plugin;
    private final FileConfiguration config;

    private String mysqlUsername;
    private int inactiveThresholdDays;
    private String mysqlHost;
    private int mysqlPort;
    private String mysqlPassword;
    private String mysqlDatabase;
    private boolean mysqlEnabled;

    public FeatherEconomyConfig(FeatherEconomy plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
        this.config = this.plugin.getConfig();
        this.loadConfig();
    }

    private void loadConfig() {
        this.mysqlEnabled = config.getBoolean("settings.mysql.enabled");
        this.mysqlUsername = config.getString("settings.mysql.username");
        this.mysqlHost = config.getString("settings.mysql.host");
        this.mysqlPort = config.getInt("settings.mysql.port");
        this.mysqlPassword = config.getString("settings.mysql.password");
        this.mysqlDatabase = config.getString("settings.mysql.database");
        this.inactiveThresholdDays = config.getInt("settings.report.inactive-threshold-days", 30);
    }

    public String getMysqlUsername() {
        return mysqlUsername;
    }

    public String getMysqlHost() {
        return mysqlHost;
    }

    public int getMysqlPort() {
        return mysqlPort;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public String getMysqlDatabase() {
        return mysqlDatabase;
    }

    public boolean isMysqlEnabled() {
        return mysqlEnabled;
    }

    public int getInactiveThresholdDays() {
        return inactiveThresholdDays;
    }
}
