package dev.zerek.feathereconomy.commands;

import dev.zerek.feathereconomy.FeatherEconomy;
import dev.zerek.feathereconomy.config.FeatherEconomyMessages;
import dev.zerek.feathereconomy.utilities.VanishedUtil;
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
        }

        catch (NumberFormatException e) {

            return null;
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if (!(sender instanceof Player)) {
            
            sender.sendMessage(messages.get("ErrorNotPlayer"));
            
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("feather.economy.transfer")) {
            
            player.sendMessage(messages.get("ErrorNoPermission"));
            
            return true;
        }

        if (args.length != 2) {

            player.sendMessage(messages.get("TransferUsage"));

            return true;
        }

        Integer amount = this.parseAmount(args[0]);

        if (amount == null || amount < 1) {

            player.sendMessage(messages.get("ErrorNotNumber"));

            return true;
        }

        Player target = plugin.getServer().getPlayer(args[1]);

        if (target == null || VanishedUtil.isVanished(target)) {

            player.sendMessage(messages.get("ErrorUnresolvedPlayer"));

            return true;
        }

        if (!plugin.getEconomy().has(player, amount)) {

            player.sendMessage(messages.get("TransferInsufficient"));

            return true;
        }

        // Checks passed ----------------------------------------------------------------

        plugin.getEconomy().depositPlayer(target, amount);

        plugin.getEconomy().withdrawPlayer(player, amount);

        target.sendMessage(messages.get("TransferReceived", Map.of("player", player.getName(), "amount", String.valueOf(amount))));

        player.sendMessage(messages.get("TransferSent", Map.of("amount", String.valueOf(amount), "player", target.getName())));

        return true;
    }
}
