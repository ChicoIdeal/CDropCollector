package dev.crius.dropcollector.economy.impl;

import dev.crius.dropcollector.economy.EconomyManager;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerPointsEconomyManager implements EconomyManager {

    private final PlayerPointsAPI pointsAPI = PlayerPoints.getInstance().getAPI();

    @Override
    public String getName() {
        return "PlayerPoints";
    }

    @Override
    public void remove(OfflinePlayer player, double amount) {
        pointsAPI.take(player.getUniqueId(), (int) amount);
    }

    @Override
    public void add(OfflinePlayer player, double amount) {
        pointsAPI.give(player.getUniqueId(), (int) amount);
    }

    @Override
    public double getMoney(OfflinePlayer player) {
        return pointsAPI.look(player.getUniqueId());
    }

    @Override
    public void setMoney(OfflinePlayer player, double amount) {
        pointsAPI.set(player.getUniqueId(), (int) amount);
    }

}
