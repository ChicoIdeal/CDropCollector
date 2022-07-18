package dev.crius.dropcollector.expiringset;

import com.google.common.base.Preconditions;
import dev.crius.dropcollector.DropCollectorPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * This class is not designed to be used in external plugins, use it at your risk.
 *
 * @param <E>
 */
public class ExpiringSet<E> extends HashSet<E> {

    private final long duration;
    private final Map<E, BukkitTask> taskMap = new HashMap<>();

    public ExpiringSet(long duration) {
        super();
        Preconditions.checkArgument(duration > 1, "Duration has to be greater than 1.");
        this.duration = duration;
    }

    @Override
    public boolean add(E e) {
        taskMap.put(e, Bukkit.getScheduler().runTaskLater(DropCollectorPlugin.getInstance(), () -> remove(e), duration));
        return super.add(e);
    }

    @Override
    public boolean remove(Object o) {
        BukkitTask task = taskMap.remove(o);
        if (task != null)
            task.cancel();

        return super.remove(o);
    }

}
