package dev.crius.dropcollector.upgrade;

import dev.crius.dropcollector.DropCollectorPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
public class UpgradeManager {

    private final DropCollectorPlugin plugin;
    private final Map<Integer, Upgrade> upgradeMap = new LinkedHashMap<>();

    public void init() {
        upgradeMap.clear();

        ConfigurationSection section = plugin.getPluginConfig().getConfigurationSection("Upgrades");
        int i = 1;
        for (String key : section.getKeys(false)) {
            double price = section.getDouble(key + ".price");
            int max = section.getInt(key + ".max");
            String displayName = section.getString(key + ".displayName");

            upgradeMap.put(i, new Upgrade(displayName, price, max, i));
            i++;
        }
    }

    public Upgrade getUpgrade(int place) {
        return upgradeMap.get(place);
    }

    public Upgrade getNext(int current) {
        return upgradeMap.values().stream().filter(upgrade -> upgrade.getMax() > current).findFirst().orElse(null);
    }

    public Upgrade getCurrent(int current) {
        return upgradeMap.values().stream().filter(upgrade -> upgrade.getMax() == current).findFirst().orElse(null);
    }

}
