package dev.crius.dropcollector.hologram;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.entity.CEntity;
import dev.crius.dropcollector.expiringset.ExpiringSet;
import dev.crius.dropcollector.hook.Hook;
import org.bukkit.Location;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.UUID;

public abstract class HologramManager implements Listener, Hook {

    private final String name;
    protected final DropCollectorPlugin plugin;
    protected final ExpiringSet<UUID> expiringSet = new ExpiringSet<>(40);

    protected HologramManager(DropCollectorPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getName() {
        return name;
    }

    public abstract void create(String id, Location location, Collector collector);

    public abstract void remove(Collector collector);

    public abstract void removeAll();

}
