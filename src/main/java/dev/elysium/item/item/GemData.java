package dev.elysium.item.item;

import java.util.List;

public class GemData {
    private final String id;
    private final String displayName;
    private final List<String> lore;
    private final List<String> mechanics;

    public GemData(String id, String displayName, List<String> lore, List<String> mechanics) {
        this.id = id;
        this.displayName = displayName;
        this.lore = lore;
        this.mechanics = mechanics;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public List<String> getLore() { return lore; }
    public List<String> getMechanics() { return mechanics; }
}