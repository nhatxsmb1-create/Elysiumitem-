package dev.elysium.item.accessory;

import dev.elysium.item.ElysiumItem;
import dev.elysium.item.item.ElysiumItemData;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AccessoryManager {
    private final ElysiumItem plugin;
    private final Map<UUID, AccessorySlotData> playerSlots = new HashMap<>();

    public AccessoryManager(ElysiumItem plugin) {
        this.plugin = plugin;
    }

    public AccessorySlotData getSlotData(UUID uuid) {
        return playerSlots.computeIfAbsent(uuid, k -> new AccessorySlotData());
    }

    public void removeSlots(UUID uuid) {
        playerSlots.remove(uuid);
    }
}