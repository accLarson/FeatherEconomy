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

public class ManageBalanceCommand implements CommandExecutor {

    private final FeatherEconomy plugin;

    private final FeatherEconomyMessages messages;

    public ManageBalanceCommand(FeatherEconomy plugin) {

        this.plugin = plugin;

        this.messages = plugin.getFeatherEconomyMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        switch (args.length) {

            // /managebalance
            case 0:

                if (sender.hasPermission("feather.economy.managebalance")) sender.sendMessage(messages.get("ManageBalanceUsage"));

                else sender.sendMessage(messages.get("ErrorNoPermission"));

                return true;

            // /managebalance [player] - checking a players balance
            case 1:

                if (!sender.hasPermission("feather.economy.managebalance")) {

                    sender.sendMessage(messages.get("ErrorNoPermission"));

                    return true;
                }

                OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[0]);

                if (plugin.getEconomy().hasAccount(target)) {

                    sender.sendMessage(messages.get("ErrorUnresolvedPlayer"));

                    return true;
                }

                // Checks passed ----------------------------------------------------------------

                sender.sendMessage(messages.get("ManageBalanceView", Map.of("player", target.getName(), "balance", String.valueOf((int) plugin.getEconomy().getBalance(target)))));

                return true;

            // /managebalance [player] [balance/remove/add/set/empty]
            case 2:

                if (!sender.hasPermission("feather.economy.managebalance")) {

                    sender.sendMessage(messages.get("ErrorNoPermission"));

                    return true;
                }

                // /managebalance [player] balance
                if (args[1].equalsIgnoreCase("balance")) {

                    OfflinePlayer target2 = plugin.getServer().getOfflinePlayer(args[0]);

                    if (plugin.getEconomy().hasAccount(target2)) {

                        sender.sendMessage(messages.get("ErrorUnresolvedPlayer"));

                        return true;
                    }

                    // Checks passed ----------------------------------------------------------------

                    sender.sendMessage(messages.get("ManageBalanceView", Map.of("player", target2.getName(), "balance", String.valueOf((int) plugin.getEconomy().getBalance(target2)))));

                    return true;
                }

                // /managebalance [player] remove
                else if (args[1].equalsIgnoreCase("remove")) {

                    if (sender.hasPermission("feather.economy.managebalance")) sender.sendMessage(messages.get("ManageBalanceRemoveUsage"));

                    else sender.sendMessage(messages.get("ErrorNoPermission"));

                    return true;
                }

                // /managebalance [player] add
                else if (args[1].equalsIgnoreCase("add")) {

                    if (sender.hasPermission("feather.economy.managebalance")) sender.sendMessage(messages.get("ManageBalanceAddUsage"));

                    else sender.sendMessage(messages.get("ErrorNoPermission"));

                    return true;
                }

                // /managebalance [player] set
                else if (args[1].equalsIgnoreCase("set")) {

                    if (sender.hasPermission("feather.economy.managebalance")) sender.sendMessage(messages.get("ManageBalanceSetUsage"));

                    else sender.sendMessage(messages.get("ErrorNoPermission"));

                    return true;
                }

                // /managebalance [player] empty
                else if (args[1].equalsIgnoreCase("empty")) {

                    OfflinePlayer target2 = plugin.getServer().getOfflinePlayer(args[0]);

                    if (plugin.getEconomy().hasAccount(target2)) {

                        sender.sendMessage(messages.get("ErrorUnresolvedPlayer"));

                        return true;
                    }

                    // Checks passed ----------------------------------------------------------------

                    plugin.getEconomy().withdrawPlayer(target2,plugin.getEconomy().getBalance(target2));

                    if (target2.isOnline()) ((Player)target2).sendMessage(messages.get("ManageBalanceEmptyInform"));

                    sender.sendMessage(messages.get("ManageBalanceEmpty", Map.of("player",target2.getName())));

                    return true;
                }

                else {

                }

        }

        return true;
    }
}
