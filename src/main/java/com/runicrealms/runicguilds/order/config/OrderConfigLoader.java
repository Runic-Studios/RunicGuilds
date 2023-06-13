package com.runicrealms.runicguilds.order.config;

import com.runicrealms.runicguilds.order.WorkOrder;
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

    public WorkOrder loadOrder(String orderName) {
        if (!config.contains("orders." + orderName)) {
            throw new IllegalArgumentException("Order '" + orderName + "' does not exist in config");
        }

        Map<String, Integer> items = new HashMap<>();
        for (String key : config.getConfigurationSection("orders." + orderName + ".ids").getKeys(false)) {
            items.put(key, config.getInt("orders." + orderName + ".ids." + key));
        }
        int exp = config.getInt("orders." + orderName + ".exp");

        return new WorkOrder(orderName, items, exp);
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

