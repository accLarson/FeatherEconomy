package dev.zerek.feathereconomy.commands;

import dev.zerek.feathereconomy.FeatherEconomy;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WithdrawTabCompleter implements TabCompleter {

    private final FeatherEconomy plugin;

    public WithdrawTabCompleter(FeatherEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        // /withdraw [amount]
        if (args.length == 1) {

            // Check if sender has permission
            if (!sender.hasPermission("feather.economy.withdraw")) return new ArrayList<>();

            ArrayList<String> suggestions = new ArrayList<>();

            if (sender instanceof Player) {

                Player player = (Player) sender;

                suggestions.add(String.valueOf(plugin.getEconomy().getBalance(player)));
            }

            return suggestions;
        }

        // /withdraw [amount] [player]
        else if (args.length == 2){

            // Check if sender has permission
            if (!sender.hasPermission("feather.economy.withdraw.other")) return new ArrayList<>();

            return null;
        }

        return new ArrayList<>();
    }
}
