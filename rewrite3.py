import re

with open('src/main/java/dev/elysium/item/item/ItemManager.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Remove PlayerItemState, getState, removeState, and refreshItemInInventory
pattern1 = r'private final Map<UUID, PlayerItemState>      playerStates  = new HashMap\<\>\(\);'
content = re.sub(pattern1, '', content)

pattern2 = r'// ── Player State ──.*?// ── Getters ──'
content = re.sub(pattern2, '// ── Getters ──', content, flags=re.DOTALL)

with open('src/main/java/dev/elysium/item/item/ItemManager.java', 'w', encoding='utf-8') as f:
    f.write(content)
