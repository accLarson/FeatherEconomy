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

public class TransferCommand implements CommandExecutor {

    private final FeatherEconomy plugin;
    private final FeatherEconomyMessages messages;

    public TransferCommand(FeatherEconomy plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherEconomyMessages();
    }

    private Integer parseAmount(String amount) {
        try {
            return Integer.parseInt(amount);
        } catch(NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(messages.get("economy_error_player"));
            return true;
        }

        Player player = (Player) sender;
        if(!player.hasPermission("feather.economy.transfer")) {
            player.sendMessage(messages.get("economy_error_permission"));
            return true;
        }

        if(args.length != 2) {
            player.sendMessage(messages.get("economy_transfer_usage"));
            return true;
        }

        Integer amount = this.parseAmount(args[0]);
        if(amount == null || amount < 1) {
            player.sendMessage(messages.get("economy_error_non-number"));
            return true;
        }

        String name = args[1];
        Player target = Bukkit.getPlayer(name);
        if(target == null) {
            player.sendMessage(messages.get("economy_error_unresolved_player"));
            return true;
        }

        if(this.plugin.getEconomy().has(player, amount)) {

            this.plugin.getEconomy().depositPlayer(target, amount);
            this.plugin.getEconomy().withdrawPlayer(player, amount);

            target.sendMessage(messages.get("economy_transfer_message_target", Map.of(
                    "player", player.getName(),
                    "amount", String.valueOf(amount)
            )));
            player.sendMessage(messages.get("economy_transfer_message_player", Map.of(
                    "amount", String.valueOf(amount),
                    "player", target.getName()
            )));

        } else {
            player.sendMessage(messages.get("economy_transfer_insufficient"));
        }
        return true;
    }
}
