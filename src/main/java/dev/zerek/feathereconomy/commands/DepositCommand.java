package dev.zerek.feathereconomy.commands;

import dev.zerek.feathereconomy.FeatherEconomy;
import dev.zerek.feathereconomy.config.FeatherEconomyMessages;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

    private Integer parseAmount(String amount, Player player) {

        int result = 0;
        
        if (amount.equalsIgnoreCase("all")) {

            for(ItemStack itemStack : player.getInventory().getContents()) {

                if (itemStack != null && itemStack.getType().equals(Material.LAPIS_LAZULI)) result += itemStack.getAmount();
            }
            
            return result;
        }

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

        if (!sender.hasPermission("feather.economy.deposit")) {

            sender.sendMessage(messages.get("ErrorNoPermission"));

            return true;
        }

        if (args.length != 1) {

            sender.sendMessage(messages.get("DepositUsage"));

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

        if (!player.getInventory().containsAtLeast(new ItemStack(Material.LAPIS_LAZULI), amount)) {

            player.sendMessage(messages.get("DepositInsufficient"));

            return true;
        }

        // Checks passed ----------------------------------------------------------------

        player.getInventory().removeItem(new ItemStack(Material.LAPIS_LAZULI, amount));

        plugin.getEconomy().depositPlayer(player, amount);

        player.sendMessage(messages.get("Deposit", Map.of("amount", String.valueOf(amount))));

        player.sendMessage(messages.get("Balance", Map.of("balance",String.valueOf((int) plugin.getEconomy().getBalance(player)))));

        return true;
    }
}
