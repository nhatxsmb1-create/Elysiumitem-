package dev.elysium.item.command;

import dev.elysium.item.ElysiumItem;
import dev.elysium.item.item.ElysiumItemData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemCommand implements CommandExecutor {

    private final ElysiumItem plugin;

    public ItemCommand(ElysiumItem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            player.sendMessage("§e/eitem info §7- Xem thông tin trang bị đang cầm");
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            ItemStack held = player.getInventory().getItemInMainHand();
            String itemId = plugin.getItemManager().getItemId(held);
            
            if (itemId == null) {
                player.sendMessage("§cBạn không cầm trang bị Elysium nào!");
                return true;
            }

            ElysiumItemData data = plugin.getItemManager().getItemData(itemId);
            if (data == null) {
                player.sendMessage("§cLỗi: Không tìm thấy dữ liệu trang bị.");
                return true;
            }

            player.sendMessage("§8=========================");
            player.sendMessage("§eTên: " + data.getDisplayName());
            player.sendMessage("§7Loại: §f" + data.getSubType());
            
            if (!data.getMechanics().isEmpty()) {
                player.sendMessage("§aCơ Chế Mảnh Ghép:");
                for (String m : data.getMechanics()) {
                    player.sendMessage(" §8- §f" + m);
                }
            }
            
            if (!data.getTradeoffs().isEmpty()) {
                player.sendMessage("§cĐánh Đổi (Trade-offs):");
                for (String m : data.getTradeoffs()) {
                    player.sendMessage(" §8- §c" + m);
                }
            }
            player.sendMessage("§8=========================");
            return true;
        }

        return true;
    }
}