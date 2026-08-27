import re

with open(r'src/main/java/dev/elysium/item/ElysiumItem.java', 'r', encoding='utf-8') as f:
    text = f.read()

imports = 'import org.bukkit.event.player.PlayerJoinEvent;\n'
text = text.replace('import org.bukkit.event.player.PlayerQuitEvent;', imports + 'import org.bukkit.event.player.PlayerQuitEvent;')

join_event = '''
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // Auto-update visuals (Lore, Name) of all Elysium items in inventory
        // This ensures admin config changes apply visually immediately upon login
        // while safely preserving player's Sockets (NBT).
        itemManager.updatePlayerInventory(e.getPlayer());
    }

    @EventHandler'''
text = text.replace('    @EventHandler', join_event, 1)

with open(r'src/main/java/dev/elysium/item/ElysiumItem.java', 'w', encoding='utf-8') as f:
    f.write(text)

with open(r'src/main/java/dev/elysium/item/command/AdminCommand.java', 'r', encoding='utf-8') as f:
    text = f.read()

reload_logic = '''        if (args[0].equalsIgnoreCase("reload")) {
            plugin.getItemManager().loadAll();
            plugin.getGemManager().loadGems();
            
            // Auto update all online players
            for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                plugin.getItemManager().updatePlayerInventory(p);
            }
            
            sender.sendMessage("§a[!] Đã tải lại Config Vật Phẩm & Ngọc!");
            sender.sendMessage("§a[!] Đã tự động làm mới giao diện trang bị cho toàn bộ người chơi Online!");
            return true;
        }'''

# Replace standard reload block if it exists, otherwise just insert it
if 'args[0].equalsIgnoreCase("reload")' in text:
    text = re.sub(r'if \(args\[0\]\.equalsIgnoreCase\("reload"\)\) \{.*?(?=return true;\n        })return true;\n        }', reload_logic, text, flags=re.DOTALL)
else:
    text = text.replace('if (args[0].equalsIgnoreCase("gem") && args.length == 3) {', reload_logic + '\n\n        if (args[0].equalsIgnoreCase("gem") && args.length == 3) {')

with open(r'src/main/java/dev/elysium/item/command/AdminCommand.java', 'w', encoding='utf-8') as f:
    f.write(text)

print("Auto-update logic applied!")