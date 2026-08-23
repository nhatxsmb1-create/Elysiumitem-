package dev.elysium.item.item;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class ElysiumItemData {

    public enum ItemCategory { ACCESSORY, ARMOR, TROPHY }
    public enum AccessoryType { RING, NECKLACE, CHARM }
    public enum ArmorType { HELMET, CHESTPLATE, LEGGINGS, BOOTS }

    private final String        id;
    private final String        displayName;
    private final ItemCategory  category;
    private final String        subType;      // RING/NECKLACE/CHARM/HELMET.../TROPHY
    private final String        material;
    private final String        color;        // Hex color for leather armor
    private final int           modelData;
    private final List<String>  lore;
    private final Map<String, Object> stats;  // Keep base stats just in case
    
    // BUILD FRAGMENTS
    private final List<String> mechanics;
    private final List<String> tradeoffs;

    public ElysiumItemData(String id, String displayName, ItemCategory category,
                           String subType, String material, String color, int modelData,
                           List<String> lore, Map<String, Object> stats,
                           List<String> mechanics, List<String> tradeoffs) {
        this.id          = id;
        this.displayName = displayName;
        this.category    = category;
        this.subType     = subType;
        this.material    = material;
        this.color       = color;
        this.modelData   = modelData;
        this.lore        = lore != null ? lore : new ArrayList<>();
        this.stats       = stats != null ? stats : new HashMap<>();
        this.mechanics   = mechanics != null ? mechanics : new ArrayList<>();
        this.tradeoffs   = tradeoffs != null ? tradeoffs : new ArrayList<>();
    }

    public String        getId()          { return id; }
    public String        getDisplayName() { return displayName; }
    public ItemCategory  getCategory()    { return category; }
    public String        getSubType()     { return subType; }
    public String        getMaterial()    { return material; }
    public String        getColor()       { return color; }
    public int           getModelData()   { return modelData; }
    public List<String>  getLore()        { return lore; }
    public Map<String, Object> getStats() { return stats; }
    
    public List<String> getMechanics()    { return mechanics; }
    public List<String> getTradeoffs()    { return tradeoffs; }

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
}
