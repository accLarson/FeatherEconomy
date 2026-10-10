package dev.zerek.feathereconomy;

import dev.zerek.feathereconomy.api.FeatherEconomyResponse;
import dev.zerek.feathereconomy.managers.StorageManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

public class EconomyServiceProvider implements Economy {

    private final FeatherEconomy plugin;
    private final DecimalFormat formatter = new DecimalFormat("0.00");

    public EconomyServiceProvider(FeatherEconomy plugin) {
        this.plugin = plugin;
        formatter.setRoundingMode(RoundingMode.DOWN);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return plugin.getName();
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return -1;
    }

    @Override
    public String format(double amount) {
        return formatter.format(amount);
    }

    @Override
    public String currencyNamePlural() {
        return "Lapis";
    }

    @Override
    public String currencyNameSingular() {
        return this.currencyNamePlural();
    }

    /* Unsupported operation */
    @Override
    public boolean hasAccount(String playerName) {
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return this.plugin.getStorage().hasAccount(player.getUniqueId());
    }

    /* No support for multiple worlds */
    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return false;
    }

    /* No support for multiple worlds */
    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return false;
    }

    /* Unsupported operation */
    @Override
    public double getBalance(String playerName) {
        return 0;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        if (!this.hasAccount(player)){
//            this.createPlayerAccount(player);
            return 0;
        }
        return plugin.getStorage().getBalance(player.getUniqueId());
    }

    /* No support for multiple worlds */
    @Override
    public double getBalance(String playerName, String world) {
        return 0;
    }

    /* No support for multiple worlds */
    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return 0;
    }

    /* Unsupported operation */
    @Override
    public boolean has(String playerName, double amount) {
        return false;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return !this.plugin.getStorage().isOffline() && this.getBalance(player) >= amount;
    }

    /* No support for multiple worlds */
    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return false;
    }

    /* No support for multiple worlds */
    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return false;
    }

    /* Unsupported operation */
    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return null;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        double balance = this.getBalance(player);
        if (amount < 0 || Double.isNaN(amount)) {
            return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.FAILURE, "");
        }
        if (this.has(player, amount)) {
            if (this.plugin.getStorage().withdraw(player.getUniqueId(), amount)) {
                StorageManager.LogEntry entry = this.logExternal("WITHDRAW", player, amount);
                return new FeatherEconomyResponse(amount, balance - amount, EconomyResponse.ResponseType.SUCCESS, "", entry);
            }
        }
        return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.FAILURE, "");
    }

    /* No support for multiple worlds */
    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return null;
    }

    /* No support for multiple worlds */
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return null;
    }

    /* Unsupported operation */
    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0 || Double.isNaN(amount)) {
            return new EconomyResponse(amount, this.getBalance(player), EconomyResponse.ResponseType.FAILURE, "");
        }
        double balance = 0;
        if (!this.hasAccount(player)) {
            this.createPlayerAccount(player);
        } else {
            balance = this.getBalance(player);
        }
        if (this.plugin.getStorage().deposit(player.getUniqueId(), amount)) {
            StorageManager.LogEntry entry = this.logExternal("DEPOSIT", player, amount);
            return new FeatherEconomyResponse(amount, balance + amount, EconomyResponse.ResponseType.SUCCESS, "", entry);
        } else {
            return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.FAILURE, "");
        }
    }

    /* No support for multiple worlds */
    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return null;
    }

    /* No support for multiple worlds */
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return null;
    }

    /* No support for banks */
    @Override
    public EconomyResponse createBank(String name, String player) {
        return null;
    }

    /* No support for banks */
    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return null;
    }

    /* No support for banks */
    @Override
    public EconomyResponse deleteBank(String name) {
        return null;
    }

    /* No support for banks */
    @Override
    public EconomyResponse bankBalance(String name) {
        return null;
    }

    /* No support for banks */
    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return null;
    }

    /* No support for banks */
    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return null;
    }

    /* No support for banks */
    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return null;
    }

    /* No support for banks */
    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return null;
    }

    /* No support for banks */
    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return null;
    }

    /* No support for banks */
    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return null;
    }

    /* No support for banks */
    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return null;
    }

    /* No support for banks */
    @Override
    public List<String> getBanks() {
        return null;
    }

    /* Unsupported operation */
    @Override
    public boolean createPlayerAccount(String playerName) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        if (!this.hasAccount(player)) {
            return this.plugin.getStorage().createAccount(player.getUniqueId());
        }
        return false;
    }

    /* Unsupported operation */
    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return false;
    }

    /* Unsupported operation */
    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return false;
    }

    // Logs payments made by other plugins. This plugin's commands log their own.
    private StorageManager.LogEntry logExternal(String direction, OfflinePlayer player, double amount) {
        List<Class<?>> callers = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames ->
                frames.map(StackWalker.StackFrame::getDeclaringClass)
                        .dropWhile(c -> c == EconomyServiceProvider.class)
                        .toList());
        if (!callers.isEmpty() && callers.get(0).getPackageName().startsWith("dev.zerek.feathereconomy")) {
            return null;
        }
        String name = "Other";
        for (Class<?> caller : callers) {
            if (caller.getPackageName().startsWith("net.milkbowl.vault")) {
                continue;
            }
            try {
                name = JavaPlugin.getProvidingPlugin(caller).getName();
            } catch (IllegalArgumentException | IllegalStateException e) {
                name = "Other";
            }
            break;
        }
        if (name.length() > 32) {
            name = name.substring(0, 32);
        }
        return this.plugin.getStorage().logTransaction(name, direction, player.getUniqueId(), null, amount, this.getBalance(player));
    }
}
