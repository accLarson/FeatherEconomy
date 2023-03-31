package dev.zerek.feathereconomy.commands;

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

public class DepositTabCompleter implements TabCompleter {


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        if (!sender.hasPermission("feather.economy.deposit")) return new ArrayList<>();

        if (args.length != 1) return new ArrayList<>();

        ArrayList<String> suggestions = new ArrayList<>();

        if (sender instanceof Player) {

            Player player = (Player) sender;

            Integer amount = 0;

            for (ItemStack itemStack : player.getInventory().getContents()) {

                if (itemStack != null && itemStack.getType().equals(Material.LAPIS_LAZULI)) amount += itemStack.getAmount();
            }

            if (amount != 0) suggestions.add(String.valueOf(amount));

        }

        return suggestions;
    }
}
