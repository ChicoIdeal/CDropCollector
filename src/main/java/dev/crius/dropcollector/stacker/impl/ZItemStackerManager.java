package dev.crius.dropcollector.stacker.impl;

import dev.crius.dropcollector.stacker.StackerManager;
import fr.maxlego08.zitemstacker.api.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.plugin.RegisteredServiceProvider;

public class ZItemStackerManager implements StackerManager {

    private final ItemManager itemManager = getProvider();

    private ItemManager getProvider() {
        RegisteredServiceProvider<ItemManager> provider = Bukkit.getServer().getServicesManager().getRegistration(ItemManager.class);
        if (provider == null) {
            throw new RuntimeException("Could not get provider for ItemManager!");
        }

        return provider.getProvider();
    }

    @Override
    public String getName() {
        return "zItemStacker";
    }

    @Override
    public int getRealAmount(Item item) {
        return itemManager.getItemAmount(item);
    }

    @Override
    public void setAmount(Item item, int amount) {
        itemManager.setAmount(item, amount);
    }

}
