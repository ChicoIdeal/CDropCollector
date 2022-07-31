package dev.crius.dropcollector.hologram.impl;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.hologram.HologramManager;
import dev.crius.dropcollector.region.RegionManager;
import dev.crius.dropcollector.util.ChatUtils;
import dev.crius.dropcollector.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class EmptyHologramManager extends HologramManager {

    public EmptyHologramManager(DropCollectorPlugin plugin) {
        super(plugin, "Block Based Hologram Manager");
    }

    @Override
    public void create(String id, Location location, Collector collector) {

    }

    @Override
    public void remove(String id) {

    }

    @Override
    public void removeAll() {

    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (XMaterial.supports(9) && event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (!(event.getClickedBlock().getState() instanceof Skull)) return;

        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                for (Collector collector : plugin.getCollectorManager().getCollectors()) {
                    if (!player.hasPermission(RegionManager.BYPASS_PERMISSION) &&
                            !plugin.getRegionManager().canManage(player, collector)) return;
                    if (!collector.getLocation().equals(event.getClickedBlock().getLocation())) continue;

                    plugin.getCollectorManager().openMenu(player, collector);
                }
            });
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (expiringSet.contains(player.getUniqueId())) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    for (Collector collector : plugin.getCollectorManager().getCollectors()) {
                        if (!player.hasPermission(RegionManager.BYPASS_PERMISSION) &&
                                !plugin.getRegionManager().canManage(player, collector)) return;
                        if (!collector.getLocation().equals(event.getClickedBlock().getLocation())) continue;

                        plugin.getCollectorManager().breakCollector(player, collector);
                    }
                });
            } else {
                expiringSet.add(player.getUniqueId());
                plugin.getAdventure().player(player).sendMessage(ChatUtils.format(
                        plugin.getPluginConfig().getString("Messages.breaking")
                ));
            }
        }
    }

}
