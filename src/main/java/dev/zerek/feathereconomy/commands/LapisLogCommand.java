package dev.zerek.feathereconomy.commands;

import dev.zerek.feathereconomy.FeatherEconomy;
import dev.zerek.feathereconomy.config.FeatherEconomyMessages;
import dev.zerek.feathereconomy.managers.StorageManager;
import dev.zerek.feathereconomy.managers.StorageManager.Transaction;
import dev.zerek.feathereconomy.utilities.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class LapisLogCommand implements CommandExecutor {

    private final FeatherEconomy plugin;
    private final FeatherEconomyMessages messages;
    private final ChatUtil chatUtil;
    private final StorageManager storage;

    private static final TextColor PRIMARY = TextColor.fromHexString("#949bd1");
    private static final TextColor SECONDARY = TextColor.fromHexString("#656b96");
    private static final TextColor MUTED = TextColor.fromHexString("#6C719D");
    private static final TextColor TITLE = TextColor.fromHexString("#AAAAAA");
    private static final TextColor HIGHLIGHT = TextColor.fromHexString("#FFFFFF");
    private static final TextColor POSITIVE = TextColor.fromHexString("#A8E6A3");
    private static final TextColor NEGATIVE = TextColor.fromHexString("#F2A5A5");
    private static final TextColor NEUTRAL = TextColor.fromHexString("#F2E6A3");

    public LapisLogCommand(FeatherEconomy plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherEconomyMessages();
        this.chatUtil = new ChatUtil(plugin);
        this.storage = plugin.getStorage();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("feather.economy.lapislog")) {
            sender.sendMessage(messages.get("ErrorNoPermission"));
            return true;
        }

        String targetName = null;
        int page = 1;

        if (args.length == 1) {
            if (isNumeric(args[0])) page = Integer.parseInt(args[0]);
            else targetName = args[0];
        } else if (args.length == 2) {
            targetName = args[0];
            if (!isNumeric(args[1])) {
                sender.sendMessage(messages.get("LapisLogUsage"));
                return true;
            }
            page = Integer.parseInt(args[1]);
        } else if (args.length > 2) {
            sender.sendMessage(messages.get("LapisLogUsage"));
            return true;
        }

        if (page < 1) {
            sender.sendMessage(messages.get("LapisLogUsage"));
            return true;
        }

        OfflinePlayer target;
        if (targetName != null) {
            if (!sender.hasPermission("feather.economy.managebalance")) {
                sender.sendMessage(messages.get("ErrorNoPermission"));
                return true;
            }
            target = plugin.getServer().getPlayerExact(targetName);
            if (target == null) target = plugin.getServer().getOfflinePlayerIfCached(targetName);
            if (target == null || !plugin.getEconomy().hasAccount(target)) {
                sender.sendMessage(messages.get("ErrorUnresolvedPlayer"));
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(messages.get("ErrorNotPlayer"));
                return true;
            }
            target = (Player) sender;
        }

        final OfflinePlayer finalTarget = target;
        final String finalTargetName = targetName;
        final int finalPage = page;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            int total = storage.getTransactionCount(finalTarget.getUniqueId());
            int linesPerPage = plugin.getFeatherEconomyConfig().getLapisLogLinesPerPage();

            if (total == 0) {
                sender.sendMessage(messages.get("LapisPreLine"));
                sender.sendMessage(messages.get("LapisEmpty"));
                sender.sendMessage(messages.get("LapisLine"));
                return;
            }

            int totalPages = (int) Math.ceil((double) total / linesPerPage);
            if (finalPage > totalPages) {
                sender.sendMessage(messages.get("LapisLogPageOutOfRange", Map.of(
                        "page", String.valueOf(finalPage),
                        "total", String.valueOf(totalPages)
                )));
                return;
            }

            List<Transaction> txs = storage.getTransactions(finalTarget.getUniqueId(), finalPage, linesPerPage);

            Map<UUID, String> nameCache = new HashMap<>();
            for (Transaction tx : txs) {
                UUID cp = tx.counterpartyUuid();
                if (cp != null && !nameCache.containsKey(cp)) {
                    String name = plugin.getServer().getOfflinePlayer(cp).getName();
                    if (name == null) {
                        sender.sendMessage(messages.get("LapisLogUnresolvedCounterparty"));
                        return;
                    }
                    nameCache.put(cp, name);
                }
            }

            sender.sendMessage(messages.get("LapisPreLine"));
            sender.sendMessage(buildColumnHeader());
            sender.sendMessage(messages.get("LapisLine"));
            for (Transaction tx : txs) sender.sendMessage(buildRow(tx, nameCache));
            sender.sendMessage(buildPaginatorFooter(finalTargetName, finalPage, totalPages));
        });

        return true;
    }

    private boolean isNumeric(String s) {
        try { Integer.parseInt(s); return true; } catch (NumberFormatException e) { return false; }
    }

    private Component buildColumnHeader() {
        return Component.empty()
                .append(chatUtil.addSpacing(Component.text("Date", TITLE), 40))
                .append(chatUtil.addSpacing(Component.text("Amount", TITLE), 65, true))
                .append(Component.text("  "))
                .append(chatUtil.addSpacing(Component.text("Action", TITLE), 140))
                .append(chatUtil.addSpacing(Component.text("Balance", TITLE), 65, true));
    }

    private Component buildRow(Transaction tx, Map<UUID, String> nameCache) {
        String dateStr = formatDate(tx.timestamp());
        String typeLabel = formatTypeLabel(tx, nameCache);
        boolean negative = isNegativeAmount(tx);
        String amountStr = (negative ? "-" : "") + String.format("%,d L", (int) tx.amount());
        String balanceStr = String.format("%,d L", (int) tx.balanceAfter());

        Component dateHover = Component.text(formatFullTimestamp(tx.timestamp()), HIGHLIGHT)
                .append(Component.newline())
                .append(Component.text("/servertime", SECONDARY));

        Component date = chatUtil.addSpacing(
                Component.text(dateStr, SECONDARY).hoverEvent(HoverEvent.showText(dateHover)), 40);
        Component amount = chatUtil.addSpacing(Component.text(amountStr, amountColorFor(tx)), 65, true);
        Component type = chatUtil.addSpacing(Component.text(typeLabel, TITLE), 140);
        Component balance = chatUtil.addSpacing(Component.text(balanceStr, PRIMARY), 65, true);

        return Component.empty()
                .append(date)
                .append(amount)
                .append(Component.text("  "))
                .append(type)
                .append(balance);
    }

    private TextColor amountColorFor(Transaction tx) {
        return switch (tx.type()) {
            case "DEPOSIT", "ADMIN_ADD" -> POSITIVE;
            case "WITHDRAW", "ADMIN_REMOVE" -> NEGATIVE;
            case "TRANSFER" -> "RECEIVED".equals(tx.direction()) ? POSITIVE : NEGATIVE;
            default -> NEUTRAL;
        };
    }

    private boolean isNegativeAmount(Transaction tx) {
        return switch (tx.type()) {
            case "WITHDRAW", "ADMIN_REMOVE" -> true;
            case "TRANSFER" -> "SENT".equals(tx.direction());
            default -> false;
        };
    }

    private Component buildPaginatorFooter(String targetName, int currentPage, int totalPages) {
        String base = targetName != null ? "/lapislog " + targetName : "/lapislog";
        String backCmd = base + " " + Math.max(1, currentPage - 1);
        String nextCmd = base + " " + Math.min(totalPages, currentPage + 1);

        Component backArrow = currentPage > 1
                ? messages.get("LapisBackArrow").clickEvent(ClickEvent.runCommand(backCmd))
                : messages.get("LapisBackArrowDisabled");

        Component nextArrow = currentPage < totalPages
                ? messages.get("LapisNextArrow").clickEvent(ClickEvent.runCommand(nextCmd))
                : messages.get("LapisNextArrowDisabled");

        Component pageCount = messages.get("LapisPageCount", Map.of(
                "page", String.format("%04d", currentPage),
                "total", String.format("%04d", totalPages)
        ));

        return Component.empty()
                .append(messages.get("LapisPaginatorPrefix"))
                .append(backArrow)
                .append(pageCount)
                .append(nextArrow)
                .append(messages.get("LapisPaginatorSuffix"));
    }

    private String formatTypeLabel(Transaction tx, Map<UUID, String> nameCache) {
        String cp = tx.counterpartyUuid() != null ? nameCache.get(tx.counterpartyUuid()) : "Console";
        String direction = tx.direction();
        return switch (tx.type()) {
            case "DEPOSIT" -> "Deposit";
            case "WITHDRAW" -> "Withdraw";
            case "TRANSFER" -> "SENT".equals(direction) ? "To " + cp : "From " + cp;
            case "ADMIN_ADD" -> "SELF".equals(direction) ? "Self +" : "Added by " + cp;
            case "ADMIN_REMOVE" -> "SELF".equals(direction) ? "Self -" : "Removed by " + cp;
            case "ADMIN_SET" -> "SELF".equals(direction) ? "Self Set" : "Set by " + cp;
            default -> tx.type();
        };
    }

    private static final TimeZone EASTERN = TimeZone.getTimeZone("America/Toronto");

    private String formatDate(long timestamp) {
        SimpleDateFormat fmt = new SimpleDateFormat("MMM-dd");
        fmt.setTimeZone(EASTERN);
        return fmt.format(new Date(timestamp));
    }

    private String formatFullTimestamp(long timestamp) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        fmt.setTimeZone(EASTERN);
        return fmt.format(new Date(timestamp));
    }
}