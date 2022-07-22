package dev.crius.dropcollector.listener;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.util.ChatUtils;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

@RequiredArgsConstructor
public class EntityListener implements Listener {

    private final DropCollectorPlugin plugin;

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        if (!plugin.getPluginConfig().getStringList("Settings.enabled-worlds")
                .contains(event.getEntity().getWorld().getName())) return;

        if (!plugin.getPluginConfig().getBoolean("Settings.auto-kill")) return;
        if (!plugin.getPluginConfig().getStringList("Settings.auto-kill-mobs").contains(event.getEntityType().name())) return;

        boolean allowed = true;

        if (plugin.getPluginConfig().getBoolean("Settings.auto-kill-require-collector")) {
            allowed = !plugin.getCollectorManager().getCollectors(event.getEntity().getLocation()).isEmpty();
        }

        if (!allowed) return;

        if (event.getEntity() instanceof Mob) {
            ((Mob) event.getEntity()).damage(1000);
            event.setCancelled(true);
        }
    }

}
