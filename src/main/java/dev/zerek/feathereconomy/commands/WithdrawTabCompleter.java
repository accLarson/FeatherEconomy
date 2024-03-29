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

        if (!sender.hasPermission("feather.economy.withdraw")) return new ArrayList<>();

        if (args.length != 1) return new ArrayList<>();

        ArrayList<String> suggestions = new ArrayList<>();

        if (sender instanceof Player) suggestions.add(String.valueOf(plugin.getEconomy().getBalance(((Player) sender))));

        return suggestions;
    }
}
