package dev.crius.dropcollector.command;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.entity.CEntity;
import dev.crius.dropcollector.util.ChatUtils;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
@RequiredArgsConstructor
@Command(value = "dropcollector", alias = "cdropcollector")
public class BuyCommand extends BaseCommand {

    private final DropCollectorPlugin plugin;

    @SubCommand("buy")
    @Permission("dropcollector.command.buy")
    public void buyCommand(Player player, CEntity entity) {
        if (player.getInventory().firstEmpty() == -1) {
            plugin.getAdventure().player(player).sendMessage(ChatUtils.format(
                    plugin.getPluginConfig().getString("Messages.inventory-full")
            ));
            return;
        }

        double price = entity.getPrice();
        if (!plugin.getEconomyManager().has(player, price)) {
            plugin.getAdventure().player(player).sendMessage(ChatUtils.format(
                    plugin.getPluginConfig().getString("Messages.cannot-buy-not-enough-balance"),
                    Placeholder.unparsed("required", ChatUtils.FORMATTER.format(price))
            ));
            return;
        }

        plugin.getEconomyManager().remove(player, price);

        player.getInventory().addItem(plugin.getCollectorManager().createCreator(entity, player.getUniqueId()));
        plugin.getAdventure().player(player).sendMessage(ChatUtils.format(
                plugin.getPluginConfig().getString("Messages.bought-a-creator"),
                Placeholder.unparsed("entity-displayname", entity.getDisplayName()),
                Placeholder.unparsed("price", ChatUtils.FORMATTER.format(price))
        ));
    }

}
