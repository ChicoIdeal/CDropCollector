package dev.crius.dropcollector.gui;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.api.event.CollectorUpgradeEvent;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.upgrade.Upgrade;
import dev.crius.dropcollector.util.ChatUtils;
import dev.crius.dropcollector.xseries.XMaterial;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class UpgradeGui {

    private static final DropCollectorPlugin PLUGIN = DropCollectorPlugin.getInstance();

    public static void open(Player player, Collector collector) {
        Upgrade next = PLUGIN.getUpgradeManager().getNext(collector.getMax());
        if (next == null) {
            PLUGIN.getAdventure().player(player).sendMessage(ChatUtils.format(
                    PLUGIN.getPluginConfig().getString("Messages.limit-reached")
            ));
            player.closeInventory();
            return;
        }

        Gui gui = Gui.gui()
                .title(ChatUtils.format(PLUGIN.getPluginConfig().getString("Upgrade-Gui.title")))
                .rows(PLUGIN.getPluginConfig().getInt("Upgrade-Gui.rows"))
                .disableAllInteractions().create();

        GuiItem item = ItemBuilder.from(XMaterial.matchXMaterial(
                PLUGIN.getPluginConfig().getString("Upgrade-Gui.items.upgrade.material")).get().parseItem())
                .lore(ChatUtils.format(
                        PLUGIN.getPluginConfig().getStringList("Upgrade-Gui.items.upgrade.lore"),
                        Placeholder.unparsed("limit", ChatUtils.FORMATTER.format(collector.getMax())),
                        Placeholder.unparsed("upgraded-limit", ChatUtils.FORMATTER.format(next.getMax())),
                        Placeholder.unparsed("price", ChatUtils.FORMATTER.format(next.getPrice()))
                ))
                .name(ChatUtils.format(
                        PLUGIN.getPluginConfig().getString("Upgrade-Gui.items.upgrade.displayName"),
                        Placeholder.parsed("upgrade-displayname", next.getDisplayName())
                ))
                .asGuiItem(event -> {
                    gui.close(player);

                    Upgrade nextUpgrade = PLUGIN.getUpgradeManager().getNext(collector.getMax());
                    if (nextUpgrade == null) return;

                    if (!PLUGIN.getEconomyManager().has(player, nextUpgrade.getPrice())) {
                        PLUGIN.getAdventure().player(player).sendMessage(ChatUtils.format(
                                PLUGIN.getPluginConfig().getString("Messages.cannot-upgrade-not-enough-balance"),
                                Placeholder.unparsed("price", ChatUtils.FORMATTER.format(nextUpgrade.getPrice()))
                        ));
                        return;
                    }

                    CollectorUpgradeEvent upgradeEvent = new CollectorUpgradeEvent(collector, nextUpgrade);
                    Bukkit.getPluginManager().callEvent(upgradeEvent);
                    if (upgradeEvent.isCancelled()) return;

                    PLUGIN.getEconomyManager().remove(player, nextUpgrade.getPrice());
                    collector.setLevel(nextUpgrade);
                    PLUGIN.getAdventure().player(player).sendMessage(ChatUtils.format(
                            PLUGIN.getPluginConfig().getString("Messages.upgraded"),
                            Placeholder.unparsed("limit", ChatUtils.FORMATTER.format(nextUpgrade.getMax()))
                    ));

                });

        gui.setItem(PLUGIN.getPluginConfig().getInt("Upgrade-Gui.items.upgrade.slot"), item);

        gui.open(player);

    }

}
