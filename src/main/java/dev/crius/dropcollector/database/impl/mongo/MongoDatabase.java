package dev.crius.dropcollector.database.impl.mongo;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.database.Database;
import dev.crius.dropcollector.database.impl.mongo.model.CollectorModel;
import lombok.RequiredArgsConstructor;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MongoDatabase implements Database {

    private final DropCollectorPlugin plugin;
    private final String dbName;
    private final String table;
    private final String connectionString;
    private MongoCollection<CollectorModel> collection;

    @Override
    public void onEnable() {

        ConnectionString connectionString = new ConnectionString(this.connectionString);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build())
                .codecRegistry(CodecRegistries.fromRegistries(
                        MongoClientSettings.getDefaultCodecRegistry(),
                        CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
                ))
                .uuidRepresentation(UuidRepresentation.JAVA_LEGACY)
                .build();

        MongoClient mongoClient = MongoClients.create(settings);
        com.mongodb.client.MongoDatabase database = mongoClient.getDatabase(dbName);

        if (!database.listCollectionNames().into(new ArrayList<>()).contains(table)) {
            database.createCollection(table);
        }

        collection = database.getCollection(table, CollectorModel.class);

        for (Collector collector : getCollectors()) {
            plugin.getCollectorManager().addCollector(collector, false);
        }

    }

    @Override
    public void saveAll() {
        for (CollectorModel model : plugin.getCollectorManager().getCollectors().stream()
                .map(Collector::toModel).collect(Collectors.toList())) {
            collection.replaceOne(Filters.eq("_id", model.id), model);
        }
    }

    @Override
    public void saveCollector(Collector collector) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> collection.insertOne(collector.toModel()));
    }

    @Override
    public Collection<Collector> getCollectors() {
        List<Collector> collectors = new ArrayList<>();

        for (CollectorModel model : collection.find(Filters.empty())) {
            Collector collector = new Collector(model);
            collectors.add(collector);
        }

        return collectors;
    }

    @Override
    public Collection<Collector> getCollectors(UUID uuid) {
        List<Collector> collectors = new ArrayList<>();

        for (CollectorModel model : collection.find(Filters.eq("_id", uuid))) {
            Collector collector = new Collector(model);
            collectors.add(collector);
        }

        return collectors;
    }

    @Override
    public void remove(Collector collector) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                collection.deleteOne(Filters.eq("_id", collector.getId())));
    }

    @Override
    public void removeAll() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> collection.deleteMany(Filters.empty()));
    }
}
