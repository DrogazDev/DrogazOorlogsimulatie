package nl.drogaz.kingdomOSX.cosmetics;

import nl.drogaz.kingdomOSX.database.DatabaseManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CosmeticsManager {

    private final DatabaseManager database;
    private final Map<UUID, Map<String, String>> cache = new HashMap<>();

    public CosmeticsManager(DatabaseManager database) {
        this.database = database;
    }

    public void loadPlayer(UUID uuid) {
        cache.put(uuid, database.loadCosmetics(uuid));
    }

    public void unloadPlayer(UUID uuid) {
        cache.remove(uuid);
    }

    public String get(UUID uuid, String key, String defaultValue) {
        return cache.getOrDefault(uuid, Map.of()).getOrDefault(key, defaultValue);
    }

    public void save(UUID uuid, String key, String value) {
        cache.computeIfAbsent(uuid, k -> new HashMap<>()).put(key, value);
        database.saveCosmetic(uuid, key, value);
    }
}
