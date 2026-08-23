package dev.elysium.item.accessory;

import java.util.HashMap;
import java.util.Map;

public class AccessorySlotData {

    public enum SlotType { RING_1, RING_2, NECKLACE }

    private final Map<SlotType, String> equipped = new HashMap<>();

    public AccessorySlotData() {
        equipped.put(SlotType.RING_1,     null);
        equipped.put(SlotType.RING_2,     null);
        equipped.put(SlotType.NECKLACE, null);
    }

    public String  getEquipped(SlotType slot)              { return equipped.get(slot); }
    public boolean isEquipped(SlotType slot)               { return equipped.get(slot) != null; }
    public void    equip(SlotType slot, String itemId)     { equipped.put(slot, itemId); }
    public void    unequip(SlotType slot)                  { equipped.put(slot, null); }

    public String getNecklaceId() { return getEquipped(SlotType.NECKLACE); }
    public String getRing1Id() { return getEquipped(SlotType.RING_1); }
    public String getRing2Id() { return getEquipped(SlotType.RING_2); }

    public boolean isItemEquipped(String itemId) {
        return equipped.values().stream().anyMatch(itemId::equals);
    }

    public SlotType getSlotOf(String itemId) {
        for (Map.Entry<SlotType, String> e : equipped.entrySet()) {
            if (itemId.equals(e.getValue())) return e.getKey();
        }
        return null;
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        for (SlotType slot : SlotType.values()) {
            if (sb.length() > 0) sb.append(",");
            sb.append(slot.name()).append(":").append(equipped.getOrDefault(slot, "null"));
        }
        return sb.toString();
    }

    public static AccessorySlotData deserialize(String data) {
        AccessorySlotData result = new AccessorySlotData();
        if (data == null || data.isBlank()) return result;
        for (String part : data.split(",")) {
            String[] kv = part.split(":");
            if (kv.length < 2) continue;
            try {
                SlotType slot = SlotType.valueOf(kv[0]);
                String   item = kv[1].equals("null") ? null : kv[1];
                result.equipped.put(slot, item);
            } catch (IllegalArgumentException ignored) {}
        }
        return result;
    }
}