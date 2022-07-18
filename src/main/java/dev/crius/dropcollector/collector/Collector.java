package dev.crius.dropcollector.collector;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.database.impl.mongo.model.CollectorModel;
import dev.crius.dropcollector.database.impl.yaml.YamlData;
import dev.crius.dropcollector.entity.CEntity;
import dev.crius.dropcollector.entity.item.CItem;
import dev.crius.dropcollector.upgrade.Upgrade;
import dev.crius.dropcollector.util.ChatUtils;
import dev.crius.dropcollector.util.LocationUtils;
import dev.crius.dropcollector.util.Placeholder;
import dev.crius.dropcollector.xseries.XMaterial;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Collector {

    private final UUID id;
    private final CEntity entity;
    private Location location;
    private UUID owner;
    private String hologramId;
    private Upgrade level;
    private boolean enabled = true;
    private boolean autoSellEnabled = true;
    private final List<String> holoLines;
    private final Map<XMaterial, CollectedItem> itemMap = new HashMap<>();

    public Collector(CEntity entity, UUID owner, Location location) {
        this.id = UUID.randomUUID();
        this.owner = owner;
        this.location = location;
        this.entity = entity;
        this.holoLines = ChatUtils.colorLegacy(
                DropCollectorPlugin.getInstance().getPluginConfig().getStringList("Messages.hologram-lines"),
                new Placeholder("<display-name>", entity.getDisplayName())
        );
        this.level = DropCollectorPlugin.getInstance().getUpgradeManager().getUpgrade(1);

        for (CItem item : this.entity.getMaterials()) {
            CollectedItem collectedItem = new CollectedItem(item);
            itemMap.put(item.getMaterial(), collectedItem);
        }
    }

    public Collector(YamlData data) {
        data.create();

        this.location = LocationUtils.getLocation(data.getString("location"));
        this.enabled = data.getBoolean("enabled");
        this.owner = UUID.fromString(data.getString("owner", ""));
        this.id = UUID.fromString(data.getFile().getName().split("\\.")[0]);
        this.entity = DropCollectorPlugin.getInstance().getEntityManager().getEntity(data.getString("entity"));
        this.level = DropCollectorPlugin.getInstance().getUpgradeManager().getUpgrade(data.getInt("level"));
        this.holoLines = ChatUtils.colorLegacy(
                DropCollectorPlugin.getInstance().getPluginConfig().getStringList("Messages.hologram-lines"),
                new Placeholder("<display-name>", this.entity.getDisplayName())
        );
        this.autoSellEnabled = data.getBoolean("auto-sell");

        for (CItem item : this.entity.getMaterials()) {
            CollectedItem collectedItem = new CollectedItem(item);
            collectedItem.setAmount(data.getInt("collected." + item.getMaterial().name()));
            itemMap.put(item.getMaterial(), collectedItem);
        }
    }

    public Collector(ResultSet resultSet) throws SQLException {
        this.location = LocationUtils.getLocation(resultSet.getString("location"));
        this.enabled = resultSet.getBoolean("enabled");
        this.owner = UUID.fromString(resultSet.getString("owner"));
        this.id = UUID.fromString(resultSet.getString("uuid"));
        this.entity = DropCollectorPlugin.getInstance().getEntityManager().getEntity(resultSet.getString("entity"));
        this.level = DropCollectorPlugin.getInstance().getUpgradeManager().getUpgrade(resultSet.getInt("level"));
        this.holoLines = ChatUtils.colorLegacy(
                DropCollectorPlugin.getInstance().getPluginConfig().getStringList("Messages.hologram-lines"),
                new Placeholder("<display-name>", this.entity.getDisplayName())
        );
        this.autoSellEnabled = resultSet.getBoolean("autoSell");

        String collected = resultSet.getString("collected");
        for (CItem item : this.entity.getMaterials()) {
            if (collected.length() <= 1) break;

            DropCollectorPlugin.getInstance().debug(collected.substring(0,
                    collected.indexOf(':', collected.indexOf(item.getMaterial().name() + ":")) - 1));
            int amount = Integer.parseInt(
                    collected.substring(0, collected.indexOf(':', collected.indexOf(item.getMaterial().name() + ":")) - 1));
            DropCollectorPlugin.getInstance().debug(amount + "");
            CollectedItem collectedItem = new CollectedItem(item);
            collectedItem.setAmount(amount);

            itemMap.put(item.getMaterial(), collectedItem);
        }
    }

    public Collector(CollectorModel model) {
        this.location = LocationUtils.getLocation(model.location);
        this.enabled = model.enabled;
        this.owner = model.owner;
        this.id = model.id;
        this.entity = DropCollectorPlugin.getInstance().getEntityManager().getEntity(model.entity);
        this.level = DropCollectorPlugin.getInstance().getUpgradeManager().getUpgrade(model.level);
        this.holoLines = ChatUtils.colorLegacy(
                DropCollectorPlugin.getInstance().getPluginConfig().getStringList("Messages.hologram-lines"),
                new Placeholder("<display-name>", this.entity.getDisplayName())
        );
        this.autoSellEnabled = model.autoSell;

        for (CItem item : this.entity.getMaterials()) {
            int amount = model.itemMap.getOrDefault(item.getMaterial().name(), 0);
            DropCollectorPlugin.getInstance().debug(amount + "");
            CollectedItem collectedItem = new CollectedItem(item);
            collectedItem.setAmount(amount);

            itemMap.put(item.getMaterial(), collectedItem);
        }
    }

    public Collector(File file) {
        this(new YamlData(file.getParent(), file.getName()));
    }

    public int getAmount(XMaterial material) {
        if (material == null) return 0;
        if (!itemMap.containsKey(material)) return 0;

        return itemMap.get(material).getAmount();
    }

    public void setAmount(XMaterial material, int amount) {
        if (material == null) return;
        if (!itemMap.containsKey(material)) return;

        itemMap.get(material).setAmount(amount);
    }

    public void add(XMaterial material, int amount) {
        setAmount(material, getAmount(material) + amount);
    }

    public int getTotal() {
        return itemMap.values().stream().map(CollectedItem::getAmount).mapToInt(Integer::intValue).sum();
    }

    public int getRemainingSpace() {
        return level.getMax() - getTotal();
    }

    public int getMax() {
        return level.getMax();
    }

    public CollectorModel toModel() {
        CollectorModel model = new CollectorModel();
        model.enabled = this.enabled;
        model.entity = this.entity.getName();
        model.id = this.id;
        model.owner = this.owner;
        model.level = this.level.getPlace();
        model.location = LocationUtils.getLocation(this.location);
        model.autoSell = this.autoSellEnabled;

        Map<String, Integer> map = new HashMap<>();
        for (CollectedItem item : this.itemMap.values()) {
            map.put(item.getItem().getMaterial().name(), item.getAmount());
        }
        model.itemMap = map;

        return model;
    }

}
