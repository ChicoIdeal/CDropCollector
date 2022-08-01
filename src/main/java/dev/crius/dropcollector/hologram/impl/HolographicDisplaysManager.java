package dev.crius.dropcollector.hologram.impl;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TouchableLine;
import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.hologram.HologramManager;
import dev.crius.dropcollector.region.RegionManager;
import dev.crius.dropcollector.util.ChatUtils;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class HolographicDisplaysManager extends HologramManager {

    private final Map<String, Hologram> hologramMap = new HashMap<>();

    public HolographicDisplaysManager(DropCollectorPlugin plugin) {
        super(plugin, "HolographicDisplays");
    }

    @Override
    public void create(String id, Location location, Collector collector) {
        Hologram hologram = HologramsAPI.createHologram(plugin, location);

        hologram.appendItemLine(collector.getEntity().getHead());

        collector.getHoloLines().forEach(l -> {
            TouchableLine line = hologram.appendTextLine(l);
            line.setTouchHandler(player -> {
                if (!player.hasPermission(RegionManager.BYPASS_PERMISSION) &&
                        !plugin.getRegionManager().canManage(player, collector)) return;

                if (!player.isSneaking()) {
                    plugin.getCollectorManager().openMenu(player, collector);
                } else {
                    if (expiringSet.contains(player.getUniqueId())) {
                        plugin.getCollectorManager().breakCollector(player, collector);
                    } else {
                        expiringSet.add(player.getUniqueId());
                        plugin.getAdventure().player(player).sendMessage(ChatUtils.format(
                                plugin.getPluginConfig().getString("Messages.breaking")
                        ));
                    }
                }
            });
        });

        hologram.getVisibilityManager().setVisibleByDefault(true);

        hologramMap.put(id, hologram);
    }

    @Override
    public void remove(Collector collector) {
        Hologram hologram = hologramMap.remove(collector.getHologramId());
        if (hologram != null)
            hologram.delete();
    }

    @Override
    public void removeAll() {
        hologramMap.values().forEach(Hologram::delete);
    }

}
