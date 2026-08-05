package dev.elysium.item.item;

import java.util.*;

public class PlayerItemState {

    private final UUID playerUuid;

    // itemId -> exp
    private final Map<String, Long> itemExp    = new HashMap<>();
    private final Set<String>       dirtyItems = new HashSet<>();

    // Runtime effect tracking
    // itemId -> expire millis (cho cac effect co thoi han)
    private final Map<String, Long> activeEffects = new HashMap<>();

    // Combat hit counter cho armor passive (warlord shield v.v.)
    private final Map<String, Integer> hitCounters = new HashMap<>();

    public PlayerItemState(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    // ── EXP ──────────────────────────────────────────────────────────────────

    public void addExp(String itemId, long amount) {
        itemExp.merge(itemId, amount, Long::sum);
        dirtyItems.add(itemId);
    }

    public long getExp(String itemId) {
        return itemExp.getOrDefault(itemId, 0L);
    }

    public void loadFromDb(Map<String, Long> dbData) {
        itemExp.putAll(dbData);
    }

    public Map<String, Long> flushDirty() {
        Map<String, Long> toSave = new HashMap<>();
        for (String id : dirtyItems) toSave.put(id, itemExp.getOrDefault(id, 0L));
        dirtyItems.clear();
        return toSave;
    }

    public Map<String, Long> getAllExp() {
        return Collections.unmodifiableMap(itemExp);
    }

    // ── Active Effects ────────────────────────────────────────────────────────

    public void setEffect(String effectId, long durationMs) {
        activeEffects.put(effectId, System.currentTimeMillis() + durationMs);
    }

    public boolean hasEffect(String effectId) {
        Long exp = activeEffects.get(effectId);
        if (exp == null) return false;
        if (System.currentTimeMillis() > exp) { activeEffects.remove(effectId); return false; }
        return true;
    }

    // ── Hit Counter ───────────────────────────────────────────────────────────

    public int incrementHitCounter(String itemId) {
        return hitCounters.merge(itemId, 1, Integer::sum);
    }

    public void resetHitCounter(String itemId) {
        hitCounters.remove(itemId);
    }

    public int getHitCounter(String itemId) {
        return hitCounters.getOrDefault(itemId, 0);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public UUID getPlayerUuid() { return playerUuid; }
}
