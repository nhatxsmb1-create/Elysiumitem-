package dev.elysium.item.listener;

import dev.elysium.item.ElysiumItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;

public class ArmorListener implements Listener {

    private final ElysiumItem plugin;

    public ArmorListener(ElysiumItem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEquipByClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
            scheduleUpdate(player);
        } else if (e.isShiftClick() && e.getCurrentItem() != null) {
            String type = e.getCurrentItem().getType().name();
            if (type.contains("HELMET") || type.contains("CHESTPLATE") || 
                type.contains("LEGGINGS") || type.contains("BOOTS")) {
                scheduleUpdate(player);
            }
        }
    }

    @EventHandler
    public void onEquipByRightClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = e.getItem();
            if (item != null) {
                String type = item.getType().name();
                if (type.contains("HELMET") || type.contains("CHESTPLATE") || 
                    type.contains("LEGGINGS") || type.contains("BOOTS")) {
                    scheduleUpdate(e.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.getItemManager().updatePlayerInventory(e.getPlayer());
        updateStats(e.getPlayer());
    }

    private void scheduleUpdate(Player player) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> updateStats(player), 2L);
    }

    private void updateStats(Player player) {
        if (!player.isOnline()) return;

        double maxHp = 20.0 + plugin.getMechanicEngine().getTotalStat(player, "bonus-hp");
        double speed = 0.2f + (plugin.getMechanicEngine().getTotalStat(player, "bonus-speed") / 100.0);
        
        // Ensure within limits
        if (maxHp < 1) maxHp = 1;
        if (speed > 1.0f) speed = 1.0f;
        if (speed < 0.01f) speed = 0.01f;

        var attr = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(maxHp);
            if (player.getHealth() > maxHp) player.setHealth(maxHp);
        }

        player.setWalkSpeed((float) speed);
    }
}