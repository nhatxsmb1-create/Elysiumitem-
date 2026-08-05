package dev.elysium.item.listener;

import dev.elysium.item.ElysiumItem;
import dev.elysium.item.item.ElysiumItemData;
import dev.elysium.item.item.PlayerItemState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ArmorListener implements Listener {

    // ── Armor Equip/Unequip ───────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArmorEquip(org.bukkit.event.player.PlayerItemHeldEvent e) {
        // Kiem tra khi doi item tren tay - khong ap dung cho giap
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getInventory().getType() != org.bukkit.event.inventory.InventoryType.CRAFTING) return;

        // Delay 1 tick de cho Bukkit update armor truoc
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            reapplyArmorStats(player);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDrop(org.bukkit.event.player.PlayerDropItemEvent e) {
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            reapplyArmorStats(e.getPlayer());
        }, 1L);
    }

    private void reapplyArmorStats(Player player) {
        // Remove tat ca armor stats cu
        for (org.bukkit.inventory.ItemStack armor : player.getInventory().getArmorContents()) {
            String id = plugin.getItemManager().getItemId(armor);
            if (id != null) plugin.getAccessoryManager().applyArmorStats(player, id, false);
        }
        // Apply lai theo giap hien tai
        for (org.bukkit.inventory.ItemStack armor : player.getInventory().getArmorContents()) {
            String id = plugin.getItemManager().getItemId(armor);
            if (id != null) plugin.getAccessoryManager().applyArmorStats(player, id, true);
        }
    }

    private final ElysiumItem plugin;

    public ArmorListener(ElysiumItem plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Lay tat ca item dang mac
        List<String> equipped = plugin.getItemManager().getEquippedItemIds(player);
        if (equipped.isEmpty()) return;

        PlayerItemState state = plugin.getItemManager().getState(player);

        for (String itemId : equipped) {
            ElysiumItemData data = plugin.getItemManager().getItemData(itemId);
            if (data == null || data.getMastery() == null) continue;

            // Chi armor va accessory moi tang EXP khi bi danh
            if (data.getCategory() == ElysiumItemData.ItemCategory.TROPHY) continue;

            int expPerHit = data.getMastery().getExpPerHit();
            plugin.getItemMastery().addExp(player, itemId, expPerHit);

            // Xu ly passive effect khi bi danh
            handleArmorPassive(player, data, itemId, state, event);
        }
    }

    // ── Passive Effects ───────────────────────────────────────────────────────

    private void handleArmorPassive(Player player, ElysiumItemData data,
                                     String itemId, PlayerItemState state,
                                     EntityDamageEvent event) {
        int level = plugin.getItemMastery().getLevel(player, itemId);

        switch (data.getId()) {

            // RING_OF_WARRIOR: Phan sat thuong khi MASTERY 30
            case "RING_OF_WARRIOR" -> {
                if (level >= 30 && event instanceof EntityDamageByEntityEvent edbe) {
                    double reflect = plugin.getItemMastery().getTotalBonus(player, itemId, "reflect-chance");
                    if (Math.random() < reflect && edbe.getDamager() instanceof org.bukkit.entity.LivingEntity attacker) {
                        attacker.damage(event.getFinalDamage() * 0.15, player);
                        player.sendActionBar(color("&c⟲ Phan sat thuong!"));
                    }
                }
            }

            // NECKLACE_OF_GUARDIAN: Revive chance
            case "NECKLACE_OF_GUARDIAN" -> {
                if (level >= 30) {
                    double reviveChance = plugin.getItemMastery().getTotalBonus(player, itemId, "revive-chance");
                    if (player.getHealth() - event.getFinalDamage() <= 0
                            && Math.random() < reviveChance
                            && !state.hasEffect("REVIVE_USED")) {
                        event.setCancelled(true);
                        player.setHealth(player.getMaxHealth() * 0.3);
                        state.setEffect("REVIVE_USED", 300_000); // 5 phut cooldown
                        player.sendTitle(color("&a✦ Hộ Vệ Kích Hoạt!"), color("&f20% không chết"), 5, 40, 10);
                        player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_TOTEM_USE, 1f, 1f);
                    }
                }
            }

            // CHEST_WARLORD: Warlord shield moi 5 hit
            case "CHEST_WARLORD" -> {
                if (level >= 40) {
                    int hits = state.incrementHitCounter(itemId);
                    if (hits >= 5) {
                        state.resetHitCounter(itemId);
                        state.setEffect("WARLORD_SHIELD", 3000); // 3s shield
                        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.RESISTANCE, 60, 4));
                        player.sendActionBar(color("&6⚔ Warlord Shield!"));
                        player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
                    }
                }
            }

            // CHEST_SHADOW: Dodge chance
            case "CHEST_SHADOW" -> {
                double dodge = plugin.getItemMastery().getTotalBonus(player, itemId, "dodge-chance");
                if (dodge > 0 && Math.random() < dodge) {
                    event.setCancelled(true);

                    // Shadow step khi dodge (level 30+)
                    if (level >= 30) {
                        org.bukkit.util.Vector dir = player.getLocation().getDirection().multiply(-2);
                        player.setVelocity(dir);
                    }

                    player.sendActionBar(color("&5✦ Dodge!"));
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);
                }
            }

            // BOOTS_SWIFT: Fall damage reduce
            case "BOOTS_SWIFT" -> {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    double reduce = plugin.getItemMastery().getTotalBonus(player, itemId, "fall-damage-reduce");
                    if (reduce > 0) event.setDamage(event.getDamage() * (1 - reduce));
                }
            }
        }
    }

    // ── Dragon Rage (HELMET_DRAGON lv30+) ────────────────────────────────────
    // Check khi player danh ke thu - neu HP thap thi tang damage
    // Duoc xu ly trong CombatListener

    private String color(String s) { return s.replace("&", "\u00a7"); }
}
