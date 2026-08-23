import re

with open('src/main/java/dev/elysium/item/item/ItemManager.java', 'r', encoding='utf-8') as f:
    content = f.read()

pattern = r'(// Stats - format dep)'
replacement = r'''if (data.getCategory() == ElysiumItemData.ItemCategory.ACCESSORY) {
            lore.add("");
            lore.add(color("&e[Hướng dẫn] &fGõ lệnh &a/trangbi"));
            lore.add(color("&fđể gắn mảnh ghép này vào người."));
        }
        
        \1'''
        
content = re.sub(pattern, replacement, content)

with open('src/main/java/dev/elysium/item/item/ItemManager.java', 'w', encoding='utf-8') as f:
    f.write(content)
