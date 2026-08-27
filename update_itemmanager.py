import re

with open(r'src/main/java/dev/elysium/item/item/ItemManager.java', 'r', encoding='utf-8') as f:
    text = f.read()

# Replace getEquippedItemIds and add getEquippedItems
old_method = '''    public List<String> getEquippedItemIds(Player player) {
        List<String> list = new ArrayList<>();
        if (player.getInventory().getHelmet() != null) {
            String id = getItemId(player.getInventory().getHelmet());
            if (id != null) list.add(id);
        }
        if (player.getInventory().getChestplate() != null) {
            String id = getItemId(player.getInventory().getChestplate());
            if (id != null) list.add(id);
        }
        if (player.getInventory().getLeggings() != null) {
            String id = getItemId(player.getInventory().getLeggings());
            if (id != null) list.add(id);
        }
        if (player.getInventory().getBoots() != null) {
            String id = getItemId(player.getInventory().getBoots());
            if (id != null) list.add(id);
        }

        var slotData = plugin.getAccessoryManager().getSlotData(player.getUniqueId());
        if (slotData.getNecklaceId() != null) list.add(slotData.getNecklaceId());
        if (slotData.getRing1Id() != null) list.add(slotData.getRing1Id());
        if (slotData.getRing2Id() != null) list.add(slotData.getRing2Id());
        
        return list;
    }'''

new_method = '''    public List<org.bukkit.inventory.ItemStack> getEquippedItems(Player player) {
        List<org.bukkit.inventory.ItemStack> list = new ArrayList<>();
        if (player.getInventory().getHelmet() != null) list.add(player.getInventory().getHelmet());
        if (player.getInventory().getChestplate() != null) list.add(player.getInventory().getChestplate());
        if (player.getInventory().getLeggings() != null) list.add(player.getInventory().getLeggings());
        if (player.getInventory().getBoots() != null) list.add(player.getInventory().getBoots());
        
        var slotData = plugin.getAccessoryManager().getSlotData(player.getUniqueId());
        if (slotData.getNecklace() != null) list.add(slotData.getNecklace());
        if (slotData.getRing1() != null) list.add(slotData.getRing1());
        if (slotData.getRing2() != null) list.add(slotData.getRing2());
        
        return list;
    }

    public List<String> getEquippedItemIds(Player player) {
        List<String> ids = new ArrayList<>();
        for (org.bukkit.inventory.ItemStack item : getEquippedItems(player)) {
            String id = getItemId(item);
            if (id != null) ids.add(id);
        }
        return ids;
    }'''
text = text.replace(old_method, new_method)

with open(r'src/main/java/dev/elysium/item/item/ItemManager.java', 'w', encoding='utf-8') as f:
    f.write(text)

print("ItemManager.java updated with getEquippedItems.")