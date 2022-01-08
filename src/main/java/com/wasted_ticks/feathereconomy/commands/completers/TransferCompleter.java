package com.wasted_ticks.feathereconomy.commands.completers;

import com.wasted_ticks.feathereconomy.FeatherEconomy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TransferCompleter implements TabCompleter {

    private final FeatherEconomy plugin;

    public TransferCompleter(FeatherEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if(args.length == 2) return null;
        else return new ArrayList<>();
    }
}