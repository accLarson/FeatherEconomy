package dev.zerek.feathereconomy.commands;

import dev.zerek.feathereconomy.FeatherEconomy;
import dev.zerek.feathereconomy.config.FeatherEconomyMessages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
            if(!player.hasPermission("feather.economy.withdraw")) {
                player.sendMessage(messages.get("ErrorNoPermission"));
                return true;
            }

            Integer amount = this.parseAmount(args[0]);
            if(amount == null || amount < 1) {
                player.sendMessage(messages.get("ErrorNotNumber"));
                return true;
            }

            if(this.plugin.getEconomy().has(player, amount)) {
                this.plugin.getEconomy().withdrawPlayer(player, amount);
                HashMap<Integer, ItemStack> stack = player.getInventory().addItem(new ItemStack(Material.LAPIS_LAZULI, amount));
                if (!stack.isEmpty()) {
                    int refund = (stack.get(0)).getAmount();
                    this.plugin.getEconomy().depositPlayer(player, refund);
                    player.sendMessage(messages.get("WithdrawIncomplete"));
                } else {
                    player.sendMessage(messages.get("Withdraw", Map.of(
                            "amount", String.valueOf(amount)
                    )));
                }
                double balance = this.plugin.getEconomy().getBalance(player);
                player.sendMessage(messages.get("Balance", Map.of(
                        "balance", String.valueOf((int) balance)
                )));
            } else {
                player.sendMessage(messages.get("WithdrawInsufficient"));
            }
            return true;
        }

        if(args.length == 2) {
            if(!player.hasPermission("feather.economy.withdraw.others")) {
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
                return true;
            }

            if(this.plugin.getEconomy().has(target, amount)) {

                this.plugin.getEconomy().withdrawPlayer(target, amount);

                player.sendMessage(messages.get("WithdrawOther", Map.of(
                        "amount", String.valueOf(amount),
                        "player", target.getName()
                )));

                double balance = this.plugin.getEconomy().getBalance(target);
                player.sendMessage(messages.get("BalanceOther", Map.of(
                        "player", target.getName(),
                        "balance", String.valueOf((int) balance)
                )));
            } else {
                player.sendMessage(messages.get("WithdrawOtherInsufficient", Map.of(
                        "player", target.getName()
                )));
            }
            return true;
        }

        player.sendMessage(messages.get("WithdrawUsage"));
        return true;
    }

}
