import re

with open(r'src/main/java/dev/elysium/item/mechanic/MechanicEngine.java', 'r', encoding='utf-8') as f:
    text = f.read()

# Add Ultimate Event import and listener
imports = '''import dev.elysium.core.event.ElysiumUltimateEvent;
import dev.elysium.item.item.GemData;'''

text = text.replace('import dev.elysium.item.ElysiumItem;', 'import dev.elysium.item.ElysiumItem;\n' + imports)

listener = '''
    @EventHandler
    public void onUltimateCast(ElysiumUltimateEvent e) {
        Player player = e.getPlayer();
        List<org.bukkit.inventory.ItemStack> equipped = plugin.getItemManager().getEquippedItems(player);
        
        for (org.bukkit.inventory.ItemStack item : equipped) {
            String gemId = plugin.getItemManager().getSocket(item, 1);
            if (gemId != null) {
                GemData gem = plugin.getGemManager().getGem(gemId);
                if (gem != null) {
                    for (String mechanic : gem.getMechanics()) {
                        if (mechanic.equals("BLOOD_DOMAIN_ON_ULTIMATE")) {
                            executeBloodDomain(player);
                        } else if (mechanic.equals("FROST_NOVA_ON_ULTIMATE")) {
                            executeFrostNova(player);
                        }
                    }
                }
            }
        }
    }

    private void executeBloodDomain(Player player) {
        player.sendMessage("§4§l[!] §cKích hoạt Lãnh Địa Máu!");
        player.getWorld().spawnParticle(org.bukkit.Particle.BLOCK, player.getLocation(), 200, 5, 0.1, 5, org.bukkit.Material.REDSTONE_BLOCK.createBlockData());
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 0.5f, 0.5f);
        
        int totalMarksConsumed = 0;
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(10, 5, 10)) {
            if (entity instanceof LivingEntity target && target != player) {
                int marks = plugin.getMarkManager().getStacks(target, "BLOOD_MARK");
                if (marks > 0) {
                    totalMarksConsumed += marks;
                    plugin.getMarkManager().consumeMarks(target, "BLOOD_MARK");
                    target.damage(100.0 * marks, player); // Heavy damage
                }
            }
        }
        
        if (totalMarksConsumed > 0) {
            double heal = totalMarksConsumed * 20.0;
            player.setHealth(Math.min(player.getHealth() + heal, player.getMaxHealth()));
            player.sendMessage("§c❤ Hồi phục " + heal + " HP từ Huyết Ấn!");
        }
    }

    private void executeFrostNova(Player player) {
        player.sendMessage("§b§l[!] §fKích hoạt Sương Giá Cực Hàn!");
        player.getWorld().spawnParticle(org.bukkit.Particle.SNOWFLAKE, player.getLocation(), 300, 8, 1, 8, 0.1);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_GLASS_BREAK, 1f, 0.5f);
        
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(8, 5, 8)) {
            if (entity instanceof LivingEntity target && target != player) {
                target.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS, 100, 9, false, false));
                target.damage(50.0, player); // Flat frost damage
            }
        }
    }
'''

text = text.replace('public class MechanicEngine implements Listener {', 'public class MechanicEngine implements Listener {' + listener)

with open(r'src/main/java/dev/elysium/item/mechanic/MechanicEngine.java', 'w', encoding='utf-8') as f:
    f.write(text)

print("MechanicEngine updated with Gem execution logic!")