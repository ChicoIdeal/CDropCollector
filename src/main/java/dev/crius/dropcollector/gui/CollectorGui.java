package dev.crius.dropcollector.gui;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.api.event.CollectorSellEvent;
import dev.crius.dropcollector.collector.CollectedItem;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.economy.impl.EmptyEconomyManager;
import dev.crius.dropcollector.util.ChatUtils;
import dev.crius.dropcollector.util.ItemUtils;
import dev.crius.dropcollector.xseries.XMaterial;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class CollectorGui {

    private static final DropCollectorPlugin PLUGIN = DropCollectorPlugin.getInstance();

    // not a good solution but better than a yellow color everywhere, it can break the gui if configuration is wrong
    @SuppressWarnings("ConstantConditions")
    public static void open(Player player, Collector collector) {
        PaginatedGui gui = Gui.paginated().title(
                ChatUtils.format(PLUGIN.getPluginConfig().getString("Gui.title"),
                        Placeholder.unparsed("entity-displayname", collector.getEntity().getDisplayName())))
                .rows(PLUGIN.getPluginConfig().getInt("Gui.rows"))
                .pageSize(PLUGIN.getPluginConfig().getInt("Gui.pageSize"))
                .disableAllInteractions()
                .create();

        List<Integer> fillerSlots = PLUGIN.getPluginConfig().getIntegerList("Gui.fillerSlots");

        gui.setDefaultTopClickAction(event -> {
            if (gui.getGuiItem(event.getRawSlot()) == null) return;
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            if (fillerSlots.contains(event.getRawSlot())) return;

            PLUGIN.getAdventure().player(player)
                    .playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.MASTER, 1f, 1f));
        });

        for (Integer slot : fillerSlots) {
            gui.setItem(slot, ItemBuilder.from(
                    XMaterial.matchXMaterial(PLUGIN.getPluginConfig()
                            .getString("Gui.fillerMaterial")).orElse(null).parseItem()
            ).name(Component.text(" ")).asGuiItem());
        }

        GuiItem previous = ItemBuilder.from(
                XMaterial.matchXMaterial(PLUGIN.getPluginConfig()
                        .getString("Gui.items.previous.material")).orElse(null).parseItem())
                .name(ChatUtils.format(PLUGIN.getPluginConfig().getString("Gui.items.previous.displayName")))
                .lore(ChatUtils.format(PLUGIN.getPluginConfig().getStringList("Gui.items.previous.lore")))
                .asGuiItem((event) -> gui.previous());
        gui.setItem(PLUGIN.getPluginConfig().getInt("Gui.items.previous.slot"), previous);

        GuiItem next = ItemBuilder.from(
                        XMaterial.matchXMaterial(PLUGIN.getPluginConfig()
                                .getString("Gui.items.next.material")).orElse(null).parseItem())
                .name(ChatUtils.format(PLUGIN.getPluginConfig().getString("Gui.items.next.displayName")))
                .lore(ChatUtils.format(PLUGIN.getPluginConfig().getStringList("Gui.items.next.lore")))
                .asGuiItem((event) -> gui.next());
        gui.setItem(PLUGIN.getPluginConfig().getInt("Gui.items.next.slot"), next);

        if (!(PLUGIN.getEconomyManager() instanceof EmptyEconomyManager)) {
            GuiItem sellAll = ItemBuilder.from(
                            XMaterial.matchXMaterial(PLUGIN.getPluginConfig()
                                    .getString("Gui.items.sell_all.material")).orElse(null).parseItem())
                    .name(ChatUtils.format(PLUGIN.getPluginConfig().getString("Gui.items.sell_all.displayName")))
                    .lore(ChatUtils.format(PLUGIN.getPluginConfig().getStringList("Gui.items.sell_all.lore")))
                    .asGuiItem((event) -> {
                        if (collector.getTotal() <= 0) return;

                        double total = 0;
                        for (CollectedItem collectedItem : collector.getItemMap().values()) {
                            CollectorSellEvent sellEvent = new CollectorSellEvent(collector, collectedItem,
                                    collectedItem.getAmount(), false);
                            Bukkit.getPluginManager().callEvent(sellEvent);
                            if (sellEvent.isCancelled()) continue;

                            total += collectedItem.getItem().getPrice() * collectedItem.getAmount();
                            collectedItem.setAmount(0);
                        }
                        total = total - (PLUGIN.getPluginConfig().getInt("Settings.tax") * total / 100);

                        PLUGIN.getEconomyManager().add(player, total);
                        PLUGIN.getAdventure().player(player).sendMessage(ChatUtils.format(
                                PLUGIN.getPluginConfig().getString("Messages.sold-all"),
                                Placeholder.unparsed("price", ChatUtils.FORMATTER.format(total))
                        ));

                        open(player, collector);
                    });
            gui.setItem(PLUGIN.getPluginConfig().getInt("Gui.items.sell_all.slot"), sellAll);

            GuiItem upgrade = ItemBuilder.from(
                            XMaterial.matchXMaterial(PLUGIN.getPluginConfig()
                                    .getString("Gui.items.upgrade.material")).orElse(null).parseItem())
                    .name(ChatUtils.format(PLUGIN.getPluginConfig().getString("Gui.items.upgrade.displayName")))
                    .lore(ChatUtils.format(PLUGIN.getPluginConfig().getStringList("Gui.items.upgrade.lore")))
                    .asGuiItem((event) -> UpgradeGui.open(player, collector));
            gui.setItem(PLUGIN.getPluginConfig().getInt("Gui.items.upgrade.slot"), upgrade);

            if (collector.isAutoSellEnabled()) {
                GuiItem disableAutoSell = ItemBuilder.from(
                                XMaterial.matchXMaterial(PLUGIN.getPluginConfig()
                                        .getString("Gui.items.auto-sell-enabled.material")).orElse(null).parseItem())
                        .name(ChatUtils.format(PLUGIN.getPluginConfig().getString("Gui.items.auto-sell-enabled.displayName")))
                        .lore(ChatUtils.format(PLUGIN.getPluginConfig().getStringList("Gui.items.auto-sell-enabled.lore")))
                        .asGuiItem((event) -> {
                            collector.setAutoSellEnabled(false);
                            PLUGIN.getAdventure().player(player).sendMessage(ChatUtils.format(
                                    PLUGIN.getPluginConfig().getString("Messages.disabled-auto-sell")
                            ));

                            open(player, collector);
                        });
                gui.setItem(PLUGIN.getPluginConfig().getInt("Gui.items.auto-sell-enabled.slot"), disableAutoSell);
            } else {
                GuiItem enableAutoSell = ItemBuilder.from(
                                XMaterial.matchXMaterial(PLUGIN.getPluginConfig()
                                        .getString("Gui.items.auto-sell-disabled.material")).orElse(null).parseItem())
                        .name(ChatUtils.format(PLUGIN.getPluginConfig().getString("Gui.items.auto-sell-disabled.displayName")))
                        .lore(ChatUtils.format(PLUGIN.getPluginConfig().getStringList("Gui.items.auto-sell-disabled.lore")))
                        .asGuiItem((event) -> {
                            collector.setAutoSellEnabled(true);
                            PLUGIN.getAdventure().player(player).sendMessage(ChatUtils.format(
                                    PLUGIN.getPluginConfig().getString("Messages.enabled-auto-sell")
                            ));

                            open(player, collector);
                        });
                gui.setItem(PLUGIN.getPluginConfig().getInt("Gui.items.auto-sell-disabled.slot"), enableAutoSell);
            }
        }

        GuiItem info = ItemBuilder.from(
                        XMaterial.matchXMaterial(PLUGIN.getPluginConfig()
                                .getString("Gui.items.info.material")).orElse(null).parseItem())
                .name(ChatUtils.format(PLUGIN.getPluginConfig().getString("Gui.items.info.displayName")))
                .lore(ChatUtils.format(
                        PLUGIN.getPluginConfig().getStringList("Gui.items.info.lore"),
                        Placeholder.unparsed("current", ChatUtils.FORMATTER.format(collector.getTotal())),
                        Placeholder.unparsed("max", ChatUtils.FORMATTER.format(collector.getMax()))
                ))
                .asGuiItem();
        gui.setItem(PLUGIN.getPluginConfig().getInt("Gui.items.info.slot"), info);

        if (collector.isEnabled()) {
            GuiItem disable = ItemBuilder.from(
                            XMaterial.matchXMaterial(PLUGIN.getPluginConfig()
                                    .getString("Gui.items.status-enabled.material")).orElse(null).parseItem())
                    .name(ChatUtils.format(PLUGIN.getPluginConfig().getString("Gui.items.status-enabled.displayName")))
                    .lore(ChatUtils.format(PLUGIN.getPluginConfig().getStringList("Gui.items.status-enabled.lore")))
                    .asGuiItem((event) -> {
                        collector.setEnabled(false);
                        PLUGIN.getAdventure().player(player).sendMessage(ChatUtils.format(
                                PLUGIN.getPluginConfig().getString("Messages.disabled")
                        ));

                        open(player, collector);
                    });
            gui.setItem(PLUGIN.getPluginConfig().getInt("Gui.items.status-enabled.slot"), disable);
        } else {
            GuiItem enable = ItemBuilder.from(
                            XMaterial.matchXMaterial(PLUGIN.getPluginConfig()
                                    .getString("Gui.items.status-disabled.material")).orElse(null).parseItem())
                    .name(ChatUtils.format(PLUGIN.getPluginConfig().getString("Gui.items.status-disabled.displayName")))
                    .lore(ChatUtils.format(PLUGIN.getPluginConfig().getStringList("Gui.items.status-disabled.lore")))
                    .asGuiItem((event) -> {
                        collector.setEnabled(true);
                        PLUGIN.getAdventure().player(player).sendMessage(ChatUtils.format(
                                PLUGIN.getPluginConfig().getString("Messages.enabled")
                        ));

                        open(player, collector);
                    });
            gui.setItem(PLUGIN.getPluginConfig().getInt("Gui.items.status-disabled.slot"), enable);
        }

        for (CollectedItem collectedItem : collector.getItemMap().values()) {
            int current = collectedItem.getAmount();
            if (current <= 0) continue;
            int maxStackSize = collectedItem.getItem().getMaterial().parseItem().getMaxStackSize();
            int times = current / maxStackSize + 1;

            if (times > 1) {
                for (int i = 0; i < times; i++) {
                    if (current <= 0) break;

                    addItem(player, collector, gui, collectedItem, current, maxStackSize);
                    current -= Math.min(current, maxStackSize);
                }
            } else {
                addItem(player, collector, gui, collectedItem, current, maxStackSize);
            }
        }

        gui.open(player);
    }

    // not a good solution but better than a yellow color everywhere, it can break the gui if configuration is wrong
    @SuppressWarnings("ConstantConditions")
    private static void addItem(Player player, Collector collector,
                                PaginatedGui gui, CollectedItem collectedItem, int current, int maxStackSize) {
        int amount = Math.min(current, maxStackSize);
        gui.addItem(ItemBuilder.from(collectedItem.getItem().getMaterial().parseItem())
                .lore(ChatUtils.format(PLUGIN.getPluginConfig().getStringList("Gui.items.drop.lore")))
                .amount(amount)
                .asGuiItem(event -> {
                    if (amount <= 0) {
                        open(player, collector);
                        return;
                    }

                    collectedItem.setAmount(collectedItem.getAmount() - amount);

                    if (event.isLeftClick()) {
                        if (player.getInventory().firstEmpty() == -1) {
                            PLUGIN.getAdventure().player(player).sendMessage(ChatUtils.format(
                                    PLUGIN.getPluginConfig().getString("Messages.inventory-full")
                            ));
                            return;
                        }

                        player.getInventory().addItem(ItemBuilder.from(collectedItem.getItem().getMaterial().parseItem())
                                .amount(amount).build());

                        String key = ItemUtils.getKey(collectedItem.getItem().getMaterial().parseMaterial());
                        PLUGIN.getAdventure().player(player).sendMessage(ChatUtils.format(
                                PLUGIN.getPluginConfig().getString("Messages.took"),
                                Placeholder.unparsed("amount", String.valueOf(amount)),
                                Placeholder.component("item", key != null ?
                                        Component.translatable(key) :
                                        Component.text(collectedItem.getItem().getMaterial().parseMaterial().name())
                                )
                        ));
                    } else if (event.isRightClick() && (!(PLUGIN.getEconomyManager() instanceof EmptyEconomyManager))) {
                        CollectorSellEvent sellEvent = new CollectorSellEvent(collector, collectedItem, amount, false);
                        Bukkit.getPluginManager().callEvent(sellEvent);
                        if (sellEvent.isCancelled()) return;

                        double total = collectedItem.getItem().getPrice() * amount;
                        total = total - (PLUGIN.getPluginConfig().getInt("Settings.tax") * total / 100);

                        PLUGIN.getEconomyManager().add(player, total);

                        String key = ItemUtils.getKey(collectedItem.getItem().getMaterial().parseMaterial());
                        PLUGIN.getAdventure().player(player).sendMessage(ChatUtils.format(
                                PLUGIN.getPluginConfig().getString("Messages.sold"),
                                Placeholder.unparsed("price", ChatUtils.FORMATTER.format(total)),
                                Placeholder.unparsed("amount", String.valueOf(amount)),
                                Placeholder.component("item", key != null ?
                                        Component.translatable(key) :
                                        Component.text(collectedItem.getItem().getMaterial().parseMaterial().name())
                                )
                        ));
                    }

                    open(player, collector);
                }));
    }

}
