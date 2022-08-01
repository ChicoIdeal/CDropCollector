package dev.crius.dropcollector.listener;

import de.tr7zw.changeme.nbtapi.NBTItem;
import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.api.event.CreatorPlaceEvent;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.entity.CEntity;
import dev.crius.dropcollector.hologram.impl.EmptyHologramManager;
import dev.crius.dropcollector.region.RegionManager;
import dev.crius.dropcollector.util.ChatUtils;
import dev.crius.dropcollector.xseries.XMaterial;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@RequiredArgsConstructor
public class InteractListener implements Listener {

    private final DropCollectorPlugin plugin;

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (XMaterial.supports(9) && event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getItem() == null || event.getClickedBlock() == null) return;
        if (event.getItem().getType() != XMaterial.PLAYER_HEAD.parseMaterial()) return;

        Player player = event.getPlayer();

        ItemStack item = event.getItem();
        NBTItem nbtItem = new NBTItem(item);

        if (!nbtItem.hasKey("dropcollector:creator")) return;

        if (!plugin.getPluginConfig().getStringList("Settings.enabled-worlds").contains(player.getWorld().getName())) {
            plugin.getAdventure().player(player).sendMessage(ChatUtils.format(
                    plugin.getPluginConfig().getString("Messages.cannot-create-world")
            ));
            event.setCancelled(true);
            return;
        }

        CEntity entity = plugin.getEntityManager().getEntity(nbtItem.getString("dropcollector:type"));
        if (entity == null) return; // this creator is broken, skip

        UUID owner = UUID.fromString(nbtItem.getString("dropcollector:owner"));
        String id = nbtItem.getString("dropcollector:id");

        if (plugin.getPluginConfig().getBoolean("only-the-owner-can-place") && !owner.equals(player.getUniqueId())) {
            plugin.getAdventure().player(player).sendMessage(ChatUtils.format(
                    plugin.getPluginConfig().getString("Messages.not-the-owner-of-creator")
            ));
            event.setCancelled(true);
            return;
        }

        double required = plugin.getPluginConfig().getDouble("Settings.required-balance-to-place");
        if (!plugin.getEconomyManager().has(player, required)) {
            plugin.getAdventure().player(player).sendMessage(ChatUtils.format(
                    plugin.getPluginConfig().getString("Messages.cannot-create-not-enough-balance"),
                    Placeholder.unparsed("required", ChatUtils.FORMATTER.format(required))
            ));
            event.setCancelled(true);
            return;
        }

        Collector collector = new Collector(entity, player.getUniqueId(), event.getClickedBlock().getLocation().add(0,1,0));
        RegionManager.Response response = plugin.getRegionManager().canCreate(player, collector);
        if (response == RegionManager.Response.NO_PERMISSION && !player.hasPermission(RegionManager.BYPASS_PERMISSION)) {
            plugin.getAdventure().player(player).sendMessage(
                    ChatUtils.format(plugin.getPluginConfig().getString("Messages.cannot-create-in-this-region"))
            );
            event.setCancelled(true);
            return;
        }

        if (response == RegionManager.Response.ALREADY_EXISTS) {
            plugin.getAdventure().player(player).sendMessage(
                    ChatUtils.format(plugin.getPluginConfig().getString("Messages.cannot-create-already-exists"))
            );
            event.setCancelled(true);
            return;
        }

        if (!(plugin.getHologramManager() instanceof EmptyHologramManager))
            event.setCancelled(true);

        CreatorPlaceEvent placeEvent = new CreatorPlaceEvent(player, collector);
        Bukkit.getPluginManager().callEvent(placeEvent);
        if (placeEvent.isCancelled()) return;

        plugin.getEconomyManager().remove(player, required);
        removeCreator(player, id);
        plugin.getCollectorManager().addCollector(collector, true);

        plugin.getAdventure().player(player).sendMessage(
                ChatUtils.format(plugin.getPluginConfig().getString("Messages.placed-a-collector"))
        );

    }

    private void removeCreator(Player target, String id) {
        int slot = -1;
        for (ItemStack item : target.getInventory().getContents()) {
            slot++;
            if (item == null || item.getType() != XMaterial.PLAYER_HEAD.parseMaterial()) continue;

            NBTItem nbtItem = new NBTItem(item);
            if (nbtItem.hasKey("dropcollector:creator") && nbtItem.getString("dropcollector:id").equals(id)) {
                // It is actually impossible to stack creators but just in case
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                    break;
                } else if (item.getAmount() == 1) {
                    target.getInventory().setItem(slot, null);
                    break;
                }
            }
        }
    }


}
