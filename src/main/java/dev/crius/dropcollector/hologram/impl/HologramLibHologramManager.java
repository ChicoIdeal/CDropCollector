package dev.crius.dropcollector.hologram.impl;

import com.github.unldenis.hologram.Hologram;
import com.github.unldenis.hologram.HologramPool;
import com.github.unldenis.hologram.event.PlayerHologramInteractEvent;
import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.hologram.HologramManager;
import dev.crius.dropcollector.region.RegionManager;
import dev.crius.dropcollector.util.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class HologramLibHologramManager extends HologramManager {

    private final HologramPool hologramPool;
    private final Map<String, Hologram> hologramMap = new HashMap<>();

    public HologramLibHologramManager(DropCollectorPlugin plugin) {
        super(plugin, "HologramLib");
        hologramPool = new HologramPool(plugin, 100, 0, 5);
    }

    @Override
    public void create(String id, Location location, Collector collector) {
        Hologram.Builder builder = Hologram.builder().location(location);

        builder.addLine(collector.getEntity().getHead());
        collector.getHoloLines().forEach(line -> builder.addLine(line, true));

        hologramMap.put(id, builder.build(hologramPool));
    }

    @Override
    public void remove(String id) {
        Hologram hologram = hologramMap.remove(id);
        if (hologram != null)
            hologramPool.remove(hologram);
    }

    @Override
    public void removeAll() {
        hologramMap.values().forEach(hologramPool::remove);
    }

    @EventHandler
    public void onClick(PlayerHologramInteractEvent event) {
        Player player = event.getPlayer();

        Hologram hologram = event.getHologram();

        UUID id = UUID.fromString(Objects.requireNonNull(this.hologramMap.entrySet().stream().filter(entry ->
                entry.getValue().equals(hologram)).findFirst().orElse(null)).getKey());

        Collector collector = plugin.getCollectorManager().getCollector(id);
        if (!player.hasPermission(RegionManager.BYPASS_PERMISSION) &&
                !plugin.getRegionManager().canManage(player, collector)) return;

        if (!event.getPlayer().isSneaking()) {
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
