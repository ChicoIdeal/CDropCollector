package dev.crius.dropcollector.listener;

import dev.crius.dropcollector.DropCollectorPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.security.SecureRandom;
import java.util.Random;

@RequiredArgsConstructor
public class EntityListener implements Listener {

    private static final Random RANDOM = new SecureRandom();

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

        event.getEntity().damage(1000);

        if (event.getEntityType() == EntityType.BLAZE) {
            if (RANDOM.nextInt(100) >= 50)
                event.getEntity().getWorld().dropItemNaturally(event.getLocation(), new ItemStack(Material.BLAZE_ROD));
        }

    }

}
