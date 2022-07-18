package dev.crius.dropcollector.economy.impl;

import dev.crius.dropcollector.economy.EconomyManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class EmptyEconomyManager implements EconomyManager {
    @Override
    public String getName() {
        return "None";
    }

    @Override
    public void remove(OfflinePlayer player, double amount) {

    }

    @Override
    public void add(OfflinePlayer player, double amount) {

    }

    @Override
    public double getMoney(OfflinePlayer player) {
        return Double.MAX_VALUE;
    }

    @Override
    public void setMoney(OfflinePlayer player, double amount) {

    }
}
