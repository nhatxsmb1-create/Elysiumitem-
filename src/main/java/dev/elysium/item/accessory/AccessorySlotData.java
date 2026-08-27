package dev.elysium.item.accessory;

import dev.elysium.item.util.ItemSerializer;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class AccessorySlotData {

    public enum SlotType { RING_1, RING_2, NECKLACE }

    private final Map<SlotType, ItemStack> equipped = new HashMap<>();

    public AccessorySlotData() {
        equipped.put(SlotType.RING_1,   null);
        equipped.put(SlotType.RING_2,   null);
        equipped.put(SlotType.NECKLACE, null);
    }

    public ItemStack getEquippedItem(SlotType slot)         { return equipped.get(slot); }
    public boolean   isEquipped(SlotType slot)              { return equipped.get(slot) != null; }
    public void      equip(SlotType slot, ItemStack item)   { equipped.put(slot, item); }
    public void      unequip(SlotType slot)                 { equipped.put(slot, null); }

    public ItemStack getNecklace() { return getEquippedItem(SlotType.NECKLACE); }
    public ItemStack getRing1()    { return getEquippedItem(SlotType.RING_1); }
    public ItemStack getRing2()    { return getEquippedItem(SlotType.RING_2); }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        for (SlotType slot : SlotType.values()) {
            if (sb.length() > 0) sb.append("|||");
            sb.append(slot.name()).append(":::").append(ItemSerializer.serialize(equipped.get(slot)));
        }
        return sb.toString();
    }

    public static AccessorySlotData deserialize(String data) {
        AccessorySlotData result = new AccessorySlotData();
        if (data == null || data.isBlank()) return result;
        for (String part : data.split("\\|\\|\\|")) {
            String[] kv = part.split(":::");
            if (kv.length < 2) continue;
            try {
                SlotType slot = SlotType.valueOf(kv[0]);
                ItemStack item = ItemSerializer.deserialize(kv[1]);
                result.equipped.put(slot, item);
            } catch (Exception ignored) {}
        }
        return result;
    }
}