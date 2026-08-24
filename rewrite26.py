import re

with open('src/main/java/dev/elysium/item/item/ItemManager.java', 'r', encoding='utf-8') as f:
    content = f.read()

replacement = '''private String translateTradeoff(String t) {
        if (t.startsWith("HEAL_REDUCTION")) {
            String[] parts = t.split(":");
            return "Giảm " + (parts.length > 1 ? parts[1] : "0") + "% Khả năng hồi máu";
        }
        if (t.startsWith("INCOMING_DAMAGE_INCREASE")) {
            String[] parts = t.split(":");
            return "Nhận thêm " + (parts.length > 1 ? parts[1] : "0") + "% Sát thương từ kẻ địch";
        }
        return t;
    }'''

content = re.sub(r'private String translateTradeoff\(String t\) \{.*?return t;\s*\}', replacement, content, flags=re.DOTALL)

with open('src/main/java/dev/elysium/item/item/ItemManager.java', 'w', encoding='utf-8') as f:
    f.write(content)
