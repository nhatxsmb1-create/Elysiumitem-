import re

with open(r'src/main/java/dev/elysium/item/gui/LorenGui.java', 'r', encoding='utf-8') as f:
    text = f.read()

old_logic = '''            // Remove items
            selectedGear.setAmount(selectedGear.getAmount() - 1);
            selectedGem.setAmount(selectedGem.getAmount() - 1);

            // Create new socketed item
            ItemStack socketed = selectedGear.clone();
            socketed.setAmount(1);
            plugin.getItemManager().setSocket(socketed, 1, gemId);
            plugin.getItemManager().rebuildLore(socketed);
            
            player.getInventory().addItem(socketed);'''

new_logic = '''            // Create new socketed item (Clone first before amounts hit 0)
            ItemStack socketed = selectedGear.clone();
            socketed.setAmount(1);
            plugin.getItemManager().setSocket(socketed, 1, gemId);
            plugin.getItemManager().rebuildLore(socketed);
            
            // Remove old items
            selectedGear.setAmount(selectedGear.getAmount() - 1);
            selectedGem.setAmount(selectedGem.getAmount() - 1);

            // Give the new socketed item back to player
            player.getInventory().addItem(socketed);'''

text = text.replace(old_logic, new_logic)

with open(r'src/main/java/dev/elysium/item/gui/LorenGui.java', 'w', encoding='utf-8') as f:
    f.write(text)

print("Fixed item clone logic in LorenGui!")