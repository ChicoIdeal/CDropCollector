package dev.crius.dropcollector.database.impl.yaml;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class YamlData extends YamlConfiguration {

    private final File file;

    public YamlData(String path, String name) {
        file = new File(path, name.endsWith(".yml") ? name : name + ".yml");
    }

    @SuppressWarnings("all")
    public void create() {
        if (!file.exists()) {
            file.getParentFile().mkdirs();

            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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

    public void save() {
        try {
            this.save(this.file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getFile() {
        return file;
    }
}