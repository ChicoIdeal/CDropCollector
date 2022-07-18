package dev.crius.dropcollector.economy;

import dev.crius.dropcollector.hook.Hook;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface EconomyManager extends Hook {

    default void init() {}

    void remove(OfflinePlayer player, double amount);

    void add(OfflinePlayer player, double amount);

    double getMoney(OfflinePlayer player);

    void setMoney(OfflinePlayer player, double amount);

    default boolean has(OfflinePlayer player, double amount) {
        return getMoney(player) >= amount;
    }

}
