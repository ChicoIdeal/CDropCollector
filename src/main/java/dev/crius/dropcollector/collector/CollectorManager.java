package dev.crius.dropcollector.collector;

import de.tr7zw.changeme.nbtapi.NBTItem;
import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.entity.CEntity;
import dev.crius.dropcollector.gui.CollectorGui;
import dev.crius.dropcollector.util.ChatUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CollectorManager {

    private final DropCollectorPlugin plugin;
    private final Map<UUID, Collector> collectorMap = new ConcurrentHashMap<>();

    public Collection<Collector> getCollectors() {
        return Collections.unmodifiableCollection(collectorMap.values());
    }

    public Collection<Collector> getCollectorsByEntity(CEntity entity) {
        return collectorMap.values().stream().filter(collector -> collector.getEntity().equals(entity)).collect(Collectors.toList());
    }

    public void removeCollector(Collector collector) {
        plugin.getHologramManager().remove(collector);
        collectorMap.remove(collector.getId());
        plugin.getPluginDatabase().remove(collector);
    }

    public void breakCollector(Player player, Collector collector) {
        removeCollector(collector);

        ItemStack item = createCreator(collector.getEntity(), player.getUniqueId());
        player.getInventory().addItem(item);
    }

    /**
     *
     * @param collector The Collector Object
     * @param save Should we save the collector immediately?
     */
    public void addCollector(Collector collector, boolean save) {
        collectorMap.put(collector.getId(), collector);

        plugin.getHologramManager().create(
                collector.getId().toString(),
                collector.getLocation().clone().add(0, 3, 0),
                collector
        );

        collector.setHologramId(collector.getId().toString());

        if (save)
            plugin.getPluginDatabase().saveCollector(collector);
    }

    /**
     * Creates a collector creator with the specified entity.
     *
     * @param entity Entity of the creator
     * @param owner Owner of the creator
     * @return Creator Item
     */
    public ItemStack createCreator(CEntity entity, UUID owner) {
        ItemStack item = ItemBuilder.skull(entity.getHead().clone())
                .lore(
                        ChatUtils.format(plugin.getPluginConfig().getStringList("Creator-Item.lore"),
                                Placeholder.unparsed("entity-displayname", entity.getDisplayName()))
                )
                .name(
                        ChatUtils.format(plugin.getPluginConfig().getString("Creator-Item.displayName"),
                                Placeholder.unparsed("entity-displayname", entity.getDisplayName()))
                )
                .glow(plugin.getPluginConfig().getBoolean("Creator-Item.glow"))
                .build();

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setBoolean("dropcollector:creator", true);
        nbtItem.setString("dropcollector:type", entity.getName());
        nbtItem.setString("dropcollector:owner", owner.toString());
        nbtItem.setString("dropcollector:id", UUID.randomUUID().toString());

        return nbtItem.getItem();
    }

    public Set<Collector> getCollectors(Location location) {
        return plugin.getRegionManager().getCollectors(location);
    }

    public Collector getCollector(Location location, CEntity entity) {
        return plugin.getRegionManager().getCollector(location, entity);
    }

    public Collector getCollector(String holoId) {
        return getCollectors().stream().filter(collector ->
                collector.getHologramId().equals(holoId)).findFirst().orElse(null);
    }

    public Collector getCollector(UUID uuid) {
        return collectorMap.get(uuid);
    }

    public void openMenu(Player player, Collector collector) {
        CollectorGui.open(player, collector);
    }

}
