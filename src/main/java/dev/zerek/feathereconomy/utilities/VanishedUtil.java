package dev.zerek.feathereconomy.utilities;

import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

public class VanishedUtil {

    public static boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }
}
