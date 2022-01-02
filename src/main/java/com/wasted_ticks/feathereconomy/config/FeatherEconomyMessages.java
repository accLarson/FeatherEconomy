package com.wasted_ticks.feathereconomy.config;

import com.wasted_ticks.feathereconomy.FeatherEconomy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.transformation.TransformationType;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FeatherEconomyMessages {

    private final FeatherEconomy plugin;
    private final Map<String, String> messages;
    private FileConfiguration config;
    private String themePrimary;
    private String themeSecondary;

    public FeatherEconomyMessages(FeatherEconomy plugin) {
        messages = new HashMap<>();
        this.plugin = plugin;
        this.init();
        this.load();
    }

    private void load() {
        Set<String> keys = config.getKeys(false);
        for (String key: keys) {
            messages.put(key, config.getString(key));
        }
    }

    private void init() {
        File file = new File(this.plugin.getDataFolder(), "messages.yml");
        if(!file.exists()) {
            this.plugin.saveResource("messages.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        Reader stream = new InputStreamReader(this.plugin.getResource("messages.yml"));
        YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(stream);
        config.options().copyDefaults(true);
        config.setDefaults(defaultConfig);
        try {
            config.save(file);
        } catch (IOException e) {}

        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public TextComponent get(String key){
        if(messages.containsKey(key)) {
            return (TextComponent) MiniMessage.builder()
                    .removeDefaultTransformations()
                    .transformation(TransformationType.COLOR)
                    .transformation(TransformationType.RESET)
                    .build()
                    .parse(messages.get(key));
        } else return Component.text("");
    }

    public TextComponent get(String key, Map<String, String> placeholders) {
        if(messages.containsKey(key)) {
            return (TextComponent) MiniMessage.builder()
                    .removeDefaultTransformations()
                    .transformation(TransformationType.COLOR)
                    .transformation(TransformationType.RESET)
                    .build()
                    .parse(messages.get(key), placeholders);
        } else return Component.text("");
    }
}