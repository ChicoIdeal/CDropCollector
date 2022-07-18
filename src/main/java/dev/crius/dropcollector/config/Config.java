package dev.crius.dropcollector.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Config extends YamlConfiguration {

    private final File file;
    private final JavaPlugin plugin;

    public Config(JavaPlugin plugin, String name){
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), name);
    }

    public void create() {
        if (!file.exists()) {
            if (file.getParentFile().mkdirs())
                plugin.saveResource(this.file.getName(), false);
        }

        reload();
    }

    public void reload() {
        try {
            this.load(this.file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
