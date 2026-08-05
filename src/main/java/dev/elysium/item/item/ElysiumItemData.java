package dev.elysium.item.item;

import java.util.List;
import java.util.Map;

public class ElysiumItemData {

    public enum ItemCategory { ACCESSORY, ARMOR, TROPHY }
    public enum AccessoryType { RING, NECKLACE, CHARM }
    public enum ArmorType { HELMET, CHESTPLATE, LEGGINGS, BOOTS }

    private final String        id;
    private final String        displayName;
    private final ItemCategory  category;
    private final String        subType;      // RING/NECKLACE/CHARM/HELMET.../TROPHY
    private final String        material;
    private final int           modelData;
    private final List<String>  lore;
    private final Map<String, Object> stats;
    private final MasteryConfig mastery;

    public ElysiumItemData(String id, String displayName, ItemCategory category,
                           String subType, String material, int modelData,
                           List<String> lore, Map<String, Object> stats,
                           MasteryConfig mastery) {
        this.id          = id;
        this.displayName = displayName;
        this.category    = category;
        this.subType     = subType;
        this.material    = material;
        this.modelData   = modelData;
        this.lore        = lore;
        this.stats       = stats;
        this.mastery     = mastery;
    }

    public String        getId()          { return id; }
    public String        getDisplayName() { return displayName; }
    public ItemCategory  getCategory()    { return category; }
    public String        getSubType()     { return subType; }
    public String        getMaterial()    { return material; }
    public int           getModelData()   { return modelData; }
    public List<String>  getLore()        { return lore; }
    public Map<String, Object> getStats() { return stats; }
    public MasteryConfig getMastery()     { return mastery; }

    public double getStatDouble(String key) {
        Object v = stats.get(key);
        return v instanceof Number n ? n.doubleValue() : 0.0;
    }
    public int getStatInt(String key) {
        Object v = stats.get(key);
        return v instanceof Number n ? n.intValue() : 0;
    }
    public boolean getStatBool(String key) {
        Object v = stats.get(key);
        return v instanceof Boolean b && b;
    }

    // ── MasteryConfig ─────────────────────────────────────────────────────────

    public static class MasteryConfig {
        private final int                        maxLevel;
        private final int                        expPerHit;       // For accessory/armor
        private final Map<String, Integer>       oreExp;          // For trophy: material -> exp
        private final Map<Integer, BonusUnlock>  bonuses;         // level -> bonus

        public MasteryConfig(int maxLevel, int expPerHit,
                             Map<String, Integer> oreExp,
                             Map<Integer, BonusUnlock> bonuses) {
            this.maxLevel  = maxLevel;
            this.expPerHit = expPerHit;
            this.oreExp    = oreExp;
            this.bonuses   = bonuses;
        }

        public int                       getMaxLevel()  { return maxLevel; }
        public int                       getExpPerHit() { return expPerHit; }
        public Map<String, Integer>      getOreExp()    { return oreExp; }
        public Map<Integer, BonusUnlock> getBonuses()   { return bonuses; }
        public BonusUnlock getBonus(int level)          { return bonuses.get(level); }
    }

    // ── BonusUnlock ───────────────────────────────────────────────────────────

    public static class BonusUnlock {
        private final String             description;
        private final Map<String, Object> values;

        public BonusUnlock(String description, Map<String, Object> values) {
            this.description = description;
            this.values      = values;
        }

        public String getDescription() { return description; }

        public double getDouble(String key) {
            Object v = values.get(key);
            return v instanceof Number n ? n.doubleValue() : 0.0;
        }
        public int getInt(String key) {
            Object v = values.get(key);
            return v instanceof Number n ? n.intValue() : 0;
        }
        public boolean getBool(String key) {
            Object v = values.get(key);
            return v instanceof Boolean b && b;
        }
        public boolean has(String key) { return values.containsKey(key); }
    }
}
