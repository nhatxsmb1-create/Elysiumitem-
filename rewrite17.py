import re

with open('src/main/java/dev/elysium/item/item/ItemManager.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace the createItem method's lore building part
pattern = r'(String typeBadge = buildTypeBadge\(data\);).*?meta\.setLore\(lore\);'

replacement = """String typeBadge = buildTypeBadge(data);
        lore.add(color("&f&l« " + typeBadge + "&f&l »"));
        lore.add("");

        if (!data.getStats().isEmpty()) {
            lore.add(color("&e&l✦ &6&lCHỈ SỐ TĂNG THÊM &e&l✦"));
            for (Map.Entry<String, Object> e : data.getStats().entrySet()) {
                lore.add(color("  &8▪ " + formatStat(e.getKey(), e.getValue())));
            }
            lore.add("");
        }

        if (!data.getTradeoffs().isEmpty()) {
            lore.add(color("&4&l⚠️ &c&lĐÁNH ĐỔI &4&l⚠️"));
            for (String t : data.getTradeoffs()) {
                lore.add(color("  &8▪ &c" + translateTradeoff(t)));
            }
            lore.add("");
        }

        data.getLore().forEach(l -> lore.add(color(l)));
        lore.add("");

        if (data.getCategory() == ElysiumItemData.ItemCategory.ACCESSORY) {
            lore.add(color("&8&m                                  "));
            lore.add(color("&a&l[!] &aGõ lệnh &f/trangbi &ađể sử dụng"));
            lore.add(color("&8&m                                  "));
        } else if (data.getCategory() == ElysiumItemData.ItemCategory.ARMOR) {
            lore.add(color("&8&m                                  "));
            lore.add(color("&a&l[!] &aMặc vào người để kích hoạt"));
            lore.add(color("&8&m                                  "));
        }
        
        lore.add(color("&8ID: " + itemId));
        meta.setLore(lore);"""

content = re.sub(pattern, replacement, content, flags=re.DOTALL)

# Add translateTradeoff function at the bottom before the last '}'
func = """
    private String translateTradeoff(String t) {
        if (t.startsWith("HEAL_REDUCTION")) {
            String[] parts = t.split(":");
            return "Giảm " + (parts.length > 1 ? parts[1] : "0") + "% Khả năng hồi máu";
        }
        return t;
    }
}"""
content = content.replace("}\n", func, 1) if content.endswith("}\n") else content[:-1] + func

# Actually, to be safe, I'll just append it right before the last closing brace
content = re.sub(r'\}\s*$', func, content)

with open('src/main/java/dev/elysium/item/item/ItemManager.java', 'w', encoding='utf-8') as f:
    f.write(content)
