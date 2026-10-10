package dev.zerek.feathereconomy;

import dev.zerek.feathereconomy.commands.*;
import dev.zerek.feathereconomy.config.FeatherEconomyConfig;
import dev.zerek.feathereconomy.config.FeatherEconomyMessages;
import dev.zerek.feathereconomy.listeners.EntityDropItemListener;
import dev.zerek.feathereconomy.listeners.VillagerAcquireTradeListener;
import dev.zerek.feathereconomy.managers.DatabaseManager;
import dev.zerek.feathereconomy.managers.StorageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

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
        if (!this.database.isConnected()) {
            this.getLogger().severe("No database connection, disabling FeatherEconomy.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.storage = new StorageManager(this);
        if (!this.storage.isLoaded()) {
            this.getLogger().severe("Failed to load accounts, disabling FeatherEconomy.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

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
        this.getCommand("lapislog").setExecutor(new LapisLogCommand(this));
        this.getCommand("lapislog").setTabCompleter(new LapisLogTabCompleter());

        this.getServer().getServicesManager().register(Economy.class, this.economy, this, ServicePriority.Normal);

        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);

        if (provider == null) this.getLogger().severe("Unable to hook into Vault.");

        this.printBanner();
    }

    private void printBanner() {
        String version = getPluginMeta().getVersion();
        String author = String.join(", ", getPluginMeta().getAuthors());
        String line1 = "FeatherEconomy v" + version;
        String line2 = "Author: " + author;
        int width = Math.max(line1.length(), line2.length()) + 4;
        String border = "─".repeat(width);
        MiniMessage mm = MiniMessage.miniMessage();
        Component[] lines = {
                mm.deserialize("<dark_gray>┌" + border + "┐"),
                mm.deserialize("<dark_gray>│  <green>FeatherEconomy v" + version + " ".repeat(width - line1.length() - 2) + "<dark_gray>│"),
                mm.deserialize("<dark_gray>│  <gray>Author: " + author + " ".repeat(width - line2.length() - 2) + "<dark_gray>│"),
                mm.deserialize("<dark_gray>└" + border + "┘"),
        };
        for (Component line : lines) {
            getServer().getConsoleSender().sendMessage(line);
        }
    }

    @Override
    public void onDisable() {

        if (this.storage != null) this.storage.shutdown();
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
