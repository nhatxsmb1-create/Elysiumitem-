import re

with open('src/main/java/dev/elysium/item/item/ItemManager.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Make createItemForPlayer just return createItem
pattern = r'public ItemStack createItemForPlayer\(String itemId, Player player\)\s*\{.*?(?=public void giveItem)'
replacement = """public ItemStack createItemForPlayer(String itemId, Player player) {
        return createItem(itemId);
    }

    """
content = re.sub(pattern, replacement, content, flags=re.DOTALL)

with open('src/main/java/dev/elysium/item/item/ItemManager.java', 'w', encoding='utf-8') as f:
    f.write(content)
