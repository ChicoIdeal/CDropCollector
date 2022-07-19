package dev.crius.dropcollector.task;

import dev.crius.dropcollector.DropCollectorPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class AutoSaveTask extends BukkitRunnable {

    private final DropCollectorPlugin plugin;

    @Override
    public void run() {
        plugin.getPluginDatabase().saveAll();
    }

}
