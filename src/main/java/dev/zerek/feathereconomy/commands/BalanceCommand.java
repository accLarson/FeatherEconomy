package dev.zerek.feathereconomy.commands;

import dev.zerek.feathereconomy.FeatherEconomy;
import dev.zerek.feathereconomy.config.FeatherEconomyMessages;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BalanceCommand implements CommandExecutor {

    private final FeatherEconomy plugin;

    private final FeatherEconomyMessages messages;

    public BalanceCommand(FeatherEconomy plugin) {

        this.plugin = plugin;

        this.messages = plugin.getFeatherEconomyMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        switch (args.length) {

            // /balance
            case 0:

                if (!(sender instanceof Player)) {

                    sender.sendMessage(messages.get("ErrorNotPlayer"));

                    return true;
                }

                if (!sender.hasPermission("feather.economy.balance")) {

                    sender.sendMessage(messages.get("ErrorNoPermission"));

                    return true;
                }

                // Checks passed ----------------------------------------------------------------

                sender.sendMessage(messages.get("Balance", Map.of("balance",String.valueOf((int) plugin.getEconomy().getBalance((OfflinePlayer) sender)))));

                return true;

            // /balance [player]
            case 1:

                if (!sender.hasPermission("feather.economy.balance.others")) {

                    sender.sendMessage(messages.get("ErrorNoPermission"));

                    return true;
                }

                OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[0]);

                if (!plugin.getEconomy().hasAccount(target)) {

                    sender.sendMessage(messages.get("ErrorUnresolvedPlayer"));

                    return true;
                }

                // Checks passed ----------------------------------------------------------------

                sender.sendMessage(messages.get("BalanceOther", Map.of("player", target.getName(), "balance", String.valueOf((int) plugin.getEconomy().getBalance(target)))));

                return true;
        }

        if (sender.hasPermission("feather.economy.balance.others")) sender.sendMessage(messages.get("BalanceUsageOthers"));

        else if (sender.hasPermission("feather.economy.balance")) sender.sendMessage(messages.get("BalanceUsage"));

        else sender.sendMessage(messages.get("ErrorNoPermission"));

        return true;
    }
}
