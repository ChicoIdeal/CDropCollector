package dev.crius.dropcollector;

import dev.crius.dropcollector.collector.CollectorManager;
import dev.crius.dropcollector.command.BuyCommand;
import dev.crius.dropcollector.command.GiveCommand;
import dev.crius.dropcollector.command.MainCommand;
import dev.crius.dropcollector.command.ReloadCommand;
import dev.crius.dropcollector.config.Config;
import dev.crius.dropcollector.database.Database;
import dev.crius.dropcollector.database.DatabaseType;
import dev.crius.dropcollector.database.impl.mongo.MongoDatabase;
import dev.crius.dropcollector.database.impl.mysql.MySQLDatabase;
import dev.crius.dropcollector.database.impl.yaml.YamlDatabase;
import dev.crius.dropcollector.economy.EconomyManager;
import dev.crius.dropcollector.economy.impl.EmptyEconomyManager;
import dev.crius.dropcollector.economy.impl.PlayerPointsEconomyManager;
import dev.crius.dropcollector.economy.impl.VaultEconomyManager;
import dev.crius.dropcollector.entity.CEntity;
import dev.crius.dropcollector.entity.EntityManager;
import dev.crius.dropcollector.hologram.HologramManager;
import dev.crius.dropcollector.hologram.impl.DecentHologramsManager;
import dev.crius.dropcollector.hologram.impl.EmptyHologramManager;
import dev.crius.dropcollector.hologram.impl.HolographicDisplaysManager;
import dev.crius.dropcollector.listener.CropListener;
import dev.crius.dropcollector.listener.EntityListener;
import dev.crius.dropcollector.listener.InteractListener;
import dev.crius.dropcollector.listener.ItemListener;
import dev.crius.dropcollector.region.RegionManager;
import dev.crius.dropcollector.region.impl.*;
import dev.crius.dropcollector.shop.ShopManager;
import dev.crius.dropcollector.shop.impl.*;
import dev.crius.dropcollector.stacker.StackerManager;
import dev.crius.dropcollector.stacker.impl.*;
import dev.crius.dropcollector.task.AutoSaveTask;
import dev.crius.dropcollector.upgrade.UpgradeManager;
import dev.crius.dropcollector.util.ChatUtils;
import dev.crius.dropcollector.util.UpdateChecker;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.bukkit.message.BukkitMessageKey;
import dev.triumphteam.cmd.core.exceptions.CommandRegistrationException;
import dev.triumphteam.cmd.core.message.MessageKey;
import dev.triumphteam.gui.guis.BaseGui;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Getter
public final class DropCollectorPlugin extends JavaPlugin {

    // Instance:
    @Getter private static DropCollectorPlugin instance;

    // Other finals:
    private final boolean debugMode = true;
    private final Config pluginConfig = new Config(this, "config.yml");

    // Managers:
    private final CollectorManager collectorManager = new CollectorManager(this);
    private final EntityManager entityManager = new EntityManager(this);
    private HologramManager hologramManager;
    private RegionManager regionManager;
    private EconomyManager economyManager;
    private ShopManager shopManager;
    private UpgradeManager upgradeManager;
    private StackerManager stackerManager;
    private BukkitCommandManager<CommandSender> commandManager;

    // Others:
    private Database pluginDatabase;
    private BukkitAudiences adventure;
    private UpdateChecker updateChecker;

    @Override
    public void onEnable() {

        instance = this;
        adventure = BukkitAudiences.create(this);

        setupConfig();

        setupEconomyManager();
        setupShopManager();
        entityManager.init();

        setupHologramManager();
        setupRegionManager();
        setupUpgradeManager();
        setupStackerManager();
        setupDatabase();
        setupCommands();
        setupListeners();

        setupMetrics();

        setupTasks();

        setupUpdateChecker();

    }

    @Override
    public void onDisable() {
        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }

        if (this.pluginDatabase != null) {
            log("Saving collectors...");
            this.pluginDatabase.saveAll();
            log("Collectors saved.");
        }

        this.hologramManager.removeAll();

        Bukkit.getOnlinePlayers().stream().filter(player ->
                player.getOpenInventory().getTopInventory().getHolder() instanceof BaseGui).forEach(HumanEntity::closeInventory);

        // remove the commands from commandlist
        getBukkitCommands(getCommandMap()).remove("dropcollector");
        getBukkitCommands(getCommandMap()).remove("cdropcollector");
    }

    // copied from triumph-cmd, credit goes to triumph-team
    @NotNull
    private CommandMap getCommandMap() {
        try {
            final Server server = Bukkit.getServer();
            final Method getCommandMap = server.getClass().getDeclaredMethod("getCommandMap");
            getCommandMap.setAccessible(true);

            return (CommandMap) getCommandMap.invoke(server);
        } catch (final Exception ignored) {
            throw new CommandRegistrationException("Unable get Command Map. Commands will not be registered!");
        }
    }

    // copied from triumph-cmd, credit goes to triumph-team
    @NotNull
    private Map<String, Command> getBukkitCommands(@NotNull final CommandMap commandMap) {
        try {
            final Field bukkitCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
            bukkitCommands.setAccessible(true);
            //noinspection unchecked
            return (Map<String, org.bukkit.command.Command>) bukkitCommands.get(commandMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new CommandRegistrationException("Unable get Bukkit commands. Commands might not be registered correctly!");
        }
    }

    public void debug(String message) {
        if (debugMode) log(message);
    }

    public void log(String message) {
        log(message, Level.INFO);
    }

    public void log(String message, Level level) {
        this.getLogger().log(level, message);
    }

    public void log(String message, Exception exception) {
        log(Level.SEVERE, message, exception);
    }

    public void log(Level level, String message, Exception exception) {
        this.getLogger().log(level, message, exception);
    }

    public void setupUpdateChecker() {
        (updateChecker = new UpdateChecker(this)).checkUpdates();

        this.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                Player player = event.getPlayer();

                if (!player.hasPermission("dropcollector.notify")) return;

                if (!updateChecker.isUpToDate()) {
                    adventure.player(player).sendMessage(ChatUtils.format(
                            "<gold>[CDropCollector] <yellow>An update was found!"
                    ));
                    adventure.player(player).sendMessage(ChatUtils.format(
                            "<gold>[CDropCollector] <yellow>Update message:"
                    ));
                    adventure.player(player).sendMessage(ChatUtils.format(
                            "<gold>[CDropCollector] <yellow><message>",
                            Placeholder.parsed("message", updateChecker.getUpdateMessage())
                    ));
                }
            }
        }, this);
    }

    public void setupMetrics() {
        Metrics metrics = new Metrics(this, 15820);
        metrics.addCustomChart(new SingleLineChart("collectors", () -> collectorManager.getCollectors().size()));
    }

    public void setupTasks() {
        // save all collectors every 15 minute so even if the server crash we won't lose all the data
        new AutoSaveTask(this).runTaskTimerAsynchronously(this, 18_000, 18_000);
    }

    public void setupConfig() {
        pluginConfig.create();
    }

    public void setupCommands() {
        this.commandManager = BukkitCommandManager.create(this);

        commandManager.registerArgument(CEntity.class, (sender, entity) -> this.entityManager.getEntity(entity));

        commandManager.registerSuggestion(CEntity.class, (sender, context) -> {
            Collection<CEntity> entities = this.entityManager.getEntities();
            return entities.stream().map(CEntity::getName)
                    .filter(e -> e.toLowerCase().startsWith(context.getArgs().get(0).toLowerCase()))
                    .collect(Collectors.toList());
        });

        commandManager.registerCommand(
                new MainCommand(this),
                new GiveCommand(this),
                new ReloadCommand(this),
                new BuyCommand(this)
        );

        commandManager.registerMessage(MessageKey.INVALID_ARGUMENT, (sender, context) -> adventure.sender(sender)
                .sendMessage(ChatUtils.format(pluginConfig.getString("Messages.invalid-argument"))));

        commandManager.registerMessage(MessageKey.UNKNOWN_COMMAND, (sender, context) -> adventure.sender(sender)
                .sendMessage(ChatUtils.format(pluginConfig.getString("Messages.unknown-command"))));

        commandManager.registerMessage(MessageKey.NOT_ENOUGH_ARGUMENTS, (sender, context) -> adventure.sender(sender)
                .sendMessage(ChatUtils.format(pluginConfig.getString("Messages.not-enough-arguments"))));

        commandManager.registerMessage(MessageKey.TOO_MANY_ARGUMENTS, (sender, context) -> adventure.sender(sender)
                .sendMessage(ChatUtils.format(pluginConfig.getString("Messages.too-many-arguments"))));

        commandManager.registerMessage(BukkitMessageKey.NO_PERMISSION, (sender, context) -> adventure.sender(sender)
                .sendMessage(ChatUtils.format(pluginConfig.getString("Messages.no-permission"))));

    }

    public void setupListeners() {
        PluginManager pluginManager = this.getServer().getPluginManager();

        pluginManager.registerEvents(new InteractListener(this), this);
        pluginManager.registerEvents(new ItemListener(this), this);
        pluginManager.registerEvents(new CropListener(this), this);
        pluginManager.registerEvents(new EntityListener(this), this);
    }

    public void setupUpgradeManager() {
        upgradeManager = new UpgradeManager(this);
        upgradeManager.init();
    }

    public void setupHologramManager() {
        if (!pluginConfig.getBoolean("Hooks.preferredHologramPlugin.enabled")) {
            hologramManager = new EmptyHologramManager(this);
            return;
        }

        PluginManager pluginManager = this.getServer().getPluginManager();

        if (pluginManager.getPlugin("HolographicDisplays") != null)
            hologramManager = new HolographicDisplaysManager(this);
        else if (pluginManager.getPlugin("DecentHolograms") != null)
            hologramManager = new DecentHologramsManager(this);
        else
            hologramManager = new EmptyHologramManager(this);

        String preferred = pluginConfig.getString("Hooks.preferredHologramPlugin.name", "");
        if (!preferred.equals(hologramManager.getName()) && pluginManager.getPlugin(preferred) != null)
            switch (preferred) {
                case "HolographicDisplays":
                    hologramManager = new HolographicDisplaysManager(this);
                    break;
                case "DecentHolograms":
                    hologramManager = new DecentHologramsManager(this);
                    break;
            }

        log("Using " + hologramManager.getName() + " as hologram manager.");
    }

    public void setupEconomyManager() {
        if (!pluginConfig.getBoolean("Hooks.preferredEconomyPlugin.enabled")) {
            economyManager = new EmptyEconomyManager();
            return;
        }

        PluginManager pluginManager = this.getServer().getPluginManager();

        if (pluginManager.getPlugin("Vault") != null)
            economyManager = new VaultEconomyManager();
        else if (pluginManager.getPlugin("PlayerPoints") != null)
            economyManager = new PlayerPointsEconomyManager();
        else
            economyManager = new EmptyEconomyManager();

        String preferred = pluginConfig.getString("Hooks.preferredEconomyPlugin.name", "");
        if (!preferred.equals(economyManager.getName()) && pluginManager.getPlugin(preferred) != null)
            switch (preferred) {
                case "Vault":
                    economyManager = new VaultEconomyManager();
                    break;
                case "PlayerPoints":
                    economyManager = new PlayerPointsEconomyManager();
                    break;
            }

        log("Using " + economyManager.getName() + " as economy manager.");
    }

    public void setupShopManager() {
        if (!pluginConfig.getBoolean("Hooks.preferredShopPlugin.enabled")) {
            shopManager = new EmptyShopManager();
            return;
        }

        PluginManager pluginManager = this.getServer().getPluginManager();

        if (pluginManager.getPlugin("EconomyShopGUI") != null
                || pluginManager.getPlugin("EconomyShopGUI-Premium") != null)
            shopManager = new EconomyShopGuiShopManager();
        else if (pluginManager.getPlugin("ShopGUIPlus") != null)
            shopManager = new ShopGuiPlusShopManager();
        else if (pluginManager.getPlugin("OneStopShop") != null)
            shopManager = new OneStopShopShopManager();
        else if (pluginManager.getPlugin("GUIShop") != null)
            shopManager = new GUIShopShopManager();
        else
            shopManager = new EmptyShopManager();

        String preferred = pluginConfig.getString("Hooks.preferredShopPlugin.name", "");
        if (!preferred.equals(shopManager.getName()) && pluginManager.getPlugin(preferred) != null)
            switch (preferred) {
                case "EconomyShopGUI-Premium":
                case "EconomyShopGUI":
                    shopManager = new EconomyShopGuiShopManager();
                    break;
                case "ShopGUIPlus":
                    shopManager = new ShopGuiPlusShopManager();
                    break;
                case "OneStopShop":
                    shopManager = new OneStopShopShopManager();
                    break;
                case "GUIShop":
                    shopManager = new GUIShopShopManager();
                    break;
            }

        log("Using " + shopManager.getName() + " as shop manager.");
    }

    public void setupRegionManager() {
        if (!pluginConfig.getBoolean("Hooks.preferredRegionPlugin.enabled")) {
            regionManager = new ChunkBasedRegionManager(this);
            return;
        }

        PluginManager pluginManager = this.getServer().getPluginManager();

        if (pluginManager.getPlugin("ASkyBlock") != null)
            regionManager = new ASkyBlockRegionManager(this);
        else if (pluginManager.getPlugin("BentoBox") != null)
            regionManager = new BentoBoxRegionManager(this);
        else if (pluginManager.getPlugin("FabledSkyBlock") != null)
            regionManager = new FabledSkyBlockRegionManager(this);
        else if (pluginManager.getPlugin("GriefDefender") != null)
            regionManager = new GriefDefenderRegionManager(this);
        else if (pluginManager.getPlugin("GriefPrevention") != null)
            regionManager = new GriefPreventionRegionManager(this);
        else if (pluginManager.getPlugin("SuperiorSkyblock2") != null)
            regionManager = new SuperiorSkyBlockRegionManager(this);
        else if (pluginManager.getPlugin("UltimateClaims") != null)
            regionManager = new UltimateClaimsRegionManager(this);
        else if (pluginManager.getPlugin("PlotSquared") != null)
            regionManager = getPlotSquaredRegionManager();
        else
            regionManager = new ChunkBasedRegionManager(this);

        String preferred = pluginConfig.getString("Hooks.preferredRegionPlugin.name", "");
        if (!preferred.equals(hologramManager.getName()) && pluginManager.getPlugin(preferred) != null)
            switch (preferred) {
                case "ASkyBlock":
                    regionManager = new ASkyBlockRegionManager(this);
                    break;
                case "BentoBox":
                    regionManager = new BentoBoxRegionManager(this);
                    break;
                case "FabledSkyBlock":
                    regionManager = new FabledSkyBlockRegionManager(this);
                    break;
                case "GriefDefender":
                    regionManager = new GriefDefenderRegionManager(this);
                    break;
                case "GriefPrevention":
                    regionManager = new GriefPreventionRegionManager(this);
                    break;
                case "SuperiorSkyblock2":
                    regionManager = new SuperiorSkyBlockRegionManager(this);
                    break;
                case "UltimateClaims":
                    regionManager = new UltimateClaimsRegionManager(this);
                    break;
                case "PlotSquared":
                    regionManager = getPlotSquaredRegionManager();
                    break;
            }

        regionManager.init();

        log("Using " + regionManager.getName() + " as region manager.");
    }

    private RegionManager getPlotSquaredRegionManager() {
        RegionManager manager = null;

        if (checkPSClass("com.intellectualcrafters.plot.api.PlotAPI"))
            manager = new PlotSquaredV3RegionManager(this);
        else if (checkPSClass("com.github.intellectualsites.plotsquared.api.PlotAPI"))
            manager = new PlotSquaredV4RegionManager(this);
        else if (checkPSClass("com.plotsquared.core.PlotAPI")) {
            try {
                manager = (RegionManager) Class.forName("dev.crius.dropcollector.region.impl.PlotSquaredV6RegionManager")
                        .getConstructor(DropCollectorPlugin.class).newInstance(this);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException | ClassNotFoundException e) {
                throw new RuntimeException("Could not create an instance for PlotSquared v6 Region Manager!", e);
            }
        }

        return manager;
    }

    private boolean checkPSClass(String clazz) {
        try {
            Class.forName(clazz);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public void setupStackerManager() {
        if (!pluginConfig.getBoolean("Hooks.preferredStackerPlugin.enabled")) {
            stackerManager = new EmptyStackerManager();
            return;
        }

        PluginManager pluginManager = this.getServer().getPluginManager();

        if (pluginManager.getPlugin("UltimateStacker") != null)
            stackerManager = new UltimateStackerManager();
        else if (pluginManager.getPlugin("RoseStacker") != null)
            stackerManager = new RoseStackerManager();
        else if (pluginManager.getPlugin("WildStacker") != null)
            stackerManager = new WildStackerManager();
        else if (pluginManager.getPlugin("zItemStacker") != null)
            stackerManager = new ZItemStackerManager();
        else
            stackerManager = new EmptyStackerManager();

        String preferred = pluginConfig.getString("Hooks.preferredStackerPlugin.name", "");
        if (!preferred.equals(hologramManager.getName()) && pluginManager.getPlugin(preferred) != null)
            switch (preferred) {
                case "UltimateStacker":
                    stackerManager = new UltimateStackerManager();
                    break;
                case "RoseStacker":
                    stackerManager = new RoseStackerManager();
                    break;
                case "WildStacker":
                    stackerManager = new WildStackerManager();
                    break;
                case "zItemStacker":
                    stackerManager = new ZItemStackerManager();
                    break;
            }

        log("Using " + stackerManager.getName() + " as stacker manager.");
    }

    public void setupDatabase() {
        DatabaseType type = DatabaseType.match(pluginConfig.getString("Settings.database.type"));
        switch (type) {
            case MONGO:
                pluginDatabase = new MongoDatabase(this,
                        pluginConfig.getString("Settings.database.database"),
                        pluginConfig.getString("Settings.database.table"),
                        pluginConfig.getString("Settings.database.connection-string")
                );
                break;
            case MYSQL:
                pluginDatabase = new MySQLDatabase(this,
                        pluginConfig.getString("Settings.database.host"),
                        pluginConfig.getString("Settings.database.database"),
                        pluginConfig.getString("Settings.database.username"),
                        pluginConfig.getString("Settings.database.password"),
                        pluginConfig.getString("Settings.database.table"),
                        pluginConfig.getInt("Settings.database.port"),
                        pluginConfig.getBoolean("Settings.database.useSSL")
                );
                break;
            default:
                pluginDatabase = new YamlDatabase(this);
                break;
        }

        Bukkit.getScheduler().runTask(this, () -> {
            log("Loading collectors...");
            pluginDatabase.onEnable();
            log("Collectors loaded.");
        });
    }

}
