import re

with open(r'src/main/java/dev/elysium/item/item/ItemManager.java', 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('lore.add(color("&a[!] Mặc vào người để kích hoạt"));', 'lore.add(color("&a[!] Dùng lệnh &f/trangbi &ađể mặc vào người"));')

with open(r'src/main/java/dev/elysium/item/item/ItemManager.java', 'w', encoding='utf-8') as f:
    f.write(text)