import re

with open(r'src/main/java/dev/elysium/item/ElysiumItem.java', 'r', encoding='utf-8') as f:
    text = f.read()

# Add import
text = text.replace('import dev.elysium.item.item.ItemManager;', 'import dev.elysium.item.item.ItemManager;\nimport dev.elysium.item.item.GemManager;')

# Add field
text = text.replace('private ItemManager  itemManager;', 'private ItemManager  itemManager;\n    private GemManager gemManager;')

# Instantiate and load
old_init = '''        itemManager  = new ItemManager(this);
        itemManager.loadAll(); // LOAD DATA HERE'''
new_init = '''        gemManager = new GemManager(this);
        gemManager.loadGems();
        itemManager  = new ItemManager(this);
        itemManager.loadAll(); // LOAD DATA HERE'''
text = text.replace(old_init, new_init)

# Add getter
text = text.replace('public ItemManager        getItemManager()  { return itemManager; }', 'public ItemManager        getItemManager()  { return itemManager; }\n    public GemManager         getGemManager()   { return gemManager; }')

with open(r'src/main/java/dev/elysium/item/ElysiumItem.java', 'w', encoding='utf-8') as f:
    f.write(text)

print("ElysiumItem.java updated with GemManager.")