package dev.zerek.feathereconomy.managers;

import dev.zerek.feathereconomy.FeatherEconomy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class StorageManager {

    private final FeatherEconomy plugin;
    private final Set<UUID> accounts;
    private final DatabaseManager database;

    public StorageManager(FeatherEconomy plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabaseManager();
        this.accounts = new HashSet<>();
        this.init();
    }

    private void init() {
        String query = "SELECT * FROM economy_accounts;";
        try (ResultSet results = database.executeQuery(query)) {
            if (results != null) {
                while (results.next()) {
                    accounts.add(UUID.fromString(results.getString("mojang_uuid")));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to init storage.");
            e.printStackTrace();
        }
    }

    public boolean hasAccount(UUID uuid) {
        String query = "SELECT * FROM economy_accounts WHERE mojang_uuid = '" + uuid.toString() + "' LIMIT 1;";
        if (accounts.contains(uuid)) {
            return true;
        } else {
            try (ResultSet results = database.executeQuery(query)) {
                if (results != null && results.next()) {
                    accounts.add(UUID.fromString(results.getString("mojang_uuid")));
                    return true;
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed query storage for account (hasAccount): " + uuid);
            }
        }
        return false;
    }

    public int getTotalLapis(boolean activeOnly) {
        String query = "SELECT SUM(balance) as total FROM economy_accounts";
        if (activeOnly) {
            query += " WHERE last_deposit_date >= DATE_SUB(NOW(), INTERVAL " + 
                    plugin.getFeatherEconomyConfig().getInactiveThresholdDays() + 
                    " DAY) OR last_withdraw_date >= DATE_SUB(NOW(), INTERVAL " + 
                    plugin.getFeatherEconomyConfig().getInactiveThresholdDays() + " DAY)";
        }
        try (ResultSet results = database.executeQuery(query)) {
            if (results != null && results.next()) {
                return (int) results.getDouble("total");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get " + (activeOnly ? "active " : "") + "total lapis amount");
        }
        return 0;
    }
    

    public int getActiveAccounts() {
        int days = plugin.getFeatherEconomyConfig().getInactiveThresholdDays();
        return getAccountCountByCondition(
            "last_deposit_date >= DATE_SUB(NOW(), INTERVAL " + days + " DAY) OR " +
            "last_withdraw_date >= DATE_SUB(NOW(), INTERVAL " + days + " DAY)"
        );
    }

    public int getActiveAccountsPeriod(int hours) {
        String query = "SELECT COUNT(*) as count FROM economy_accounts WHERE " +
                      "last_deposit_date >= DATE_SUB(NOW(), INTERVAL " + hours + " HOUR) OR " +
                      "last_withdraw_date >= DATE_SUB(NOW(), INTERVAL " + hours + " HOUR)";
        try (ResultSet results = database.executeQuery(query)) {
            if (results != null && results.next()) {
                return results.getInt("count");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get active accounts for last " + hours + " hours");
        }
        return 0;
    }

    private int getAccountCountByCondition(String condition) {
        String query = "SELECT COUNT(*) as count FROM economy_accounts WHERE " + condition;
        try (ResultSet results = database.executeQuery(query)) {
            if (results != null && results.next()) {
                return results.getInt("count");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get total non-zero accounts");
        }
        return 0;
    }

    public int getTotalAccounts() {
        String query = "SELECT COUNT(*) as count FROM economy_accounts";
        try (ResultSet results = database.executeQuery(query)) {
            if (results != null && results.next()) {
                return results.getInt("count");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get total accounts count");
        }
        return 0;
    }

    public double getTopNWealth(int n, String mode) {
        String whereClause = "";
        
        if (mode.equals("active")) {
            whereClause = " WHERE last_deposit_date >= DATE_SUB(NOW(), INTERVAL " + 
                         plugin.getFeatherEconomyConfig().getInactiveThresholdDays() + 
                         " DAY) OR last_withdraw_date >= DATE_SUB(NOW(), INTERVAL " + 
                         plugin.getFeatherEconomyConfig().getInactiveThresholdDays() + " DAY)";
        }
        
        String countQuery = "SELECT SUM(balance) as total FROM economy_accounts" + whereClause;
        
        try (ResultSet totalResult = database.executeQuery(countQuery)) {
            if (!totalResult.next() || totalResult.getDouble("total") <= 0) {
                return 0.0;
            }
            double totalLapis = totalResult.getDouble("total");

            String query = "SELECT SUM(balance) as top_sum FROM (SELECT balance FROM economy_accounts" + 
                         whereClause + " ORDER BY balance DESC LIMIT " + n + ") as top_n";
            try (ResultSet resultSet = database.executeQuery(query)) {
                return resultSet.next() ? (resultSet.getDouble("top_sum") / totalLapis) * 100.0 : 0.0;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to calculate top " + n + " wealth");
            return 0.0;
        }
    }

    public double getAverageBalance() {
        String query = "SELECT AVG(balance) as avg FROM economy_accounts";
        try (ResultSet results = database.executeQuery(query)) {
            if (results != null && results.next()) {
                return results.getDouble("avg");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get average balance");
        }
        return 0.0;
    }

    public double getAverageBalance(boolean activeOnly) {
        String query = "SELECT AVG(balance) as avg FROM economy_accounts WHERE " +
                      "last_deposit_date >= DATE_SUB(NOW(), INTERVAL " + plugin.getFeatherEconomyConfig().getInactiveThresholdDays() + " DAY) OR " +
                      "last_withdraw_date >= DATE_SUB(NOW(), INTERVAL " + plugin.getFeatherEconomyConfig().getInactiveThresholdDays() + " DAY)";
        try (ResultSet results = database.executeQuery(query)) {
            if (results != null && results.next()) {
                return results.getDouble("avg");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get average balance");
        }
        return 0.0;
    }

    public int getWealthRanking(UUID uuid) {
        double playerBalance = getBalance(uuid);
        String query = "SELECT COUNT(*) + 1 as rank FROM economy_accounts WHERE balance > " + playerBalance;
        try (ResultSet results = database.executeQuery(query)) {
            if (results != null && results.next()) {
                return results.getInt("rank");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to calculate wealth ranking for: " + uuid);
        }
        return 0;
    }


    public double getBalance(UUID uuid) {
        String query = "SELECT balance FROM economy_accounts WHERE mojang_uuid = '" + uuid.toString() + "' LIMIT 1;";
        try (ResultSet results = database.executeQuery(query)) {
            if (results != null && results.next()) {
                return results.getDouble("balance");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed query storage for account (getBalance): " + uuid);
        }
        return 0.0f;
    }

    public boolean createAccount(UUID uuid) {
        boolean update = database.executeUpdate("INSERT INTO economy_accounts (mojang_uuid) VALUES ('" + uuid.toString() + "');");
        if (update) {
            accounts.add(uuid);
        }
        return update;
    }

    public boolean withdraw(UUID uuid, double amount) {
        double balance = this.getBalance(uuid);
        double updatedBalance = balance - amount;
        return database.executeUpdate("UPDATE economy_accounts SET balance = " + updatedBalance + ", last_withdraw_value = " + amount + ", last_withdraw_date = CURRENT_TIMESTAMP where mojang_uuid = '" + uuid + "';");
    }

    public boolean deposit(UUID uuid, double amount) {
        double balance = this.getBalance(uuid);
        double updatedBalance = balance + amount;
        return database.executeUpdate("UPDATE economy_accounts SET balance = " + updatedBalance + ", last_deposit_value = " + amount + ", last_deposit_date = CURRENT_TIMESTAMP where mojang_uuid = '" + uuid + "';");
    }

}
