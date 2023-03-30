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
        try(ResultSet results = database.executeQuery(query)){
            if(results != null) {
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
        if(accounts.contains(uuid)) {
            return true;
        } else {
            try(ResultSet results = database.executeQuery(query)){
                if(results != null && results.next()) {
                    accounts.add(UUID.fromString(results.getString("mojang_uuid")));
                    return true;
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed query storage for account (hasAccount): " + uuid);
            }
        }
        return false;
    }

    public double getBalance(UUID uuid) {
        String query = "SELECT balance FROM economy_accounts WHERE mojang_uuid = '" + uuid.toString() + "' LIMIT 1;";
        try(ResultSet results = database.executeQuery(query)){
            if(results != null && results.next()){
                return results.getDouble("balance");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed query storage for account (getBalance): " + uuid);
        }
        return 0.0f;
    }

    public boolean createAccount(UUID uuid) {
        boolean update = database.executeUpdate("INSERT INTO economy_accounts (mojang_uuid) VALUES ('" + uuid.toString() + "');");
        if(update) {
            accounts.add(uuid);
        }
        return update;
    }

    public boolean withdraw(UUID uuid, double amount) {
        double balance = this.getBalance(uuid);
        double updatedBalance = balance - amount;
        Date date = new Date();
        return database.executeUpdate("UPDATE economy_accounts SET balance = " + updatedBalance + ", last_withdraw_value = " + amount + ", last_withdraw_date = CURRENT_TIMESTAMP where mojang_uuid = '" + uuid + "';");
    }

    public boolean deposit(UUID uuid, double amount) {
        double balance = this.getBalance(uuid);
        double updatedBalance = balance + amount;
        Date date = new Date();
        return database.executeUpdate("UPDATE economy_accounts SET balance = " + updatedBalance + ", last_deposit_value = " + amount + ", last_deposit_date = CURRENT_TIMESTAMP where mojang_uuid = '" + uuid + "';");
    }

}
