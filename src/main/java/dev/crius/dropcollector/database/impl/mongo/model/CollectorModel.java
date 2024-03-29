package dev.crius.dropcollector.database.impl.mongo.model;

import dev.crius.dropcollector.collector.log.ItemLog;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.Deque;
import java.util.Map;
import java.util.UUID;

public class CollectorModel {

    @BsonId
    public UUID id;

    public String entity;

    public String location;

    public UUID owner;

    public int level;

    public boolean enabled;

    public boolean autoSell;

    public Map<String, Integer> itemMap;

    public Deque<ItemLog> logs;

}
