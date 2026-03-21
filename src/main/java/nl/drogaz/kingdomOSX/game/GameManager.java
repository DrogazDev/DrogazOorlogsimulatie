package nl.drogaz.kingdomOSX.game;

import nl.drogaz.kingdomOSX.Main;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class GameManager {

    private final Main plugin;
    private final Map<String, Arena> arenas = new LinkedHashMap<>();       // key → Arena
    private final Map<UUID, Arena> wizardSessions = new HashMap<>();       // playerUuid → arena in bewerking

    private final File configFile;
    private YamlConfiguration config;

    public GameManager(Main plugin) {
        this.plugin = plugin;
        configFile = new File(plugin.getDataFolder(), "arenas.yml");
        loadArenas();
    }

    // ── Laden & opslaan ───────────────────────────────────────────────────────────

    public void loadArenas() {
        if (!configFile.exists()) {
            try { configFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        ConfigurationSection root = config.getConfigurationSection("arenas");
        if (root == null) return;

        for (String key : root.getKeys(false)) {
            ConfigurationSection a = root.getConfigurationSection(key);
            if (a == null) continue;

            Arena arena = new Arena(key);
            arena.setDisplayName(a.getString("display-name", key));

            Material icon = Material.matchMaterial(a.getString("icon", "GRASS_BLOCK"));
            arena.setIcon(icon != null ? icon : Material.GRASS_BLOCK);
            arena.setEnabled(a.getBoolean("enabled", false));
            arena.setSpawn1(readLocation(a.getConfigurationSection("spawn1")));
            arena.setSpawn2(readLocation(a.getConfigurationSection("spawn2")));
            arena.setPos1(readLocation(a.getConfigurationSection("pos1")));
            arena.setPos2(readLocation(a.getConfigurationSection("pos2")));

            arenas.put(key, arena);
        }
        plugin.getLogger().info("Geladen: " + arenas.size() + " arena(s).");
    }

    public void saveArena(Arena arena) {
        arenas.put(arena.getKey(), arena);
        writeArena(arena);
        try { config.save(configFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public void deleteArena(String key) {
        arenas.remove(key);
        config.set("arenas." + key, null);
        try { config.save(configFile); } catch (IOException e) { e.printStackTrace(); }
    }

    // ── YAML helpers ──────────────────────────────────────────────────────────────

    private void writeArena(Arena arena) {
        String p = "arenas." + arena.getKey();
        config.set(p + ".display-name", arena.getDisplayName());
        config.set(p + ".icon", arena.getIcon().name());
        config.set(p + ".enabled", arena.isEnabled());
        writeLocation(p + ".spawn1", arena.getSpawn1());
        writeLocation(p + ".spawn2", arena.getSpawn2());
        writeLocation(p + ".pos1",   arena.getPos1());
        writeLocation(p + ".pos2",   arena.getPos2());
    }

    private void writeLocation(String path, LocationData loc) {
        if (loc == null) return;
        config.set(path + ".world", loc.world());
        config.set(path + ".x",     loc.x());
        config.set(path + ".y",     loc.y());
        config.set(path + ".z",     loc.z());
        config.set(path + ".yaw",   loc.yaw());
        config.set(path + ".pitch", loc.pitch());
    }

    private LocationData readLocation(ConfigurationSection s) {
        if (s == null) return null;
        return new LocationData(
                s.getString("world", "world"),
                s.getDouble("x"),
                s.getDouble("y"),
                s.getDouble("z"),
                (float) s.getDouble("yaw",   0),
                (float) s.getDouble("pitch", 0)
        );
    }

    // ── Wizard sessies ────────────────────────────────────────────────────────────

    /**
     * Start een wizard voor een nieuwe of bestaande arena.
     * Bij een bestaande key wordt de bestaande Arena hergebruikt (live-edit).
     */
    public Arena startWizard(UUID playerUuid, String key) {
        Arena arena = arenas.containsKey(key) ? arenas.get(key) : new Arena(key);
        wizardSessions.put(playerUuid, arena);
        return arena;
    }

    public Arena getWizard(UUID playerUuid) {
        return wizardSessions.get(playerUuid);
    }

    public boolean hasWizard(UUID playerUuid) {
        return wizardSessions.containsKey(playerUuid);
    }

    public void cancelWizard(UUID playerUuid) {
        wizardSessions.remove(playerUuid);
    }

    // ── Queries ───────────────────────────────────────────────────────────────────

    public Arena getArena(String key) { return arenas.get(key); }

    public boolean arenaExists(String key) { return arenas.containsKey(key); }

    public Collection<Arena> getAllArenas() { return Collections.unmodifiableCollection(arenas.values()); }

    public Map<String, Arena> getArenas() { return Collections.unmodifiableMap(arenas); }
}
