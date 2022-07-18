package dev.crius.dropcollector.database;

import org.jetbrains.annotations.NotNull;

public enum DatabaseType {

    YAML, MYSQL, MONGO, UNKNOWN;

    @NotNull
    public static DatabaseType match(String str) {
        if (str == null || str.isEmpty()) return UNKNOWN;

        for (DatabaseType value : values()) {
            if (value.name().equals(str)) return value;
        }

        return UNKNOWN;
    }

}
