import re

with open('src/main/java/dev/elysium/item/item/ItemManager.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Find the exact string "private String color(String s) { return s.replace("&", "\u00a7"); }"
# And cut everything after it.
idx = content.find('private String color(String s) { return s.replace("&", "\\u00a7"); }')
if idx != -1:
    content = content[:idx + len('private String color(String s) { return s.replace("&", "\\u00a7"); }')]

correct_end = """

    private String translateTradeoff(String t) {
        if (t.startsWith("HEAL_REDUCTION")) {
            String[] parts = t.split(":");
            return "Giảm " + (parts.length > 1 ? parts[1] : "0") + "% Khả năng hồi máu";
        }
        return t;
    }
}
"""
content = content + correct_end

with open('src/main/java/dev/elysium/item/item/ItemManager.java', 'w', encoding='utf-8') as f:
    f.write(content)
