package com.runicrealms.plugin.runicguilds.order.config;

import com.runicrealms.plugin.runicguilds.order.WorkOrder;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class OrderConfigLoader {
    private final YamlConfiguration config;

    public OrderConfigLoader(File configFile) throws IOException, InvalidConfigurationException {
        config = new YamlConfiguration();
        config.load(configFile);
    }

    public WorkOrder loadOrder(String orderId) {
        if (!config.contains("orders." + orderId)) {
            throw new IllegalArgumentException("Order '" + orderId + "' does not exist in config");
        }

        // Load display name
        String displayName = config.getString("orders." + orderId + ".display-name");
        // Load exp
        String icon = config.getString("orders." + orderId + ".icon");
        // Load exp
        int exp = config.getInt("orders." + orderId + ".exp");
        // Load required Items
        Map<String, Integer> items = new HashMap<>();
        for (String key : config.getConfigurationSection("orders." + orderId + ".ids").getKeys(false)) {
            items.put(key, config.getInt("orders." + orderId + ".ids." + key));
        }

        return new WorkOrder(orderId, displayName, icon, items, exp);
    }

    public WorkOrder chooseRandomOrder() {
        // Load all orders
        Set<String> orderNames = config.getConfigurationSection("orders").getKeys(false);

        // Choose a random order
        Random random = new Random();
        String newOrderName = orderNames.toArray(new String[0])[random.nextInt(orderNames.size())];

        // Load the new work order
        return this.loadOrder(newOrderName);
    }

}

