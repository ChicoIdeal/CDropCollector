package dev.crius.dropcollector.database.impl.yaml;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.collector.CollectedItem;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.database.Database;
import dev.crius.dropcollector.util.LocationUtils;
import dev.crius.dropcollector.xseries.XMaterial;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class YamlDatabase implements Database {

    private static final String DATA_DIRECTORY = DropCollectorPlugin.getInstance().getDataFolder() + File.separator + "data";
    private static final File DATA_FOLDER = new File(DATA_DIRECTORY);

    private final DropCollectorPlugin plugin;

    @Override
    public void onEnable() {
        if (!DATA_FOLDER.exists() && !DATA_FOLDER.mkdirs()) {
            plugin.log("Could not create data folder!", Level.SEVERE);
            return;
        }

        plugin.debug("Loading collectors...");
        for (Collector collector : getCollectors()) {
            plugin.debug("Loading collector: " + collector.getId());
            plugin.getCollectorManager().addCollector(collector, false);
            plugin.debug("Loaded collector: " + collector.getId());
        }
        plugin.debug("Loaded collectors.");
    }

    @Override
    public void saveAll() {
        plugin.debug("Saving collectors...");
        for (Collector collector : plugin.getCollectorManager().getCollectors()) {
            saveCollector(collector);
        }
        plugin.debug("Saved collectors.");
    }

    @Override
    public void saveCollector(Collector collector) {
        plugin.debug("Saving collector: " + collector.getId());
        YamlData data = new YamlData(DATA_DIRECTORY, collector.getId().toString());
        data.create();
        data.set("location", LocationUtils.getLocation(collector.getLocation()));
        data.set("level", collector.getLevel().getPlace());
        data.set("owner", collector.getOwner().toString());
        data.set("enabled", collector.isEnabled());
        data.set("entity", collector.getEntity().getName());
        data.set("auto-sell", collector.isAutoSellEnabled());
        for (Map.Entry<XMaterial, CollectedItem> entry : collector.getItemMap().entrySet()) {
            data.set("collected." + entry.getKey().name(), entry.getValue().getAmount());
        }
        data.save();
        plugin.debug("Saved collector: " + collector.getId());
    }

    @Override
    public Collection<Collector> getCollectors() {
        final File[] files = new File(DATA_DIRECTORY).listFiles();
        if (files == null) return null;

        List<Collector> collectors = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory() || !file.getName().endsWith(".yml")) continue;

            Collector collector = new Collector(file);
            collectors.add(collector);
        }

        return collectors;
    }

    @Override
    public Collection<Collector> getCollectors(UUID uuid) {
        return getCollectors().stream().filter(collector -> collector.getOwner().equals(uuid)).collect(Collectors.toList());
    }

    @Override
    public void remove(Collector collector) {
        new File(DATA_FOLDER, collector.getId().toString() + ".yml").delete();
    }

    @Override
    public void removeAll() {
        DATA_FOLDER.delete();
    }
}
