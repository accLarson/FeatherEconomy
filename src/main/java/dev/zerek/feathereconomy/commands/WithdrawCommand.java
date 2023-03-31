package dev.zerek.feathereconomy.commands;

import dev.zerek.feathereconomy.FeatherEconomy;
import dev.zerek.feathereconomy.config.FeatherEconomyMessages;
import org.bukkit.Bukkit;
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

        switch (args.length) {

            // /withdraw
            case 0:

                if (sender.hasPermission("feather.economy.withdraw.others")) sender.sendMessage(messages.get("WithdrawUsageOthers"));

                else if (sender.hasPermission("feather.economy.withdraw")) sender.sendMessage(messages.get("WithdrawUsage"));

                else sender.sendMessage(messages.get("ErrorNoPermission"));

                return true;

            // /withdraw [amount]
            case 1:

                if (!(sender instanceof Player)) {

                    sender.sendMessage(messages.get("ErrorNotPlayer"));

                    return true;
                }

                Player player = (Player) sender;

                if (!player.hasPermission("feather.economy.withdraw")) {

                    player.sendMessage(messages.get("ErrorNoPermission"));

                    return true;
                }

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

                else {

                    player.sendMessage(messages.get("Withdraw", Map.of("amount", String.valueOf(amount))));
                }

                player.sendMessage(messages.get("Balance", Map.of("balance", String.valueOf((int) plugin.getEconomy().getBalance(player)))));

                return true;

            // /withdraw [amount] [player]
            case 2:

                if (!sender.hasPermission("feather.economy.withdraw.others")) {

                    sender.sendMessage(messages.get("ErrorNoPermission"));

                    return true;
                }

                OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[0]);

                if (!plugin.getEconomy().hasAccount(target)) {

                    sender.sendMessage(messages.get("ErrorUnresolvedPlayer"));

                    return true;
                }

                Integer amount2 = this.parseAmount(args[0],target);

                if (amount2 == null || amount2 < 1) {

                    sender.sendMessage(messages.get("ErrorNotNumber"));

                    return true;
                }

                if (!plugin.getEconomy().has(target, amount2)) {

                    sender.sendMessage(messages.get("WithdrawOtherInsufficient", Map.of("player", target.getName())));

                    return true;
                }

                plugin.getEconomy().withdrawPlayer(target, amount2);

                String a = String.valueOf(amount2);

                String b = String.valueOf((int) plugin.getEconomy().getBalance(target));

                if (target.isOnline()) {

                    ((Player) target).sendMessage(messages.get("Withdraw", Map.of("amount", a)));

                    ((Player) target).sendMessage(messages.get("Balance", Map.of("balance", b)));
                }

                sender.sendMessage(messages.get("WithdrawOther", Map.of("amount", a, "player", target.getName())));

                sender.sendMessage(messages.get("BalanceOther", Map.of("player", target.getName(), "balance", b)));

                return true;
        }

        if (sender.hasPermission("feather.economy.withdraw.others")) sender.sendMessage(messages.get("WithdrawUsageOthers"));

        else if (sender.hasPermission("feather.economy.withdraw")) sender.sendMessage(messages.get("WithdrawUsage"));

        else sender.sendMessage(messages.get("ErrorNoPermission"));

        return true;
    }
}
