package dev.crius.dropcollector.database.impl.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.collector.CollectedItem;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.database.Database;
import dev.crius.dropcollector.util.LocationUtils;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class MySQLDatabase implements Database {

    private final DropCollectorPlugin plugin;
    private final String host;
    private final String database;
    private final String username;
    private final String password;
    private final String table;
    private final int port;
    private final boolean useSSL;
    private DataSource dataSource;

    @Override
    public void onEnable() {

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL);
        hikariConfig.setPassword(password);
        hikariConfig.setUsername(username);
        hikariConfig.setMaxLifetime(30000);
        hikariConfig.setIdleTimeout(10000);
        hikariConfig.setMaximumPoolSize(20);
        hikariConfig.setMinimumIdle(3);
        hikariConfig.setPoolName("DropCollector ConnectionPool");

        hikariConfig.addDataSourceProperty("cachePrepStmts", true);
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", 250);
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        hikariConfig.addDataSourceProperty("useServerPrepStmts", true);
        hikariConfig.addDataSourceProperty("cacheCallableStmts", true);
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", true);
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", true);
        hikariConfig.addDataSourceProperty("useLocalSessionState", true);
        hikariConfig.addDataSourceProperty("elideSetAutoCommits", true);
        hikariConfig.addDataSourceProperty("alwaysSendSetIsolation", false);

        hikariConfig.setConnectionTestQuery("SELECT 1");

        this.dataSource = new HikariDataSource(hikariConfig);

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS `" + table + "` (uuid varchar(255) NOT NULL UNIQUE," +
                    " owner varchar(255) NOT NULL, location varchar(100) NOT NULL, " +
                    "`level` integer, enabled BOOLEAN, entity varchar(50), autoSell BOOLEAN, collected TEXT);");
        } catch (SQLException exception) {
            plugin.log("An exception was found in database!", exception);
        }

        for (Collector collector : getCollectors()) {
            plugin.getCollectorManager().addCollector(collector, false);
        }
    }

    @Override
    public void saveAll() {
        plugin.debug("Saving collectors...");
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("REPLACE INTO `" + table
                     + "` (uuid, owner, location, `level`, enabled, entity, autoSell, collected)" +
                     " VALUES (?, ?, ?, ?, ?, ?, ?, ?);")) {

            for (Collector collector : plugin.getCollectorManager().getCollectors()) {
                statement.setString(1, collector.getId().toString());
                statement.setString(2, collector.getOwner().toString());
                statement.setString(3, LocationUtils.getLocation(collector.getLocation()));
                statement.setInt(4, collector.getLevel().getPlace());
                statement.setBoolean(5, collector.isEnabled());
                statement.setString(6, collector.getEntity().getName());
                statement.setBoolean(7, collector.isAutoSellEnabled());

                StringBuilder builder = new StringBuilder();
                for (CollectedItem item : collector.getItemMap().values()) {
                    builder.append(item.getItem().getMaterial().name())
                            .append(':')
                            .append(item.getAmount()).append(',');
                }

                String collected = builder.length() > 1 ?
                        builder.deleteCharAt(builder.lastIndexOf(",")).toString() : builder.toString();

                statement.setString(7, collected);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            plugin.log("An exception was found in database!", exception);
        }

        plugin.debug("Saved collectors.");
    }

    @Override
    public void saveCollector(Collector collector) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("REPLACE INTO `" + table
                         + "` (uuid, owner, location, `level`, enabled, entity, collected) VALUES (?, ?, ?, ?, ?, ?, ?);")) {

                statement.setString(1, collector.getId().toString());
                statement.setString(2, collector.getOwner().toString());
                statement.setString(3, LocationUtils.getLocation(collector.getLocation()));
                statement.setInt(4, collector.getLevel().getPlace());
                statement.setBoolean(5, collector.isEnabled());
                statement.setString(6, collector.getEntity().getName());
                statement.setBoolean(7, collector.isAutoSellEnabled());

                StringBuilder builder = new StringBuilder();
                for (CollectedItem item : collector.getItemMap().values()) {
                    builder.append(item.getItem().getMaterial().name())
                            .append(':')
                            .append(item.getAmount()).append(',');
                }

                String collected = builder.length() > 1 ?
                        builder.deleteCharAt(builder.lastIndexOf(",")).toString() : builder.toString();

                statement.setString(7, collected);
                statement.executeUpdate();
            } catch (SQLException exception) {
                plugin.log("An exception was found in database!", exception);
            }
        });
    }

    @Override
    public Collection<Collector> getCollectors() {
        List<Collector> collectors = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery("SELECT * FROM `" + table + "`");

            while (resultSet.next()) {
                Collector collector = new Collector(resultSet);
                collectors.add(collector);
            }
        } catch (SQLException e) {
            plugin.log("An exception was found in database!", e);
        }

        return collectors;
    }

    @Override
    public Collection<Collector> getCollectors(UUID uuid) {
        List<Collector> collectors = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM `" + table + "` WHERE owner = ?")) {

            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Collector collector = new Collector(resultSet);
                collectors.add(collector);
            }
        } catch (SQLException e) {
            plugin.log("An exception was found in database!", e);
        }

        return collectors;
    }

    @Override
    public void remove(Collector collector) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM `" + table + "` WHERE uuid = ?")) {

                statement.setString(1, collector.getId().toString());

                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.log("An exception was found in database!", e);
            }
        });
    }

    @Override
    public void removeAll() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("DELETE FROM `" + table + "`");
        } catch (SQLException e) {
            plugin.log("An exception was found in database!", e);
        }
    }
}
