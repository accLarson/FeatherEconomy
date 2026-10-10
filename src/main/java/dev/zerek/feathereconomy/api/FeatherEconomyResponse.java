package dev.zerek.feathereconomy.api;

import dev.zerek.feathereconomy.managers.StorageManager;
import net.milkbowl.vault.economy.EconomyResponse;

public class FeatherEconomyResponse extends EconomyResponse {

    private final StorageManager.LogEntry entry;

    public FeatherEconomyResponse(double amount, double balance, ResponseType type, String errorMessage, StorageManager.LogEntry entry) {
        super(amount, balance, type, errorMessage);
        this.entry = entry;
    }

    StorageManager.LogEntry getEntry() {
        return this.entry;
    }
}
