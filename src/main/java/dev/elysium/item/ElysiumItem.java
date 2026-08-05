package dev.elysium.item;

import dev.elysium.item.command.AdminCommand;
import dev.elysium.item.command.ItemCommand;
import dev.elysium.item.database.ItemDatabase;
import dev.elysium.item.gui.GuiListener;
import dev.elysium.item.item.ItemManager;
import dev.elysium.item.accessory.AccessoryManager;
import dev.elysium.item.listener.ArmorListener;
import dev.elysium.item.listener.DatabaseListener;
import dev.elysium.item.listener.TrophyListener;
import dev.elysium.item.mastery.ItemMastery;
import org.bukkit.plugin.java.JavaPlugin;

public class ElysiumItem extends JavaPlugin {

    private static ElysiumItem instance;

    private ItemDatabase itemDatabase;
    private ItemManager  itemManager;
    private ItemMastery       itemMastery;
    private AccessoryManager  accessoryManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResource("items.yml",    false);
        saveResource("armors.yml",   false);
        saveResource("trophies.yml", false);
        saveResource("config.yml",   false);

        // Init
        itemDatabase = new ItemDatabase(this);
        itemDatabase.initialize();

        itemMastery       = new ItemMastery(this);
        accessoryManager  = new AccessoryManager(this);
        itemManager  = new ItemManager(this);

        // Commands
        getCommand("eitem").setExecutor(new ItemCommand(this));
        getCommand("trangbi").setExecutor(new dev.elysium.item.command.TrangBiCommand(this));
        getCommand("eitemadmin").setExecutor(new AdminCommand(this));

        // Listeners
        getServer().getPluginManager().registerEvents(new ArmorListener(this),   this);
        getServer().getPluginManager().registerEvents(new TrophyListener(this),  this);
        getServer().getPluginManager().registerEvents(new DatabaseListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(),          this);

        getLogger().info("=== ElysiumItem v" + getDescription().getVersion() + " enabled! ===");
        getLogger().info("Items loaded: " + itemManager.getItemIds().size());
    }

    @Override
    public void onDisable() {
        // Save tat ca player data dong bo
        for (org.bukkit.entity.Player p : getServer().getOnlinePlayers()) {
            var state  = itemManager.getState(p);
            var allExp = state.getAllExp();
            if (!allExp.isEmpty()) itemDatabase.saveSync(p.getUniqueId(), allExp);
        }
        if (itemDatabase != null) itemDatabase.close();
        getLogger().info("ElysiumItem disabled.");
    }

    public static ElysiumItem getInstance() { return instance; }
    public ItemDatabase       getItemDatabase() { return itemDatabase; }
    public ItemManager        getItemManager()  { return itemManager; }
    public ItemMastery        getItemMastery()     { return itemMastery; }
    public AccessoryManager   getAccessoryManager() { return accessoryManager; }
}
