import re

# 1. Update AccessoryManager.java
with open(r'src/main/java/dev/elysium/item/accessory/AccessoryManager.java', 'r', encoding='utf-8') as f:
    am_text = f.read()

# Change serialize format to use the new serializer method. 
# Oh wait, AccessorySlotData already handles serialization.
# We just need to make sure AccessorySlotData.serialize() works correctly.
# AccessoryManager calls slotData.serialize() on save.

# 2. Update TrangBiGui.java
with open(r'src/main/java/dev/elysium/item/gui/TrangBiGui.java', 'r', encoding='utf-8') as f:
    tb_text = f.read()

old_necklace = 'setButton(20, new GuiButton(getSlotItem(slotData.getNecklaceId(), "Dây Chuyền", Material.CHAIN), e -> {'
new_necklace = 'setButton(20, new GuiButton(getSlotItem(slotData.getNecklace(), "Dây Chuyền", Material.CHAIN), e -> {'
tb_text = tb_text.replace(old_necklace, new_necklace)

old_ring1 = 'setButton(22, new GuiButton(getSlotItem(slotData.getRing1Id(), "Nhẫn 1", Material.GOLD_NUGGET), e -> {'
new_ring1 = 'setButton(22, new GuiButton(getSlotItem(slotData.getRing1(), "Nhẫn 1", Material.GOLD_NUGGET), e -> {'
tb_text = tb_text.replace(old_ring1, new_ring1)

old_ring2 = 'setButton(24, new GuiButton(getSlotItem(slotData.getRing2Id(), "Nhẫn 2", Material.GOLD_NUGGET), e -> {'
new_ring2 = 'setButton(24, new GuiButton(getSlotItem(slotData.getRing2(), "Nhẫn 2", Material.GOLD_NUGGET), e -> {'
tb_text = tb_text.replace(old_ring2, new_ring2)


old_handle = '''    private void handleSlotClick(Player player, AccessorySlotData.SlotType slotType, String requiredSubType, AccessorySlotData slotData) {
        String currentEquipped = slotData.getEquipped(slotType);
        
        if (currentEquipped != null) {
            // Unequip
            ItemStack item = plugin.getItemManager().createItem(currentEquipped);
            if (item != null) {
                player.getInventory().addItem(item);
                player.sendMessage("§aĐã tháo trang bị vào túi đồ!");
            } else {
                player.sendMessage("§cTrang bị này không còn tồn tại trong hệ thống, dữ liệu cũ đã bị xóa!");
            }
            slotData.unequip(slotType);'''

new_handle = '''    private void handleSlotClick(Player player, AccessorySlotData.SlotType slotType, String requiredSubType, AccessorySlotData slotData) {
        ItemStack currentEquipped = slotData.getEquippedItem(slotType);
        
        if (currentEquipped != null) {
            // Unequip
            player.getInventory().addItem(currentEquipped.clone());
            player.sendMessage("§aĐã tháo trang bị vào túi đồ!");
            slotData.unequip(slotType);'''
tb_text = tb_text.replace(old_handle, new_handle)

old_getSlotItem = '''    private ItemStack getSlotItem(String itemId, String slotName, Material defaultMat) {
        if (itemId == null || itemId.isEmpty()) {'''
new_getSlotItem = '''    private ItemStack getSlotItem(ItemStack equippedItem, String slotName, Material defaultMat) {
        if (equippedItem == null) {'''
tb_text = tb_text.replace(old_getSlotItem, new_getSlotItem)

old_getSlotItem_logic = '''        ItemStack item = plugin.getItemManager().createItem(itemId);
        if (item == null) {
            // Item ID da duoc luu nhung khong con ton tai trong config -> hien slot rong
            return new ItemBuilder(defaultMat).name("§7[Trống] §f" + slotName)
                .lore(
                    "",
                    "§cMón đồ §e" + itemId + " §ckhông còn tồn tại.",
                    "§7Hãy tháo ra và trang bị lại.",
                    "",
                    "§e[Click] §fđể tháo."
                )
                .build();
        }
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();'''

new_getSlotItem_logic = '''        ItemStack item = equippedItem.clone();
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();'''
tb_text = tb_text.replace(old_getSlotItem_logic, new_getSlotItem_logic)

with open(r'src/main/java/dev/elysium/item/gui/TrangBiGui.java', 'w', encoding='utf-8') as f:
    f.write(tb_text)

# 3. Update SelectAccessoryGui.java
with open(r'src/main/java/dev/elysium/item/gui/SelectAccessoryGui.java', 'r', encoding='utf-8') as f:
    sa_text = f.read()

old_isEquipped = '''                    // Kiem tra trang bi duy nhat
                    String current = slotData.getEquipped(targetSlot);
                    slotData.unequip(targetSlot);
                    
                    if (slotData.isItemEquipped(newId)) {
                        player.sendMessage("§cBạn đã đeo món đồ này ở ô khác rồi! (Trang bị duy nhất)");
                        slotData.equip(targetSlot, current); // tra lai nhu cu
                        player.closeInventory();
                        return;
                    }
                    
                    // Neu an toan thi trang bi
                    item.setAmount(item.getAmount() - 1);
                    if (current != null) {
                        player.getInventory().addItem(plugin.getItemManager().createItem(current));
                    }
                    slotData.equip(targetSlot, newId);'''

new_isEquipped = '''                    // Kiem tra trang bi duy nhat (dua tren Base ID)
                    ItemStack current = slotData.getEquippedItem(targetSlot);
                    slotData.unequip(targetSlot);
                    
                    // isItemEquipped checking ID in AccessorySlotData is broken now because it stores ItemStack.
                    // We need a helper here.
                    boolean alreadyEquipped = false;
                    for (AccessorySlotData.SlotType sType : AccessorySlotData.SlotType.values()) {
                        ItemStack eq = slotData.getEquippedItem(sType);
                        if (eq != null && newId.equals(plugin.getItemManager().getItemId(eq))) {
                            alreadyEquipped = true;
                            break;
                        }
                    }
                    
                    if (alreadyEquipped) {
                        player.sendMessage("§cBạn đã đeo món đồ này ở ô khác rồi! (Trang bị duy nhất)");
                        slotData.equip(targetSlot, current); // tra lai nhu cu
                        player.closeInventory();
                        return;
                    }
                    
                    // Neu an toan thi trang bi
                    ItemStack toEquip = item.clone();
                    toEquip.setAmount(1);
                    item.setAmount(item.getAmount() - 1);
                    
                    if (current != null) {
                        player.getInventory().addItem(current);
                    }
                    slotData.equip(targetSlot, toEquip);'''
sa_text = sa_text.replace(old_isEquipped, new_isEquipped)

with open(r'src/main/java/dev/elysium/item/gui/SelectAccessoryGui.java', 'w', encoding='utf-8') as f:
    f.write(sa_text)

print("GUI classes updated!")