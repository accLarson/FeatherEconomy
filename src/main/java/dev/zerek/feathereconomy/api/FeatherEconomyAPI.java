package dev.zerek.feathereconomy.api;

import dev.zerek.feathereconomy.FeatherEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/** Lets other plugins label the log row of a payment they made through Vault. */
public final class FeatherEconomyAPI {

    private FeatherEconomyAPI() {}

    /** Sets the label and counterparty (may be null) on the log row of a successful Vault response. */
    public static void describe(EconomyResponse response, String label, UUID counterparty) {
        if (!(response instanceof FeatherEconomyResponse feather) || feather.getEntry() == null || !response.transactionSuccess()) {
            return;
        }
        String shortLabel = label;
        if (shortLabel.length() > 32) {
            shortLabel = shortLabel.substring(0, 32);
        }
        JavaPlugin.getPlugin(FeatherEconomy.class).getStorage().describe(feather.getEntry(), shortLabel, counterparty);
    }
}
