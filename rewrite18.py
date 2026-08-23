import re

with open('src/main/java/dev/elysium/item/item/ItemManager.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Strip all occurrences of translateTradeoff and anything after the class closing
content = re.sub(r'(// NOTE: refreshItemInInventory.*?$|private String translateTradeoff.*?$)', '', content, flags=re.MULTILINE|re.DOTALL)

# Ensure the class closes correctly, we just strip trailing braces and whitespace
content = content.rstrip().rstrip('}')

# Now we inject translateTradeoff and close the class
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
