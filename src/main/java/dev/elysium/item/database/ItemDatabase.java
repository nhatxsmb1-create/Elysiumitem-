package dev.elysium.item.database;

import dev.elysium.item.ElysiumItem;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.*;

public class ItemDatabase {

    private final ElysiumItem plugin;
    private Connection connection;

    public ItemDatabase(ElysiumItem plugin) { this.plugin = plugin; }

    public void initialize() {
        try {
            File f  = new File(plugin.getDataFolder(), "item_data.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + f.getAbsolutePath());
            connection.setAutoCommit(true);
            createTables();
            plugin.getLogger().info("[ItemDB] SQLite connected.");
        } catch (SQLException e) {
            plugin.getLogger().severe("[ItemDB] Connect error: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        try (Statement s = connection.createStatement()) {
            s.execute("""
                CREATE TABLE IF NOT EXISTS item_mastery (
                    uuid     TEXT NOT NULL,
                    item_id  TEXT NOT NULL,
                    exp      BIGINT DEFAULT 0,
                    PRIMARY KEY (uuid, item_id)
                );
            """);
            s.execute("""
                CREATE TABLE IF NOT EXISTS accessory_slots (
                    uuid  TEXT PRIMARY KEY,
                    slots TEXT DEFAULT ''
                );
            """);
        }
    }

    // ── Accessory Slots ───────────────────────────────────────────────────────

    public String loadAccessorySlots(java.util.UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT slots FROM accessory_slots WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("slots");
        } catch (SQLException e) {
            plugin.getLogger().warning("[ItemDB] LoadSlots error: " + e.getMessage());
        }
        return null;
    }

    public void saveAccessorySlots(java.util.UUID uuid, String serialized) {
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = """
                INSERT INTO accessory_slots (uuid, slots) VALUES (?, ?)
                ON CONFLICT(uuid) DO UPDATE SET slots = excluded.slots
            """;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, serialized);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("[ItemDB] SaveSlots error: " + e.getMessage());
            }
        });
    }

    public Map<String, Long> load(UUID uuid) {
        Map<String, Long> result = new HashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT item_id, exp FROM item_mastery WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.put(rs.getString("item_id"), rs.getLong("exp"));
        } catch (SQLException e) {
            plugin.getLogger().warning("[ItemDB] Load error: " + e.getMessage());
        }
        return result;
    }

    public void saveAsync(UUID uuid, Map<String, Long> data) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveSync(uuid, data));
    }

    public void saveSync(UUID uuid, Map<String, Long> data) {
        if (data.isEmpty()) return;
        String sql = """
            INSERT INTO item_mastery (uuid, item_id, exp)
            VALUES (?, ?, ?)
            ON CONFLICT(uuid, item_id) DO UPDATE SET exp = excluded.exp
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Map.Entry<String, Long> entry : data.entrySet()) {
                ps.setString(1, uuid.toString());
                ps.setString(2, entry.getKey());
                ps.setLong(3, entry.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            plugin.getLogger().warning("[ItemDB] Save error: " + e.getMessage());
        }
    }

    public void close() {
        try { if (connection != null && !connection.isClosed()) connection.close(); }
        catch (SQLException ignored) {}
    }
}
