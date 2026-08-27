import re

with open(r'src/main/java/dev/elysium/item/item/ItemManager.java', 'r', encoding='utf-8') as f:
    text = f.read()

# Add SOCKET_1_KEY correctly
old_key = 'public static final String ITEM_ID_KEY = "elysium_item_id";'
new_key = '''public static final String ITEM_ID_KEY = "elysium_item_id";
    public static final String SOCKET_1_KEY = "elysium_socket_1";'''
text = text.replace(old_key, new_key)

with open(r'src/main/java/dev/elysium/item/item/ItemManager.java', 'w', encoding='utf-8') as f:
    f.write(text)

print("ItemManager.java fixed!")