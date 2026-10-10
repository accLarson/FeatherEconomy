package dev.zerek.feathereconomy.api;

import dev.zerek.feathereconomy.FeatherEconomy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.plugin.java.JavaPlugin;

/** Lets other plugins label the log row of a payment they made through Vault. */
public final class FeatherEconomyAPI {

    private FeatherEconomyAPI() {}

    /** Sets the Action cell of the log row of a successful Vault response; the component is stored as JSON and shown by /lapislog. */
    public static void describe(EconomyResponse response, Component action) {
        if (!(response instanceof FeatherEconomyResponse feather) || feather.getEntry() == null || !response.transactionSuccess()) {
            return;
        }
        String type = PlainTextComponentSerializer.plainText().serialize(action);
        if (type.length() > 32) {
            type = type.substring(0, 32);
        }
        String detail = GsonComponentSerializer.gson().serialize(action);
        JavaPlugin.getPlugin(FeatherEconomy.class).getStorage().describe(feather.getEntry(), type, detail);
    }
}
