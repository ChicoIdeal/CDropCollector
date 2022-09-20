package dev.crius.dropcollector.listener;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.xseries.XMaterial;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

@RequiredArgsConstructor
public class CropListener implements Listener {

    private final DropCollectorPlugin plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onGrow(BlockGrowEvent event) {
        // we will do our things after one tick, so we can get the updated state
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!plugin.getPluginConfig().getStringList("Settings.enabled-worlds")
                    .contains(event.getBlock().getWorld().getName())) return;

            if (!XMaterial.supports(13)) return;
            if (!plugin.getPluginConfig().getBoolean("Settings.auto-harvest")) return;
            BlockState state = event.getNewState();
            if (!plugin.getPluginConfig().getStringList("Settings.auto-harvest-crops")
                    .contains(state.getBlockData().getMaterial().name())) return;

            boolean allowed = true;

            if (plugin.getPluginConfig().getBoolean("Settings.auto-harvest-require-collector")) {
                allowed = !plugin.getCollectorManager().getCollectors(event.getBlock().getLocation()).isEmpty();
            }

            if (!allowed) return;
            if (!(state.getBlockData() instanceof Ageable)) return;

            Ageable ageable = (Ageable) state.getBlockData();
            // whenever a cactus grows, its age is 0 and not the maximum age.
            if (ageable.getAge() >= ageable.getMaximumAge() || state.getBlockData().getMaterial() == Material.CACTUS)
                state.getBlock().breakNaturally();
        }, 1);
    }

}
