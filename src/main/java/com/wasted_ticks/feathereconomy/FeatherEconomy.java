package com.wasted_ticks.feathereconomy;

import com.wasted_ticks.feathereconomy.commands.BalanceCommand;
import com.wasted_ticks.feathereconomy.commands.DepositCommand;
import com.wasted_ticks.feathereconomy.commands.TransferCommand;
import com.wasted_ticks.feathereconomy.commands.WithdrawCommand;
import com.wasted_ticks.feathereconomy.commands.completers.DepositCompleter;
import com.wasted_ticks.feathereconomy.commands.completers.TransferCompleter;
import com.wasted_ticks.feathereconomy.commands.completers.WithdrawCompleter;
import com.wasted_ticks.feathereconomy.config.FeatherEconomyConfig;
import com.wasted_ticks.feathereconomy.config.FeatherEconomyMessages;
import com.wasted_ticks.feathereconomy.listeners.EntityDropItemListener;
import com.wasted_ticks.feathereconomy.listeners.VillagerAcquireTradeListener;
import com.wasted_ticks.feathereconomy.managers.DatabaseManager;
import com.wasted_ticks.feathereconomy.managers.StorageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class FeatherEconomy extends JavaPlugin {

    private FeatherEconomy plugin;
    private DatabaseManager database;
    private FeatherEconomyConfig config;
    private FeatherEconomyMessages messages;
    private EconomyServiceProvider economy;
    private StorageManager storage;

    private static final Logger logger = Logger.getLogger("Minecraft");

    @Override
    public void onEnable() {
        this.plugin = this;

        this.config = new FeatherEconomyConfig(plugin);
        this.messages = new FeatherEconomyMessages(plugin);

        this.economy = new EconomyServiceProvider(plugin);
        this.database = new DatabaseManager(plugin);
        this.storage = new StorageManager(plugin);

        this.registerListeners();
        this.registerCommands();

        this.getServer().getServicesManager().register(Economy.class, this.economy, this, ServicePriority.Normal);

        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        if(provider == null) {
            plugin.getLog().severe("[FeatherEconomy] Unable to hook into Vault.");
        }
    }

    @Override
    public void onDisable() {
        if(this.database != null) {
            this.database.close();
        }
    }

    private void registerListeners() {
        this.getServer().getPluginManager().registerEvents(new VillagerAcquireTradeListener(), this);
        this.getServer().getPluginManager().registerEvents(new EntityDropItemListener(), this);
    }

    private void registerCommands() {
        this.getCommand("balance").setExecutor(new BalanceCommand(this));

        this.getCommand("deposit").setExecutor(new DepositCommand(this));
        this.getCommand("deposit").setTabCompleter(new DepositCompleter(this));

        this.getCommand("transfer").setExecutor(new TransferCommand(this));
        this.getCommand("transfer").setTabCompleter(new TransferCompleter(this));

        this.getCommand("withdraw").setExecutor(new WithdrawCommand(this));
        this.getCommand("withdraw").setTabCompleter(new WithdrawCompleter(this));


    }

    public DatabaseManager getDatabaseManager() {
        return database;
    }

    public FeatherEconomyConfig getFeatherEconomyConfig() {
        return config;
    }

    public FeatherEconomyMessages getFeatherEconomyMessages() {
        return messages;
    }

    public EconomyServiceProvider getEconomy() {
        return this.economy;
    }

    public StorageManager getStorage() { return this.storage; }

    public Logger getLog() {
        return logger;
    }

}
