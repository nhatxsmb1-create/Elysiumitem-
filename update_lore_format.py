import re

with open(r'src/main/java/dev/elysium/item/item/ItemManager.java', 'r', encoding='utf-8') as f:
    text = f.read()

old_format = '''                lore.add("");
                lore.add(color("&8=====[ &6LỖ KHẢM &8]====="));
                lore.add(color("&7Đang khảm: " + gem.getDisplayName()));
                for (String gl : gem.getLore()) {
                    lore.add(color(gl));
                }
                lore.add(color("&8====================="));'''

new_format = '''                lore.add("");
                lore.add(color("&8&m---------------------------"));
                lore.add(color("&6&l✦ &e&lÔ KHẢM THỨC TỈNH &6&l✦"));
                lore.add(color("&8» &fĐang Khảm: " + gem.getDisplayName()));
                for (String gl : gem.getLore()) {
                    lore.add(color("&8» " + gl));
                }
                lore.add(color("&8&m---------------------------"));'''

text = text.replace(old_format, new_format)

with open(r'src/main/java/dev/elysium/item/item/ItemManager.java', 'w', encoding='utf-8') as f:
    f.write(text)

print("Lore format updated!")