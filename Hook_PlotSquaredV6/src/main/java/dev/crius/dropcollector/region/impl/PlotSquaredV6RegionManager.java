package dev.crius.dropcollector.region.impl;

import com.google.common.eventbus.Subscribe;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.events.PlotDeleteEvent;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.entity.CEntity;
import dev.crius.dropcollector.region.RegionManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
public class PlotSquaredV6RegionManager implements RegionManager {

    private final static PlotAPI PLOT_API = new PlotAPI();

    private final DropCollectorPlugin plugin;

    @Override
    public void init() {
        PLOT_API.registerListener(this);
    }

    @Override
    public String getName() {
        return "PlotSquared v6";
    }

    @Override
    public boolean canManage(Player player, Collector collector) {
        com.plotsquared.core.location.Location location = toPSLocation(collector.getLocation());
        PlotArea plotArea = PLOT_API.getPlotSquared().getPlotAreaManager().getPlotArea(location);
        if (plotArea == null) return false;
        Plot plot = plotArea.getPlot(location);
        if (plot == null) return false;
        if (plot.isOwner(player.getUniqueId())) return true;
        if (plugin.getPluginConfig().getBoolean("Settings.allow-coops-to-manage")
                && plot.getTrusted().contains(player.getUniqueId())) return true;

        return plot.getTrusted().contains(player.getUniqueId());
    }

    @Override
    public Collector getCollector(Location location, CEntity entity) {
        com.plotsquared.core.location.Location loc = toPSLocation(location);
        PlotArea plotArea = PLOT_API.getPlotSquared().getPlotAreaManager().getPlotArea(loc);
        if (plotArea == null) return null;
        Plot plot = plotArea.getPlot(loc);
        if (plot == null) return null;

        for (Collector collector : plugin.getCollectorManager().getCollectorsByEntity(entity)) {
            Location l = collector.getLocation();
            com.plotsquared.core.location.Location loc1 = toPSLocation(l);
            PlotArea pa = PLOT_API.getPlotSquared().getPlotAreaManager().getPlotArea(loc1);
            if (pa == null) continue;
            if (!plotArea.equals(pa)) continue;
            Plot p = plotArea.getPlot(loc1);
            if (!plot.equals(p)) continue;

            return collector;
        }

        return null;
    }

    @Override
    public Set<Collector> getCollectors(Location location) {
        Set<Collector> collectors = new HashSet<>();

        com.plotsquared.core.location.Location loc = toPSLocation(location);
        PlotArea plotArea = PLOT_API.getPlotSquared().getPlotAreaManager().getPlotArea(loc);
        if (plotArea == null) return collectors;
        Plot plot = plotArea.getPlot(loc);
        if (plot == null) return collectors;

        for (Collector collector : plugin.getCollectorManager().getCollectors()) {
            Location l = collector.getLocation();
            com.plotsquared.core.location.Location loc1 = toPSLocation(l);
            PlotArea pa = PLOT_API.getPlotSquared().getPlotAreaManager().getPlotArea(loc1);
            if (pa == null) continue;
            if (!plotArea.equals(pa)) continue;
            Plot p = plotArea.getPlot(loc1);
            if (!plot.equals(p)) continue;

            collectors.add(collector);
        }

        return collectors;
    }

    private com.plotsquared.core.location.Location toPSLocation(Location location) {
        return com.plotsquared.core.location.Location.at(
                Objects.requireNonNull(location.getWorld()).getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    private Location toBukkitLocation(com.plotsquared.core.location.Location location) {
        return new Location(
                Bukkit.getWorld(location.getWorld().getName()),
                location.getX(),
                location.getY(),
                location.getZ()
        );
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onPlotDelete(PlotDeleteEvent event) {
        event.getPlot().getCenter(location -> getCollectors(
                toBukkitLocation(location)
        ).forEach(plugin.getCollectorManager()::removeCollector));
    }

}
