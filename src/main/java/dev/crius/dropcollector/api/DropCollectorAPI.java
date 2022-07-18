package dev.crius.dropcollector.api;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.collector.CollectorManager;
import dev.crius.dropcollector.economy.EconomyManager;
import dev.crius.dropcollector.entity.EntityManager;
import dev.crius.dropcollector.hologram.HologramManager;
import dev.crius.dropcollector.region.RegionManager;
import dev.crius.dropcollector.shop.ShopManager;
import dev.crius.dropcollector.upgrade.UpgradeManager;

public class DropCollectorAPI {

    public static CollectorManager getCollectorManager() {
        return getInstance().getCollectorManager();
    }

    public static EntityManager getEntityManager() {
        return getInstance().getEntityManager();
    }

    public static UpgradeManager getUpgradeManager() {
        return getInstance().getUpgradeManager();
    }

    public static EconomyManager getEconomyManager() {
        return getInstance().getEconomyManager();
    }

    public static HologramManager getHologramManager() {
        return getInstance().getHologramManager();
    }

    public static ShopManager getShopManager() {
        return getInstance().getShopManager();
    }

    public static RegionManager getRegionManager() {
        return getInstance().getRegionManager();
    }

    public static DropCollectorPlugin getInstance() {
        return DropCollectorPlugin.getInstance();
    }

}
