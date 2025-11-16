package dev.zerek.feathereconomy;

import dev.zerek.feathereconomy.commands.*;
import dev.zerek.feathereconomy.config.FeatherEconomyConfig;
import dev.zerek.feathereconomy.config.FeatherEconomyMessages;
import dev.zerek.feathereconomy.listeners.EntityDropItemListener;
import dev.zerek.feathereconomy.listeners.VillagerAcquireTradeListener;
import dev.zerek.feathereconomy.managers.DatabaseManager;
import dev.zerek.feathereconomy.managers.StorageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class FeatherEconomy extends JavaPlugin {

    private DatabaseManager database;
    private FeatherEconomyConfig config;
    private FeatherEconomyMessages messages;
    private EconomyServiceProvider economy;
    private StorageManager storage;

    @Override
    public void onEnable() {

        this.config = new FeatherEconomyConfig(this);
        this.messages = new FeatherEconomyMessages(this);
        this.economy = new EconomyServiceProvider(this);
        this.database = new DatabaseManager(this);
        this.storage = new StorageManager(this);

        this.getServer().getPluginManager().registerEvents(new VillagerAcquireTradeListener(), this);
        this.getServer().getPluginManager().registerEvents(new EntityDropItemListener(), this);

        this.getCommand("balance").setExecutor(new BalanceCommand(this));
        this.getCommand("balance").setTabCompleter(new BalanceTabCompleter());
        this.getCommand("deposit").setExecutor(new DepositCommand(this));
        this.getCommand("deposit").setTabCompleter(new DepositTabCompleter());
        this.getCommand("transfer").setExecutor(new TransferCommand(this));
        this.getCommand("transfer").setTabCompleter(new TransferTabCompleter());
        this.getCommand("withdraw").setExecutor(new WithdrawCommand(this));
        this.getCommand("withdraw").setTabCompleter(new WithdrawTabCompleter(this));
        this.getCommand("managebalance").setExecutor(new ManageBalanceCommand(this));
        this.getCommand("managebalance").setTabCompleter(new ManageBalanceTabCompleter());
        this.getCommand("economyreport").setExecutor(new EconomyReportCommand(this));
        this.getCommand("economyreport").setTabCompleter(new EconomyReportTabCompleter());

        this.getServer().getServicesManager().register(Economy.class, this.economy, this, ServicePriority.Normal);

        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);

        if (provider == null) this.getLogger().severe("Unable to hook into Vault.");
    }

    @Override
    public void onDisable() {

        if (this.database != null) this.database.close();
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
}
