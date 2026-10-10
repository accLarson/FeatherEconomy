package dev.zerek.feathereconomy.managers;

import dev.zerek.feathereconomy.FeatherEconomy;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTransientException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class StorageManager {

    private interface Task {
        void run() throws SQLException;
    }

    public static class LogEntry {
        volatile long id;
    }

    private final FeatherEconomy plugin;
    private final ConcurrentHashMap<UUID, Double> balances;
    private final DatabaseManager database;
    private final ExecutorService executor;
    private final Object pendingLock = new Object();
    private final Deque<Long> pendingTimes = new ArrayDeque<>();
    private volatile boolean offline;
    private long lastFailureLog;
    private boolean loaded;

    public StorageManager(FeatherEconomy plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabaseManager();
        this.balances = new ConcurrentHashMap<>();
        this.executor = Executors.newSingleThreadExecutor();
        this.init();
    }

    private void init() {
        String query = "SELECT mojang_uuid, balance FROM economy_accounts;";
        try (ResultSet results = database.executeQuery(query)) {
            if (results != null) {
                while (results.next()) {
                    balances.put(UUID.fromString(results.getString("mojang_uuid")), results.getDouble("balance"));
                }
                this.loaded = true;
            }
        } catch (SQLException | IllegalArgumentException e) {
            plugin.getLogger().severe("Failed to init storage.");
            e.printStackTrace();
        }
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    // Offline when a write failed or has waited over 2 seconds.
    public boolean isOffline() {
        if (this.offline) {
            return true;
        }
        synchronized (this.pendingLock) {
            return !this.pendingTimes.isEmpty() && System.currentTimeMillis() - this.pendingTimes.peekFirst() > 2000;
        }
    }

    // After shutdown, late writes run once on the calling thread.
    private void queue(Task task) {
        synchronized (this.pendingLock) {
            this.pendingTimes.addLast(System.currentTimeMillis());
        }
        try {
            this.executor.execute(() -> {
                if (this.runUntilSuccess(task)) {
                    this.finish();
                }
            });
        } catch (RejectedExecutionException e) {
            try {
                task.run();
            } catch (SQLException exception) {
                plugin.getLogger().severe("Failed to write to storage after shutdown: " + exception.getMessage());
            }
            this.finish();
        }
    }

    private void finish() {
        synchronized (this.pendingLock) {
            this.pendingTimes.removeFirst();
        }
    }

    public void submit(Runnable runnable) {
        try {
            this.executor.execute(runnable);
        } catch (RejectedExecutionException e) {
            // Shut down, nothing to run
        }
    }

    private boolean runUntilSuccess(Task task) {
        long delay = 1000;
        while (true) {
            try {
                task.run();
                this.offline = false;
                return true;
            } catch (SQLException e) {
                // A bad statement on a live connection is skipped so it can't block the queue.
                if (!(e instanceof SQLTransientException) && database.isValid()) {
                    plugin.getLogger().severe("Failed to write to storage, write skipped: " + e.getMessage());
                    return true;
                }
                this.offline = true;
                long now = System.currentTimeMillis();
                if (now - this.lastFailureLog >= 30000) {
                    this.lastFailureLog = now;
                    plugin.getLogger().severe("Failed to write to storage, retrying: " + e.getMessage());
                }
            }
            try {
                Thread.sleep(delay);
                delay = Math.min(delay * 2, 5000);
                database.reconnect();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                plugin.getLogger().severe("Storage write interrupted and abandoned.");
                return false;
            } catch (SQLException e) {
                // Retried on the next attempt
            }
        }
    }

    public void shutdown() {
        this.executor.shutdown();
        try {
            if (!this.executor.awaitTermination(30, TimeUnit.SECONDS)) {
                int dropped = this.executor.shutdownNow().size();
                plugin.getLogger().severe("Storage queue did not finish in 30 seconds, " + dropped + " queued writes dropped.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean hasAccount(UUID uuid) {
        return balances.containsKey(uuid);
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
        String query = "SELECT COUNT(*) + 1 as player_rank FROM economy_accounts WHERE balance > " + playerBalance;
        try (ResultSet results = database.executeQuery(query)) {
            if (results != null && results.next()) {
                return results.getInt("player_rank");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to calculate wealth ranking for: " + uuid);
        }
        return 0;
    }


    public double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, 0.0);
    }

    public boolean createAccount(UUID uuid) {
        if (balances.putIfAbsent(uuid, 0.0) != null) {
            return false;
        }
        String sql = "INSERT INTO economy_accounts (mojang_uuid) VALUES (?)";
        this.queue(() -> {
            try (PreparedStatement ps = database.getConnection().prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            }
        });
        return true;
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (balances.computeIfPresent(uuid, (key, balance) -> Math.round((balance - amount) * 100) / 100.0) == null) {
            return false;
        }
        this.queueBalanceWrite(uuid, amount, "withdraw");
        return true;
    }

    public boolean deposit(UUID uuid, double amount) {
        if (balances.computeIfPresent(uuid, (key, balance) -> Math.round((balance + amount) * 100) / 100.0) == null) {
            return false;
        }
        this.queueBalanceWrite(uuid, amount, "deposit");
        return true;
    }

    private void queueBalanceWrite(UUID uuid, double amount, String kind) {
        String sql = "UPDATE economy_accounts SET balance = ?, last_" + kind + "_value = ?, last_" + kind + "_date = CURRENT_TIMESTAMP WHERE mojang_uuid = ?";
        this.queue(() -> {
            try (PreparedStatement ps = database.getConnection().prepareStatement(sql)) {
                ps.setDouble(1, this.getBalance(uuid));
                ps.setDouble(2, amount);
                ps.setString(3, uuid.toString());
                ps.executeUpdate();
            }
        });
    }

    // ----- Transaction log -----

    public record Transaction(
            long timestamp,
            String type,
            String direction,
            UUID playerUuid,
            UUID counterpartyUuid,
            double amount,
            double balanceAfter
    ) {}

    public LogEntry logTransaction(String type, String direction, UUID playerUuid, UUID counterpartyUuid, double amount, double balanceAfter) {
        LogEntry entry = new LogEntry();
        String sql = "INSERT INTO economy_transactions (type, direction, player_uuid, counterparty_uuid, amount, balance_after) VALUES (?, ?, ?, ?, ?, ?)";
        this.queue(() -> {
            try (PreparedStatement ps = database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, type);
                ps.setString(2, direction);
                ps.setString(3, playerUuid.toString());
                if (counterpartyUuid != null) ps.setString(4, counterpartyUuid.toString());
                else ps.setNull(4, Types.VARCHAR);
                ps.setDouble(5, amount);
                ps.setDouble(6, balanceAfter);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        entry.id = keys.getLong(1);
                    }
                }
            }
        });
        return entry;
    }

    public void describe(LogEntry entry, String label, UUID counterpartyUuid) {
        String sql = "UPDATE economy_transactions SET type = ?, counterparty_uuid = ? WHERE id = ?";
        this.queue(() -> {
            if (entry.id <= 0) {
                return;
            }
            try (PreparedStatement ps = database.getConnection().prepareStatement(sql)) {
                ps.setString(1, label);
                if (counterpartyUuid != null) ps.setString(2, counterpartyUuid.toString());
                else ps.setNull(2, Types.VARCHAR);
                ps.setLong(3, entry.id);
                ps.executeUpdate();
            }
        });
    }

    public void logSelf(String type, UUID uuid, double amount, double balanceAfter) {
        logTransaction(type, "SELF", uuid, null, amount, balanceAfter);
    }

    public void logTransfer(UUID senderUuid, UUID recipientUuid, double amount, double senderBalanceAfter, double recipientBalanceAfter) {
        logTransaction("TRANSFER", "SENT", senderUuid, recipientUuid, amount, senderBalanceAfter);
        logTransaction("TRANSFER", "RECEIVED", recipientUuid, senderUuid, amount, recipientBalanceAfter);
    }

    public void logAdminAction(String type, UUID adminUuid, UUID targetUuid, double amount, double targetBalanceAfter) {
        if (adminUuid != null && adminUuid.equals(targetUuid)) {
            logTransaction(type, "SELF", adminUuid, null, amount, targetBalanceAfter);
        } else {
            logTransaction(type, "RECEIVED", targetUuid, adminUuid, amount, targetBalanceAfter);
        }
    }

    public List<Transaction> getTransactions(UUID uuid, int page, int pageSize) {
        List<Transaction> results = new ArrayList<>();
        int offset = Math.max(0, (page - 1) * pageSize);
        String sql = "SELECT timestamp, type, direction, player_uuid, counterparty_uuid, amount, balance_after "
                + "FROM economy_transactions WHERE player_uuid = ? AND direction != 'PERFORMED' "
                + "ORDER BY timestamp DESC, id DESC LIMIT ? OFFSET ?";
        try (PreparedStatement ps = database.getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, pageSize);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("timestamp");
                    String cpStr = rs.getString("counterparty_uuid");
                    UUID counterparty = cpStr != null ? UUID.fromString(cpStr) : null;
                    results.add(new Transaction(
                            ts != null ? ts.getTime() : 0L,
                            rs.getString("type"),
                            rs.getString("direction"),
                            UUID.fromString(rs.getString("player_uuid")),
                            counterparty,
                            rs.getDouble("amount"),
                            rs.getDouble("balance_after")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to fetch transactions for " + uuid + ": " + e.getMessage());
        }
        return results;
    }

    public int getTransactionCount(UUID uuid) {
        String sql = "SELECT COUNT(*) FROM economy_transactions WHERE player_uuid = ? AND direction != 'PERFORMED'";
        try (PreparedStatement ps = database.getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to count transactions for " + uuid + ": " + e.getMessage());
        }
        return 0;
    }
}
