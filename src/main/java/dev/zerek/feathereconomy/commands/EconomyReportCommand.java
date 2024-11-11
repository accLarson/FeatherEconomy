package dev.zerek.feathereconomy.commands;

import dev.zerek.feathereconomy.FeatherEconomy;
import dev.zerek.feathereconomy.config.FeatherEconomyMessages;
import dev.zerek.feathereconomy.managers.StorageManager;
import dev.zerek.feathereconomy.utilities.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class EconomyReportCommand implements CommandExecutor {
    private final FeatherEconomy plugin;
    private final FeatherEconomyMessages messages;
    private final ChatUtil chatUtil;
    private final StorageManager storage;
    private static final TextColor PRIMARY_COLOR = TextColor.fromHexString("#949bd1");
    private static final TextColor SECONDARY_COLOR = TextColor.fromHexString("#656b96");
    private static final TextColor TITLE_COLOR = TextColor.fromHexString("#AAAAAA");
    private static final TextColor HIGHLIGHT_COLOR = TextColor.fromHexString("#FFFFFF");

    public EconomyReportCommand(FeatherEconomy plugin) {
        this.plugin = plugin;
        this.messages = this.plugin.getFeatherEconomyMessages();
        this.chatUtil = new ChatUtil(plugin);
        this.storage = this.plugin.getStorage();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("feather.economy.economyreport")) {
            sender.sendMessage(messages.get("ErrorNoPermission"));
            return true;
        }

        // Run the report generation asynchronously
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            int totalAccounts = storage.getTotalAccounts();
            int activeAccounts = storage.getActiveAccounts();
            int totalLapis = storage.getTotalLapis(false);
            double avgBalance = storage.getAverageBalance();
            double activeAvgBalance = storage.getAverageBalance(true);
            double top4All = storage.getTopNWealth(4, "all");
            double top16All = storage.getTopNWealth(16, "all");
            double top64All = storage.getTopNWealth(64, "all");
            double top4Active = storage.getTopNWealth(4, "active");
            double top16Active = storage.getTopNWealth(16, "active");
            double top64Active = storage.getTopNWealth(64, "active");
            int activeHour = storage.getActiveAccountsPeriod(1);
            int activeDay = storage.getActiveAccountsPeriod(24);
            int active7Day = storage.getActiveAccountsPeriod(168);
            int active64Day = storage.getActiveAccountsPeriod(1536);

            // Send the report header
            sender.sendMessage(messages.get("EconomyReportPreLine"));

            // Display all stats in organized sections
            displayBasicStats(sender, totalLapis, storage.getTotalLapis(true),
                            activeHour, activeDay, active7Day, active64Day, avgBalance, activeAvgBalance);

            displayWealthDistribution(sender, totalAccounts, activeAccounts,
                                    top4All, top16All, top64All,
                                    top4Active, top16Active, top64Active);

            // Personal stats if sender is a player
            if (sender instanceof Player player) {
                sender.sendMessage(messages.get("EconomyReportLine"));

                int rank = storage.getWealthRanking(player.getUniqueId());
                int balance = (int) storage.getBalance(player.getUniqueId());

                // Get ordinal suffix for rank
                String suffix = switch (rank % 10) {
                    case 1 -> rank % 100 != 11 ? "st" : "th";
                    case 2 -> rank % 100 != 12 ? "nd" : "th";
                    case 3 -> rank % 100 != 13 ? "rd" : "th";
                    default -> "th";
                };

                Component nameLabel = chatUtil.addSpacing(
                    Component.text(player.getName(), HIGHLIGHT_COLOR), 90);

                Component balanceLabel = chatUtil.addSpacing(
                    Component.text("Balance: ", SECONDARY_COLOR)
                        .append(Component.text(String.format("%,d L", balance), PRIMARY_COLOR)), 96);
                Component rankLabel = chatUtil.addSpacing(
                    Component.text("Wealth Rank: ", SECONDARY_COLOR)
                        .append(Component.text(rank + suffix, HIGHLIGHT_COLOR)), 130, true);

                Component yourStats = Component.empty().append(nameLabel).append(balanceLabel).append(rankLabel);
                sender.sendMessage(yourStats);
            }
            sender.sendMessage(messages.get("EconomyReportLine"));
        });

        return true;
    }

    private void displayBasicStats(CommandSender sender, int totalLapis, int activeLapis,
                                  int activeHour, int activeDay, int active7Day, int active64Day, double avgBalance, double activeAvgBalance) {
        // First row: Total Lapis and Active 1h
        Component totalLabel = chatUtil.addSpacing(
            Component.text("Total Lapis:", TITLE_COLOR)
                .hoverEvent(Component.text("Total lapis in all accounts.", SECONDARY_COLOR)), 80);
        Component totalValue = chatUtil.addSpacing(Component.text(String.format("%,d L", totalLapis), PRIMARY_COLOR), 56, true);

        Component activeHourLabel = chatUtil.addSpacing(
            Component.text("Active In Last 1h:", TITLE_COLOR)
                .hoverEvent(Component.text("Accounts with transactions in the last hour", SECONDARY_COLOR)), 104);
        Component activeHourValue = chatUtil.addSpacing(Component.text(String.format("%,d", activeHour), PRIMARY_COLOR), 40, true);

        sender.sendMessage(Component.empty()
            .append(totalLabel).append(totalValue)
            .append(chatUtil.addSpacing(Component.empty(), 36))
            .append(activeHourLabel).append(activeHourValue));

        // Second row: Active Lapis and Active 1d
        Component activeTotalLabel = chatUtil.addSpacing(
            Component.text("Active Lapis:", TITLE_COLOR)
                .hoverEvent(Component.text("Total lapis in active accounts. \n(Accounts with transaction within 64 days)", SECONDARY_COLOR)), 80);
        Component activeTotalValue = chatUtil.addSpacing(Component.text(String.format("%,d L", activeLapis), PRIMARY_COLOR), 56, true);

        Component activeDayLabel = chatUtil.addSpacing(
            Component.text("Active In Last 1d:", TITLE_COLOR)
                .hoverEvent(Component.text("Accounts with transactions in the last 24 hours", SECONDARY_COLOR)), 104);
        Component activeDayValue = chatUtil.addSpacing(Component.text(String.format("%,d", activeDay), PRIMARY_COLOR), 40, true);

        sender.sendMessage(Component.empty()
            .append(activeTotalLabel).append(activeTotalValue)
            .append(chatUtil.addSpacing(Component.empty(), 36))
            .append(activeDayLabel).append(activeDayValue));

        // Third row: Average Balance and Active 7d
        Component avgLabel = chatUtil.addSpacing(
            Component.text("Avg Balance:", TITLE_COLOR)
                .hoverEvent(Component.text("Average lapis balance of all accounts.", SECONDARY_COLOR)), 80);
        Component avgValue = chatUtil.addSpacing(Component.text(String.format("%,d L", (int)avgBalance), PRIMARY_COLOR), 56, true);

        Component active7DayLabel = chatUtil.addSpacing(
                Component.text("Active In Last 7d:", TITLE_COLOR)
                        .hoverEvent(Component.text("Accounts with transactions in the last 7 days", SECONDARY_COLOR)), 104);
        Component active7DayValue = chatUtil.addSpacing(Component.text(String.format("%,d", active7Day), PRIMARY_COLOR), 40, true);

        sender.sendMessage(Component.empty()
                .append(avgLabel).append(avgValue)
                .append(chatUtil.addSpacing(Component.empty(), 36))
                .append(active7DayLabel).append(active7DayValue));

        // Fourth row: Active Average Balance and Active 64d
        Component activeAvgLabel = chatUtil.addSpacing(
            Component.text("Avg Active:", TITLE_COLOR)
                .hoverEvent(Component.text("Average lapis balance of active accounts. \n(Accounts with transaction within 64 days)", SECONDARY_COLOR)), 80);
        Component activeAvgValue = chatUtil.addSpacing(Component.text(String.format("%,d L", (int)activeAvgBalance), PRIMARY_COLOR), 56, true);

        Component active64DayLabel = chatUtil.addSpacing(
                Component.text("Active In Last 64d:", TITLE_COLOR)
                        .hoverEvent(Component.text("Accounts with transactions in the last 64 days", SECONDARY_COLOR)), 104);
        Component active64DayValue = chatUtil.addSpacing(Component.text(String.format("%,d", active64Day), PRIMARY_COLOR), 40, true);

        sender.sendMessage(Component.empty()
                .append(activeAvgLabel).append(activeAvgValue)
                .append(chatUtil.addSpacing(Component.empty(), 36))
                .append(active64DayLabel).append(active64DayValue));

        sender.sendMessage(Component.empty());
    }

    private void displayWealthDistribution(CommandSender sender, int totalAccounts, int activeAccounts,
                                         double top4All, double top16All, double top64All,
                                         double top4Active, double top16Active, double top64Active) {
        // Wealth Distribution header
        Component wealthTitle = chatUtil.addSpacing(Component.text("Wealth Distribution:", TITLE_COLOR), 136);
        Component top4Label = chatUtil.addSpacing(Component.text("Top 4", TITLE_COLOR), 60,true);
        Component top16Label = chatUtil.addSpacing(Component.text("Top 16", TITLE_COLOR), 60,true);
        Component top64Label = chatUtil.addSpacing(Component.text("Top 64", TITLE_COLOR), 60,true);
        sender.sendMessage(Component.text("").append(wealthTitle).append(top4Label).append(top16Label).append(top64Label));

        // Distribution rows
        sender.sendMessage(createDistributionRow("all", totalAccounts, top4All, top16All, top64All));
        sender.sendMessage(createDistributionRow("active", activeAccounts, top4Active, top16Active, top64Active));
    }

    private Component createTopNComponent(String type, int total, int topN, double wealthPercent) {

        String wealthScope = switch (type) {
            case "active" -> "lapis in active accounts";
            default -> "lapis in all accounts";
        };

        int totalLapis = storage.getTotalLapis(type.equals("active"));
        int topLapis = (int)(totalLapis * (wealthPercent / 100.0));

        Component hoverText = Component.text("Top " + topN + " accounts own", TITLE_COLOR)
           .append(Component.text(" " + String.format("%.1f%%", wealthPercent), PRIMARY_COLOR))
           .append(Component.text("\nof " + wealthScope + "\n", TITLE_COLOR))
           .append(Component.text(String.format("%,d L", topLapis), PRIMARY_COLOR));

        return chatUtil.addSpacing(
            Component.text("", SECONDARY_COLOR)
               .append(Component.text(String.format("%.1f%%", wealthPercent), PRIMARY_COLOR))
               .hoverEvent(hoverText), 60,true);
    }

    private Component createDistributionRow(String label, int totalAccounts, double top4Percent, 
                                          double top16Percent, double top64Percent) {
        // Create the label part with proper spacing
        String hoverText = switch (label) {
            case "all" -> "Calculations consider ALL accounts.";
            case "active" -> String.format("Calculations only consider accounts which have\nhad a transaction within the last %d days.",
                                         plugin.getFeatherEconomyConfig().getInactiveThresholdDays());
            default -> "";
        };

        // Create the label and count as a grouped component with shared hover
        Component labelText = chatUtil.addSpacing(Component.text(label, TITLE_COLOR), 40);
        Component countText = chatUtil.addSpacing(Component.text(String.format("(%,d)", totalAccounts), SECONDARY_COLOR), 50,true);

        // Group the label and count together with shared hover text
        Component labelSpace = Component.empty()
            .append(labelText)
            .append(countText)
            .hoverEvent(Component.text(hoverText, SECONDARY_COLOR));

        Component emptySpace = chatUtil.addSpacing(Component.empty(),46);

        Component top4Value = createTopNComponent(label, totalAccounts, 4, top4Percent);
        Component top16Value = createTopNComponent(label, totalAccounts, 16, top16Percent);
        Component top64Value = createTopNComponent(label, totalAccounts, 64, top64Percent);

        return Component.text("")
                .append(labelSpace)
                .append(emptySpace)
                .append(top4Value)
                .append(top16Value)
                .append(top64Value);
    }
}
