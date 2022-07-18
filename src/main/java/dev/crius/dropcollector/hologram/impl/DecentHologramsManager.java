package dev.crius.dropcollector.hologram.impl;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.hologram.HologramManager;
import dev.crius.dropcollector.region.RegionManager;
import dev.crius.dropcollector.util.ChatUtils;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.actions.Action;
import eu.decentsoftware.holograms.api.actions.ActionType;
import eu.decentsoftware.holograms.api.actions.ClickType;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.event.HologramClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.Map;

public class DecentHologramsManager extends HologramManager {

    // this is required to make a hologram clickable
    private static final Action ACTION = new Action(ActionType.PAGE, "");

    private final Map<String, Hologram> hologramMap = new HashMap<>();

    public DecentHologramsManager(DropCollectorPlugin plugin) {
        super(plugin, "DecentHolograms");
    }

    @Override
    public void create(String id, Location location, Collector collector) {
        Hologram hologram = DHAPI.createHologram(id, location);

        DHAPI.addHologramLine(hologram, collector.getEntity().getHead());

        collector.getHoloLines().forEach(line -> DHAPI.addHologramLine(hologram, line));

        hologramMap.put(id, hologram);

        hologram.getPage(0).addAction(ClickType.RIGHT, ACTION);
    }

    @Override
    public void remove(String id) {
        Hologram hologram = hologramMap.remove(id);
        if (hologram != null)
            hologram.delete();
    }

    @Override
    public void removeAll() {
        hologramMap.values().forEach(Hologram::delete);
    }

    @EventHandler
    public void onClick(HologramClickEvent event) {
        Player player = event.getPlayer();

        Hologram hologram = this.hologramMap.values().stream().filter(holo ->
                holo.getId().equals(event.getHologram().getId())).findFirst().orElse(null);

        if (hologram == null) return;

        Collector collector = plugin.getCollectorManager().getCollector(hologram.getId());
        if (!player.hasPermission(RegionManager.BYPASS_PERMISSION) &&
                !plugin.getRegionManager().canManage(player, collector)) return;

        if (event.getClick() != ClickType.SHIFT_LEFT) {
            Bukkit.getScheduler().runTask(plugin, () -> plugin.getCollectorManager().openMenu(player, collector));
        } else {
            if (expiringSet.contains(player.getUniqueId())) {
                Bukkit.getScheduler().runTask(plugin, () -> plugin.getCollectorManager().breakCollector(player, collector));
            } else {
                expiringSet.add(player.getUniqueId());
                plugin.getAdventure().player(player).sendMessage(ChatUtils.format(
                        plugin.getPluginConfig().getString("Messages.breaking")
                ));
            }
        }
    }

}
