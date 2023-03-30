package dev.zerek.feathereconomy.commands;

import dev.zerek.feathereconomy.FeatherEconomy;
import dev.zerek.feathereconomy.config.FeatherEconomyMessages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DepositCommand implements CommandExecutor {

    private final FeatherEconomy plugin;
    private final FeatherEconomyMessages messages;

    public DepositCommand(FeatherEconomy plugin) {
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
            sender.sendMessage(messages.get("ErrorNotPlayer"));
            return true;
        }

        Player player = (Player) sender;

        if(args.length == 1) {
            if(!player.hasPermission("feather.economy.deposit")) {
                player.sendMessage(messages.get("ErrorNoPermission"));
                return true;
            }

            Integer amount = 0;
            if(args[0].equalsIgnoreCase("all")) {
                for(ItemStack stack : player.getInventory().getContents()) {
                    if(stack != null && stack.getType().equals(Material.LAPIS_LAZULI)) {
                        amount += stack.getAmount();
                    }
                }
            } else {
                amount = this.parseAmount(args[0]);
                if(amount == null || amount < 1) {
                    player.sendMessage(messages.get("ErrorNotNumber"));
                    return true;
                }

            }


            if (player.getInventory().containsAtLeast(new ItemStack(Material.LAPIS_LAZULI), amount)) {

                Inventory inventory = player.getInventory();
                inventory.removeItem(new ItemStack(Material.LAPIS_LAZULI, amount));

                this.plugin.getEconomy().depositPlayer(player, amount);

                player.sendMessage(messages.get("Deposit", Map.of(
                        "amount", String.valueOf(amount)
                )));

                double balance = this.plugin.getEconomy().getBalance(player);
                player.sendMessage(messages.get("Balance", Map.of(
                        "balance",String.valueOf((int) balance)
                )));

            } else {
                player.sendMessage(messages.get("BalanceInsufficient"));
            }
            return true;
        }

        if(args.length == 2) {
            if(!player.hasPermission("feather.economy.deposit.other")) {
                player.sendMessage(messages.get("ErrorNoPermission"));
                return true;
            }

            Integer amount = this.parseAmount(args[0]);
            if(amount == null || amount < 1) {
                player.sendMessage(messages.get("ErrorNotNumber"));
                return true;
            }

            String name = args[1];
            Player target = Bukkit.getPlayer(name);
            if(target == null) {
                player.sendMessage(messages.get("ErrorUnresolvedPlayer"));
            } else {

                this.plugin.getEconomy().depositPlayer(target, amount);
                target.sendMessage(messages.get("Deposit", Map.of(
                        "amount", String.valueOf(amount)
                )));

                player.sendMessage(messages.get("DepositOther", Map.of(
                        "amount", String.valueOf(amount),
                        "player", target.getName()
                )));

                double balance = this.plugin.getEconomy().getBalance(target);
                player.sendMessage(messages.get("BalanceOther", Map.of(
                        "player", target.getName(),
                        "balance", String.valueOf((int) balance)
                )));
            }
            return true;
        }

        player.sendMessage(messages.get("DepositUsage"));
        return true;
    }

}
