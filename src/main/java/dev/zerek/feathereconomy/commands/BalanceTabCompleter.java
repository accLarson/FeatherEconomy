package dev.zerek.feathereconomy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BalanceTabCompleter implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // Check if sender has permission
        if (!sender.hasPermission("feather.economy.balance.others")) return new ArrayList<>();

        // Check if player provided correct amount of arguments
        if (args.length != 1) return new ArrayList<>();

        // Checks passed ----------------------------------------------------------------

        return null;
    }
}
