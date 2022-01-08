package com.wasted_ticks.feathereconomy.commands.completers;

import com.wasted_ticks.feathereconomy.FeatherEconomy;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DepositCompleter implements TabCompleter {


    private final FeatherEconomy plugin;

    public DepositCompleter(FeatherEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        if(args.length == 1) {

            ArrayList<String> suggestions = new ArrayList<>();

            if(sender instanceof Player) {
                Player player = (Player) sender;
                Integer amount = 0;
                for(ItemStack stack : player.getInventory().getContents()) {
                    if(stack != null && stack.getType().equals(Material.LAPIS_LAZULI)) {
                        amount += stack.getAmount();
                    }
                }
                if(amount != 0) {
                    StringUtil.copyPartialMatches(args[0], new ArrayList<String>(Arrays.asList(String.valueOf(amount))), suggestions);
                }
            }
            return suggestions;
        }
        return null;
    }
}
