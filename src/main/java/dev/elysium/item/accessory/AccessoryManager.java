package dev.elysium.item.accessory;

import dev.elysium.item.ElysiumItem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AccessoryManager {
    private final ElysiumItem plugin;
    private final Map<UUID, AccessorySlotData> playerSlots = new HashMap<>();
    private final File dataFile;
    private FileConfiguration dataConfig;

    public AccessoryManager(ElysiumItem plugin) {
        this.plugin = plugin;
        File coreFolder = new File(plugin.getDataFolder().getParentFile(), "ElysiumCore/data");
        if (!coreFolder.exists()) {
            coreFolder.mkdirs();
        }
        this.dataFile = new File(coreFolder, "accessories_data.yml");
        loadData();
    }

    public AccessorySlotData getSlotData(UUID uuid) {
        return playerSlots.computeIfAbsent(uuid, k -> {
            AccessorySlotData data = new AccessorySlotData();
            if (dataConfig.contains(uuid.toString())) {
                data = AccessorySlotData.deserialize(dataConfig.getString(uuid.toString()));
            }
            return data;
        });
    }

    public void removeSlots(UUID uuid) {
        if (playerSlots.containsKey(uuid)) {
            dataConfig.set(uuid.toString(), playerSlots.get(uuid).serialize());
            saveData();
            playerSlots.remove(uuid);
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, AccessorySlotData> entry : playerSlots.entrySet()) {
            dataConfig.set(entry.getKey().toString(), entry.getValue().serialize());
        }
        saveData();
    }

    private void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}