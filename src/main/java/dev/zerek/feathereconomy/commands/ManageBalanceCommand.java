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

    private Integer parseAmount(String amount) {

        try {
            return Integer.parseInt(amount);
        }

        catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("feather.economy.managebalance")) {
            sender.sendMessage(messages.get("ErrorNoPermission"));

            return true;
        }

        switch (args.length) {

            // /managebalance
            case 0:

                sender.sendMessage(messages.get("BalanceOtherUsage"));
                return true;

            // /managebalance [player]
            case 1:

                OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[0]);

                if (plugin.getEconomy().hasAccount(target)) sender.sendMessage(messages.get("BalanceOtherUsage"));
                else sender.sendMessage(messages.get("ErrorUnresolvedPlayer"));
                return true;

            // /managebalance [player] [remove/add/set]
            case 2:

                OfflinePlayer target2 = plugin.getServer().getOfflinePlayer(args[0]);

                if (!plugin.getEconomy().hasAccount(target2)) {
                    sender.sendMessage(messages.get("ErrorUnresolvedPlayer"));
                    return true;
                }

                // /managebalance [player] remove
                else if (args[1].equalsIgnoreCase("remove")) {
                    sender.sendMessage(messages.get("ManageBalanceRemoveUsage"));
                    return true;
                }

                // /managebalance [player] add
                else if (args[1].equalsIgnoreCase("add")) {
                    sender.sendMessage(messages.get("ManageBalanceAddUsage"));
                    return true;
                }

                // /managebalance [player] set
                else if (args[1].equalsIgnoreCase("set")) {
                    sender.sendMessage(messages.get("ManageBalanceSetUsage"));
                    return true;
                }

            // /managebalance [player] [remove/add/set] [amount]
            case 3:

                OfflinePlayer target3 = plugin.getServer().getOfflinePlayer(args[0]);

                if (!plugin.getEconomy().hasAccount(target3)) {
                    sender.sendMessage(messages.get("ErrorUnresolvedPlayer"));
                    return true;
                }

                Integer amount = this.parseAmount(args[2]);

                if (amount == null || amount < 1) {
                    sender.sendMessage(messages.get("ErrorNotNumber"));
                    return true;
                }

                // /managebalance [player] remove [amount]
                if (args[1].equalsIgnoreCase("remove")) {

                    if (!plugin.getEconomy().has(target3, amount)) {
                        sender.sendMessage(messages.get("ManageBalanceRemoveInsufficient", Map.of("player", target3.getName())));
                        sender.sendMessage(messages.get("BalanceOther", Map.of("player", target3.getName(), "balance", String.valueOf((int) plugin.getEconomy().getBalance(target3)))));
                        return true;
                    }

                    plugin.getEconomy().withdrawPlayer(target3,amount);

                    sender.sendMessage(messages.get("ManageBalanceRemove", Map.of("amount",String.valueOf(amount),"player",target3.getName())));
                    sender.sendMessage(messages.get("BalanceOther", Map.of("player", target3.getName(), "balance", String.valueOf((int) plugin.getEconomy().getBalance(target3)))));

                    if (target3.isOnline()) {
                        ((Player)target3).sendMessage(messages.get("ManageBalanceRemoveInform", Map.of("amount",String.valueOf(amount))));
                        ((Player)target3).sendMessage(messages.get("Balance", Map.of("balance",String.valueOf((int) plugin.getEconomy().getBalance(target3)))));
                    }

                    return true;
                }

                // /managebalance [player] add [amount]
                else if (args[1].equalsIgnoreCase("add")) {

                    plugin.getEconomy().depositPlayer(target3,amount);

                    sender.sendMessage(messages.get("ManageBalanceAdd", Map.of("amount",String.valueOf(amount),"player",target3.getName())));
                    sender.sendMessage(messages.get("BalanceOther", Map.of("player", target3.getName(), "balance", String.valueOf((int) plugin.getEconomy().getBalance(target3)))));

                    if (target3.isOnline()) {
                        ((Player)target3).sendMessage(messages.get("ManageBalanceAddInform", Map.of("amount",String.valueOf(amount))));
                        ((Player)target3).sendMessage(messages.get("Balance", Map.of("balance",String.valueOf((int) plugin.getEconomy().getBalance(target3)))));
                    }

                    return true;
                }

                // /managebalance [player] set [amount]
                else if (args[1].equalsIgnoreCase("set")) {

                    plugin.getEconomy().withdrawPlayer(target3,plugin.getEconomy().getBalance(target3));
                    plugin.getEconomy().depositPlayer(target3,amount);

                    sender.sendMessage(messages.get("ManageBalanceSet", Map.of("amount",String.valueOf(amount),"player",target3.getName())));

                    if (target3.isOnline()) ((Player)target3).sendMessage(messages.get("ManageBalanceSetInform", Map.of("amount",String.valueOf(amount))));

                    return true;
                }

            default:

                sender.sendMessage(messages.get("BalanceOtherUsage"));

                return true;
        }
    }
}
