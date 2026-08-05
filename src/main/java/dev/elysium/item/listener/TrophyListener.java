package dev.elysium.item.listener;

import dev.elysium.item.ElysiumItem;
import dev.elysium.item.item.ElysiumItemData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Random;

public class TrophyListener implements Listener {

    private final ElysiumItem plugin;
    private final Random      rng = new Random();

    public TrophyListener(ElysiumItem plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block  block  = event.getBlock();

        // Lay trophy dang cam tren tay chinh
        ItemStack held  = player.getInventory().getItemInMainHand();
        String    itemId= plugin.getItemManager().getItemId(held);
        if (itemId == null) return;

        ElysiumItemData data = plugin.getItemManager().getItemData(itemId);
        if (data == null || data.getCategory() != ElysiumItemData.ItemCategory.TROPHY) return;
        if (data.getMastery() == null) return;

        // Apply mining speed: cong Haste potion tuong duong
        double miningSpeed = plugin.getAccessoryManager().getTrophyMiningSpeed(player);
        if (miningSpeed > 0) {
            int hasteLevel = miningSpeed >= 0.20 ? 3 : miningSpeed >= 0.10 ? 2 : 1;
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.HASTE, 40, hasteLevel - 1, true, false, false));
        }

        // Lay EXP cua loai khoang san nay
        Map<String, Integer> oreExp = data.getMastery().getOreExp();
        String matName = block.getType().name().toUpperCase();
        Integer exp = oreExp.get(matName);
        if (exp == null || exp <= 0) return;

        // Them mastery EXP
        plugin.getItemMastery().addExp(player, itemId, exp);

        // Xu ly passive effect cua trophy
        handleTrophyPassive(player, data, itemId, event, block);
    }

    // ── Trophy Passives ───────────────────────────────────────────────────────

    private void handleTrophyPassive(Player player, ElysiumItemData data,
                                      String itemId, BlockBreakEvent event, Block block) {
        int level = plugin.getItemMastery().getLevel(player, itemId);

        // Auto smelt (level 30+)
        if (level >= 30 && plugin.getItemMastery().hasUnlock(player, itemId, "auto-smelt")) {
            handleAutoSmelt(player, block, event);
        }

        // Vein miner (level 40+) - dao them 3 khoi lien ke
        if (level >= 40 && plugin.getItemMastery().hasUnlock(player, itemId, "vein-miner")) {
            if (rng.nextDouble() < 0.05) {
                handleVeinMiner(player, block, 3);
            }
        }

        // Legendary drop (level 50) - 1% ra item Elysium
        if (level >= 50 && plugin.getItemMastery().hasUnlock(player, itemId, "legendary-drop")) {
            if (rng.nextDouble() < 0.01) {
                handleLegendaryDrop(player, block);
            }
        }

        // Ancient debris finder (TROPHY_NETHER level 40)
        if (level >= 40 && plugin.getItemMastery().hasUnlock(player, itemId, "ancient-aura")) {
            findAncientDebris(player, block);
        }

        // Gem magnet (TROPHY_CRYSTAL level 35)
        if (level >= 35 && plugin.getItemMastery().hasUnlock(player, itemId, "gem-magnet")) {
            handleGemMagnet(player, block);
        }
    }

    private void handleAutoSmelt(Player player, Block block, BlockBreakEvent event) {
        Material smelted = getSmeltedResult(block.getType());
        if (smelted == null) return;

        event.setDropItems(false);
        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(smelted));
    }

    private void handleVeinMiner(Player player, Block origin, int extra) {
        Material type = origin.getType();
        int count = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block nearby = origin.getRelative(x, y, z);
                    if (nearby.getType() == type && count < extra) {
                        nearby.breakNaturally(player.getInventory().getItemInMainHand());
                        count++;
                    }
                }
            }
        }
        if (count > 0) player.sendActionBar(color("&e⛏ Vein Miner! +" + count + " khối"));
    }

    private void handleLegendaryDrop(Player player, Block block) {
        // Chon ngau nhien 1 item Elysium
        var allItems = plugin.getItemManager().getAllItems();
        if (allItems.isEmpty()) return;

        java.util.List<String> ids = new java.util.ArrayList<>(allItems.keySet());
        String randomId = ids.get(rng.nextInt(ids.size()));
        ItemStack legendaryItem = plugin.getItemManager().createItemForPlayer(randomId, player);

        block.getWorld().dropItemNaturally(block.getLocation(), legendaryItem);
        player.sendTitle(color("&6&l✦ LEGENDARY DROP!"), color("&f" +
                allItems.get(randomId).getDisplayName()), 10, 60, 10);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
    }

    private void findAncientDebris(Player player, Block origin) {
        // Scan 10m xung quanh tim Ancient Debris
        for (int x = -10; x <= 10; x += 2) {
            for (int y = -10; y <= 10; y += 2) {
                for (int z = -10; z <= 10; z += 2) {
                    Block b = origin.getRelative(x, y, z);
                    if (b.getType() == Material.ANCIENT_DEBRIS) {
                        org.bukkit.Location loc = b.getLocation().add(0.5, 0.5, 0.5);
                        origin.getWorld().spawnParticle(
                                org.bukkit.Particle.END_ROD, loc, 5, 0.2, 0.2, 0.2, 0);
                    }
                }
            }
        }
    }

    private void handleGemMagnet(Player player, Block block) {
        // Hut khoang san trong vong 5m ve player
        for (org.bukkit.entity.Entity e : block.getWorld().getNearbyEntities(
                block.getLocation(), 5, 5, 5)) {
            if (e instanceof org.bukkit.entity.Item item) {
                item.setPickupDelay(0);
                item.teleport(player.getLocation());
            }
        }
    }

    // ── Auto Smelt Table ──────────────────────────────────────────────────────

    private Material getSmeltedResult(Material ore) {
        return switch (ore) {
            case IRON_ORE, DEEPSLATE_IRON_ORE, RAW_IRON_BLOCK -> Material.IRON_INGOT;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE, NETHER_GOLD_ORE, RAW_GOLD_BLOCK -> Material.GOLD_INGOT;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE, RAW_COPPER_BLOCK -> Material.COPPER_INGOT;
            case ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP;
            default -> null;
        };
    }

    private String color(String s) { return s.replace("&", "\u00a7"); }
}
