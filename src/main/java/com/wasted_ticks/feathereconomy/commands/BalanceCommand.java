package com.wasted_ticks.feathereconomy.commands;

import com.wasted_ticks.feathereconomy.FeatherEconomy;
import com.wasted_ticks.feathereconomy.config.FeatherEconomyMessages;
import org.bukkit.Bukkit;
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
        if(!(sender instanceof Player)) {
            sender.sendMessage(messages.get("economy_error_player"));
            return true;
        }

        Player player = (Player) sender;

        if(args.length == 0) {
            if(!player.hasPermission("feather.economy.balance")) {
                player.sendMessage(messages.get("economy_error_permission"));
                return true;
            }
            double balance = this.plugin.getEconomy().getBalance(player);
            player.sendMessage(messages.get("economy_balance_display", Map.of("balance",String.valueOf((int) balance))));
            return true;
        }

        if(args.length == 1) {
            if(!player.hasPermission("feather.economy.balance.other")) {
                player.sendMessage(messages.get("economy_error_permission"));
                return true;
            }
            String name = args[0];
            Player target = Bukkit.getPlayer(name);
            if(target == null) {
                player.sendMessage(messages.get("economy_error_unresolved_player"));
                return true;
            }

            double balance = this.plugin.getEconomy().getBalance(target);
            player.sendMessage(messages.get("economy_balance_display_other", Map.of(
                    "player", target.getName(),
                    "balance", String.valueOf((int) balance)
            )));
            return true;
        }

        player.sendMessage(messages.get("economy_balance_usage"));
        return true;
    }

}
