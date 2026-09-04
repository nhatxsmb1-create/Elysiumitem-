import os

def write_file(path, content):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

sag = '''package dev.elysium.item.gui;
import dev.elysium.core.gui.ElysiumGui;
import dev.elysium.core.gui.GuiButton;
import dev.elysium.core.gui.ItemBuilder;
import dev.elysium.item.ElysiumItem;
import dev.elysium.item.accessory.AccessorySlotData;
import dev.elysium.item.item.ElysiumItemData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import java.util.ArrayList;
import java.util.List;
public class SelectAccessoryGui extends ElysiumGui {
    private final ElysiumItem plugin;
    private final AccessorySlotData.SlotType targetSlot;
    private final String requiredSubType;
    public SelectAccessoryGui(ElysiumItem plugin, AccessorySlotData.SlotType targetSlot, String requiredSubType) {
        super("§8§lChọn " + requiredSubType, 54);
        this.plugin = plugin;
        this.targetSlot = targetSlot;
        this.requiredSubType = requiredSubType;
    }
    @Override
    public void build(Player player) {
        fill(ItemBuilder.filler());
        List<ItemStack> found = new ArrayList<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            if (targetSlot == AccessorySlotData.SlotType.CORE) {
                if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey("elysiumcombat", "combat_core"), PersistentDataType.STRING)) {
                    found.add(item);
                }
            } else {
                String id = plugin.getItemManager().getItemId(item);
                if (id != null) {
                    ElysiumItemData data = plugin.getItemManager().getItemData(id);
                    if (data != null && data.getSubType().equalsIgnoreCase(requiredSubType)) {
                        found.add(item);
                    }
                }
            }
        }
        if (found.isEmpty()) {
            setButton(22, new GuiButton(
                new ItemBuilder(Material.BARRIER).name("§cTrống").lore("§7Bạn không có trang bị " + requiredSubType + " nào trong túi!").build(),
                e -> {
                    e.setCancelled(true);
                    dev.elysium.core.ElysiumCore.getInstance().getGuiManager().open(player, new TrangBiGui(plugin));
                }
            ));
        } else {
            int slot = 10;
            for (ItemStack item : found) {
                if (slot % 9 == 8) slot += 2;
                if (slot > 43) break;
                ItemStack display = item.clone();
                org.bukkit.inventory.meta.ItemMeta meta = display.getItemMeta();
                List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();
                lore.add("");
                lore.add("§a[Click] §fđể trang bị");
                meta.setLore(lore);
                display.setItemMeta(meta);
                setButton(slot, new GuiButton(display, e -> {
                    e.setCancelled(true);
                    AccessorySlotData slotData = plugin.getAccessoryManager().getSlotData(player.getUniqueId());
                    ItemStack current = slotData.getEquippedItem(targetSlot);
                    slotData.unequip(targetSlot);
                    
                    if (targetSlot != AccessorySlotData.SlotType.CORE) {
                        String newId = plugin.getItemManager().getItemId(item);
                        boolean alreadyEquipped = false;
                        for (AccessorySlotData.SlotType sType : AccessorySlotData.SlotType.values()) {
                            if (sType == AccessorySlotData.SlotType.CORE) continue;
                            ItemStack eq = slotData.getEquippedItem(sType);
                            if (eq != null && newId.equals(plugin.getItemManager().getItemId(eq))) {
                                alreadyEquipped = true;
                                break;
                            }
                        }
                        if (alreadyEquipped) {
                            player.sendMessage("§cBạn đã đeo món đồ này ở ô khác rồi! (Trang bị duy nhất)");
                            slotData.equip(targetSlot, current);
                            player.closeInventory();
                            return;
                        }
                    }
                    
                    ItemStack toEquip = item.clone();
                    toEquip.setAmount(1);
                    item.setAmount(item.getAmount() - 1);
                    
                    if (current != null) {
                        player.getInventory().addItem(current);
                    }
                    slotData.equip(targetSlot, toEquip);
                    
                    if (targetSlot == AccessorySlotData.SlotType.CORE) {
                        try {
                            String coreName = toEquip.getItemMeta().getPersistentDataContainer().get(new NamespacedKey("elysiumcombat", "combat_core"), PersistentDataType.STRING);
                            Class<?> apiClass = Class.forName("dev.elysium.combat.api.CombatAPI");
                            java.lang.reflect.Method equip = apiClass.getMethod("equipCore", Player.class, String.class);
                            equip.invoke(null, player, coreName);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    
                    player.sendMessage("§aĐã trang bị thành công!");
                    dev.elysium.core.ElysiumCore.getInstance().getGuiManager().open(player, new TrangBiGui(plugin));
                }));
                slot++;
            }
        }
        setButton(49, new GuiButton(new ItemBuilder(Material.ARROW).name("§cQuay lại").build(), e -> {
            e.setCancelled(true);
            dev.elysium.core.ElysiumCore.getInstance().getGuiManager().open(player, new TrangBiGui(plugin));
        }));
    }
}
'''
write_file('src/main/java/dev/elysium/item/gui/SelectAccessoryGui.java', sag)
print("Updated SelectAccessoryGui.java")