package dev.crius.dropcollector.listener;

import dev.crius.dropcollector.DropCollectorPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

@RequiredArgsConstructor
public class EntityListener implements Listener {

    private final DropCollectorPlugin plugin;

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!plugin.getPluginConfig().getStringList("Settings.enabled-worlds")
                .contains(event.getEntity().getWorld().getName())) return;

        if (!plugin.getPluginConfig().getBoolean("Settings.auto-kill")) return;
        if (!plugin.getPluginConfig().getStringList("Settings.auto-kill-mobs").contains(event.getEntityType().name())) return;
        if (!plugin.getPluginConfig().getStringList("Settings.auto-kill-reasons")
                .contains(event.getSpawnReason().name())) return;

        boolean allowed = true;

        if (plugin.getPluginConfig().getBoolean("Settings.auto-kill-require-collector")) {
            allowed = !plugin.getCollectorManager().getCollectors(event.getEntity().getLocation()).isEmpty();
        }

        if (!allowed) return;

        if (event.getEntity() instanceof Mob) {
            event.getEntity().damage(1000);
            event.setCancelled(true);
        }
    }

}
