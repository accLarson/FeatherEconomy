package dev.zerek.feathereconomy.commands;

import dev.zerek.feathereconomy.FeatherEconomy;
import dev.zerek.feathereconomy.config.FeatherEconomyMessages;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class WithdrawCommand implements CommandExecutor {

    private final FeatherEconomy plugin;

    private final FeatherEconomyMessages messages;

    public WithdrawCommand(FeatherEconomy plugin) {

        this.plugin = plugin;

        this.messages = plugin.getFeatherEconomyMessages();
    }

    private Integer parseAmount(String amount, OfflinePlayer offlinePlayer) {

        if (amount.equalsIgnoreCase("all")) return (int) plugin.getEconomy().getBalance(offlinePlayer);

        else {

            try {

                return Integer.parseInt(amount);
            }

            catch (NumberFormatException e) {

                return null;
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("feather.economy.withdraw")) {

            sender.sendMessage(messages.get("ErrorNoPermission"));

            return true;
        }

        if (args.length != 1) {

            sender.sendMessage(messages.get("WithdrawUsage"));

            return true;

        }

        if (!(sender instanceof Player)) {

            sender.sendMessage(messages.get("ErrorNotPlayer"));

            return true;
        }

        Player player = (Player) sender;

        Integer amount = this.parseAmount(args[0], player);

        if (amount == null || amount < 1) {

            player.sendMessage(messages.get("ErrorNotNumber"));

            return true;
        }

        if (!plugin.getEconomy().has(player, amount)) {

            player.sendMessage(messages.get("WithdrawInsufficient"));

            return true;
        }

        // Checks passed ----------------------------------------------------------------

        this.plugin.getEconomy().withdrawPlayer(player, amount);

        HashMap<Integer, ItemStack> remainingItemStackMap = player.getInventory().addItem(new ItemStack(Material.LAPIS_LAZULI, amount));

        if (!remainingItemStackMap.isEmpty()) {

            int refund = (remainingItemStackMap.get(0)).getAmount();

            this.plugin.getEconomy().depositPlayer(player, refund);

            player.sendMessage(messages.get("WithdrawIncomplete", Map.of("withdrew", String.valueOf(amount - refund), "total", String.valueOf(amount))));
        }

        else player.sendMessage(messages.get("Withdraw", Map.of("amount", String.valueOf(amount))));

        player.sendMessage(messages.get("Balance", Map.of("balance", String.valueOf((int) plugin.getEconomy().getBalance(player)))));

        return true;
    }
}
