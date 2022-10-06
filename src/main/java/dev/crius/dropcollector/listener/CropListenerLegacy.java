package dev.crius.dropcollector.listener;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.xseries.XMaterial;
import lombok.RequiredArgsConstructor;
import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.Crops;

import java.util.Objects;

/**
 * Everything in this class designed to be used in legacy (-1.13) versions and should never be used in newer versions.
 */
@SuppressWarnings("deprecation")
@RequiredArgsConstructor
public class CropListenerLegacy implements Listener {

    private final DropCollectorPlugin plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onGrow(BlockGrowEvent event) {
        if (!plugin.getPluginConfig().getStringList("Settings.enabled-worlds")
                .contains(event.getBlock().getWorld().getName())) return;

        if (XMaterial.supports(13)) return;
        if (!plugin.getPluginConfig().getBoolean("Settings.auto-harvest")) return;
        BlockState state = event.getNewState();
        if (!plugin.getPluginConfig().getStringList("Settings.auto-harvest-crops").contains(state.getType().name())) return;

        boolean allowed = true;

        if (plugin.getPluginConfig().getBoolean("Settings.auto-harvest-require-collector")) {
            allowed = !plugin.getCollectorManager().getCollectors(event.getBlock().getLocation()).isEmpty();
        }

        if (!allowed) return;
        plugin.getLogger().info(state.getType().name());
        if (state.getData() instanceof Crops) {
            Crops crops = (Crops) state.getData();
            if (crops.getState() == CropState.RIPE) {
                state.getBlock().getDrops().forEach(item -> state.getBlock().getWorld().dropItemNaturally(state.getLocation(), item));
                crops.setState(CropState.SEEDED);
            }
        } else if (state.getType() == Material.CACTUS) {
            state.getWorld().dropItemNaturally(state.getLocation(), new ItemStack(Material.CACTUS));
            event.setCancelled(true);
        } else if (state.getType() == Material.valueOf("SUGAR_CANE_BLOCK")) {
            state.getWorld().dropItemNaturally(state.getLocation(), new ItemStack(Material.SUGAR_CANE));
            event.setCancelled(true);
        } else if (state.getType() == Material.COCOA) {
            if (state.getRawData() > 8) {
                state.getWorld().dropItemNaturally(state.getLocation(), Objects.requireNonNull(XMaterial.COCOA_BEANS.parseItem()));
                CocoaPlant cp = (CocoaPlant) state.getData();
                cp.setSize(CocoaPlant.CocoaPlantSize.SMALL);
                state.setData(cp);
            }
        } else if (state.getType() == Material.CARROT) {
            if (state.getRawData() > 6) {
                state.getWorld().dropItemNaturally(state.getLocation(), Objects.requireNonNull(XMaterial.CARROT.parseItem()));
                state.setRawData((byte) 0);
            }
        } else if (state.getType() == Material.valueOf("MELON_BLOCK")) {
            state.getWorld().dropItemNaturally(state.getLocation(), new ItemStack(Material.MELON, 3));
            event.setCancelled(true);
        } else if (state.getType() == Material.PUMPKIN) {
            state.getWorld().dropItemNaturally(state.getLocation(), new ItemStack(Material.PUMPKIN));
            event.setCancelled(true);
        }
    }

}
