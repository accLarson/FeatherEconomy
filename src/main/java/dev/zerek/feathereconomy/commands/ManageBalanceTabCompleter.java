package dev.zerek.feathereconomy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ManageBalanceTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("feather.economy.managebalance")) return new ArrayList<>();

        switch (args.length) {

            // /managebalance [player]
            case 1:

                return null;

            // /managebalance [player] [remove/add/set]
            case 2:

                List<String> options = new ArrayList<>();

                options.add("remove");
                options.add("add");
                options.add("set");

                List<String> match = new ArrayList<>();
                for (String option : options) if (option.toLowerCase().startsWith(args[1].toLowerCase())) match.add(option);

                return match;

            // /managebalance [player] [remove/add/set] [amount] OR anything else
            default:

                return new ArrayList<>();

        }
    }
}
