package dev.crius.dropcollector.listener;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.api.event.CollectorCollectEvent;
import dev.crius.dropcollector.api.event.CollectorSellEvent;
import dev.crius.dropcollector.collector.CollectedItem;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.entity.CEntity;
import dev.crius.dropcollector.util.ChatUtils;
import dev.crius.dropcollector.xseries.XMaterial;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class ItemListener implements Listener {

    private final DropCollectorPlugin plugin;

    // we are using LOW priority to not conflict with other plugins
    // using the lowest priority may cause conflicts
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onItemSpawn(ItemSpawnEvent event) {
        if (event.getEntity().getPickupDelay() == 40
                && !plugin.getPluginConfig().getBoolean("Settings.collect-player-drops")) return;

        if (!plugin.getPluginConfig().getStringList("Settings.enabled-worlds")
                .contains(event.getEntity().getWorld().getName())) return;

        Item itemEntity = event.getEntity();
        ItemStack item = itemEntity.getItemStack();
        XMaterial material = XMaterial.matchXMaterial(item);
        CEntity entity = plugin.getEntityManager().getEntity(material);
        if (entity == null) return;

        Collector collector = plugin.getCollectorManager().getCollector(event.getLocation(), entity);
        if (collector == null || !collector.isEnabled()) return;

        CollectedItem collectedItem = collector.getItemMap().get(material);
        if (collectedItem == null) return;

        int remainingSpace = collector.getRemainingSpace();
        if (remainingSpace < 1) {
            if (!plugin.getPluginConfig().getBoolean("Settings.auto-sell") || !collector.isAutoSellEnabled()) return;

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(collector.getOwner());

            double total = 0;
            for (CollectedItem collected : collector.getItemMap().values()) {
                CollectorSellEvent sellEvent = new CollectorSellEvent(collector, collected, collected.getAmount(), true);
                Bukkit.getPluginManager().callEvent(sellEvent);
                if (sellEvent.isCancelled()) continue;

                total += collected.getItem().getPrice() * collected.getAmount();
                collected.setAmount(0);
            }
            total = total - (plugin.getPluginConfig().getInt("Settings.tax") * total / 100);

            plugin.getEconomyManager().add(offlinePlayer, total);

            if (offlinePlayer.isOnline()) {
                Player player = offlinePlayer.getPlayer();
                assert player != null;
                plugin.getAdventure().player(player).sendMessage(ChatUtils.format(
                        plugin.getPluginConfig().getString("Messages.sold-all-auto"),
                        Placeholder.unparsed("price", ChatUtils.FORMATTER.format(total))
                ));
            }

            remainingSpace = collector.getRemainingSpace();
        }

        int realAmount = plugin.getStackerManager().getRealAmount(itemEntity);
        int amountToAdd = Math.min(realAmount, remainingSpace);

        CollectorCollectEvent collectEvent = new CollectorCollectEvent(collector, collectedItem, amountToAdd);
        Bukkit.getPluginManager().callEvent(collectEvent);
        if (collectEvent.isCancelled()) return;

        if (plugin.getStackerManager().getRealAmount(itemEntity) <= remainingSpace) {
            event.getEntity().remove();
            event.setCancelled(true);
            // we are cancelling the event only if we remove the item entity
            // since the item entity is removed, other plugins can skip this with ignoreCancelled and should.
        } else {
            plugin.getStackerManager().setAmount(itemEntity, realAmount - amountToAdd);
        }

        collector.add(material, amountToAdd);


    }

}
