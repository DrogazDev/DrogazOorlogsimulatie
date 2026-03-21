package nl.drogaz.kingdomOSX.database;

import nl.drogaz.kingdomOSX.Main;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {

    private Connection connection;

    public void initialize(Main plugin) throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) dataFolder.mkdirs();

        connection = DriverManager.getConnection("jdbc:sqlite:" + new File(dataFolder, "cosmetics.db").getAbsolutePath());

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS player_cosmetics (
                    uuid         TEXT NOT NULL,
                    cosmetic_key TEXT NOT NULL,
                    value        TEXT NOT NULL,
                    PRIMARY KEY (uuid, cosmetic_key)
                )
            """);
        }
    }

    public void saveCosmetic(UUID uuid, String key, String value) {
        try (PreparedStatement ps = connection.prepareStatement("""
            INSERT INTO player_cosmetics (uuid, cosmetic_key, value)
            VALUES (?, ?, ?)
            ON CONFLICT(uuid, cosmetic_key) DO UPDATE SET value = excluded.value
        """)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, key);
            ps.setString(3, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> loadCosmetics(UUID uuid) {
        Map<String, String> result = new HashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT cosmetic_key, value FROM player_cosmetics WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("cosmetic_key"), rs.getString("value"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
