package dev.crius.dropcollector.entity;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.entity.item.CItem;
import dev.crius.dropcollector.xseries.XMaterial;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class EntityManager {

    private final DropCollectorPlugin plugin;
    private final Map<String, CEntity> entityMap = new HashMap<>();

    public void init() {
        entityMap.clear();

        ConfigurationSection section = plugin.getPluginConfig().getConfigurationSection("Entities");
        if (section == null) throw new IllegalStateException("Entities did not setup correctly.");

        for (String entityName : section.getKeys(false)) {
            String displayName = section.getString(entityName + ".displayName");
            String texture = section.getString(entityName + ".head", "");
            ItemStack head = ItemBuilder.skull().texture(texture).build();
            Set<CItem> materials = new HashSet<>();
            for (String s : section.getConfigurationSection(entityName + ".materials").getKeys(false)) {
                XMaterial material = XMaterial.matchXMaterial(s).orElse(null);
                ItemStack item = material.parseItem();
                materials.add(new CItem(material, plugin.getShopManager()
                        .getPrice(item, section.getDouble(entityName + ".materials." + s))));
            }

            entityMap.put(entityName, new CEntity(entityName, displayName, materials, head, texture));
        }
    }

    /**
     *
     * @param name If target entity's type is item, material's name else entity name.
     * @return Entity
     */
    public CEntity getEntity(String name) {
        return entityMap.get(name);
    }

    public CEntity getEntity(XMaterial material) {
        return this.entityMap.values().stream()
                .filter(entity -> entity.getMaterials().stream().map(CItem::getMaterial)
                        .collect(Collectors.toList()).contains(material)).findFirst().orElse(null);
    }

    public Collection<CEntity> getEntities() {
        return Collections.unmodifiableCollection(entityMap.values());
    }

}
