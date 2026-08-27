import re

with open(r'src/main/java/dev/elysium/item/mechanic/MechanicEngine.java', 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('import dev.elysium.item.item.GemData;', 'import dev.elysium.item.item.GemData;\nimport dev.elysium.core.event.ElysiumDashEvent;')

old_dash = '''    public void onPlayerDash(Player player) {
        List<String> equipped = plugin.getItemManager().getEquippedItemIds(player);'''

new_dash = '''    @EventHandler
    public void onPlayerDash(ElysiumDashEvent event) {
        Player player = event.getPlayer();
        List<String> equipped = plugin.getItemManager().getEquippedItemIds(player);'''

text = text.replace(old_dash, new_dash)

with open(r'src/main/java/dev/elysium/item/mechanic/MechanicEngine.java', 'w', encoding='utf-8') as f:
    f.write(text)

with open(r'src/main/resources/gems.yml', 'r', encoding='utf-8') as f:
    gems = f.read()

old_gem = '''      - "&7KHI TUNG CHIÊU CUỐI (Ultimate):"
      - "&7Rút cạn Huyết Ấn xung quanh để tạo ra"
      - "&7một Lãnh Địa Máu cực lớn."'''

new_gem = '''      - "&7KHI TUNG CHIÊU CUỐI (Tất cả vũ khí):"
      - "&7Kích hoạt Lãnh Địa Máu, rút cạn toàn"
      - "&7bộ Huyết Ấn trên kẻ địch xung quanh"
      - "&7(10m). Gây Sát thương Chuẩn và Hồi HP"
      - "&7cho bản thân tương ứng với số Huyết Ấn!"'''

gems = gems.replace(old_gem, new_gem)

with open(r'src/main/resources/gems.yml', 'w', encoding='utf-8') as f:
    f.write(gems)

print("MechanicEngine and Gems.yml fixed!")