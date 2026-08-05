package dev.elysium.item.command;

import dev.elysium.item.gui.GuiListener;
import dev.elysium.item.gui.MasteryGui;

import dev.elysium.item.ElysiumItem;
import dev.elysium.item.item.ElysiumItemData;
import dev.elysium.item.item.PlayerItemState;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemCommand implements CommandExecutor {

    private final ElysiumItem plugin;

    public ItemCommand(ElysiumItem plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Chi player dung duoc!"); return true;
        }

        if (args.length == 0) { sendHelp(player); return true; }

        switch (args[0].toLowerCase()) {

            case "info", "i" -> {
                // Kiem tra item dang cam hoac dang mac
                ItemStack held = player.getInventory().getItemInMainHand();
                String itemId  = plugin.getItemManager().getItemId(held);
                if (itemId == null) {
                    player.sendMessage(color("&cBan chua cam item Elysium nao!"));
                    player.sendMessage(color("&7Hoac dung &e/eitem list &7de xem tat ca item ban dang co."));
                    return true;
                }

                ElysiumItemData data  = plugin.getItemManager().getItemData(itemId);
                PlayerItemState state = plugin.getItemManager().getState(player);
                int    level   = plugin.getItemMastery().getLevelFromExp(data, state.getExp(itemId));
                long   totalExp= state.getExp(itemId);
                long   expNext = plugin.getItemMastery().getExpForNextLevel(data, level);
                long   expCur  = totalExp - plugin.getItemMastery().getExpForLevel(data, level);

                player.sendMessage(color("&5&l=== " + data.getDisplayName() + " ==="));
                player.sendMessage(color("  &7Loai: &f" + data.getCategory() + " | " + data.getSubType()));
                player.sendMessage(color("  &6Mastery: &e" + level + "/" + data.getMastery().getMaxLevel()));
                player.sendMessage(color("  &7EXP: &f" + expCur + "/" + expNext));

                // Hien unlock da co
                player.sendMessage(color("  &7Bonus da mo:"));
                for (var entry : data.getMastery().getBonuses().entrySet()) {
                    boolean unlocked = level >= entry.getKey();
                    player.sendMessage(color("  " + (unlocked ? "&a✔" : "&7○")
                            + " &7Lv." + entry.getKey() + ": &f" + entry.getValue().getDescription()));
                }
            }

            case "list", "l" -> {
                player.sendMessage(color("&5=== Item Elysium cua ban ==="));
                var equipped = plugin.getItemManager().getEquippedItemIds(player);
                if (equipped.isEmpty()) {
                    player.sendMessage(color("  &7Chua trang bi item Elysium nao."));
                } else {
                    for (String id : equipped) {
                        ElysiumItemData data = plugin.getItemManager().getItemData(id);
                        int level = plugin.getItemMastery().getLevel(player, id);
                        player.sendMessage(color("  &e" + data.getDisplayName()
                                + " &7[Lv." + level + "]"));
                    }
                }
            }

            case "mastery", "m" -> {
                // Mo GUI Mastery thay vi chat
                MasteryGui gui = new MasteryGui(plugin);
                GuiListener.register(player.getUniqueId(), gui);
                gui.open(player);
            }

            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player p) {
        p.sendMessage(color("&5=== ElysiumItem ==="));
        p.sendMessage(color("  &7/eitem info &f- Thong tin item dang cam"));
        p.sendMessage(color("  &7/eitem list &f- Item dang trang bi"));
        p.sendMessage(color("  &7/eitem mastery &f- Mo GUI Mastery Collection"));
    }

    private String color(String s) { return s.replace("&", "\u00a7"); }
}
