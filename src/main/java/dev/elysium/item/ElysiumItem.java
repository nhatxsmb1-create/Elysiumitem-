package dev.elysium.item;

import dev.elysium.item.command.AdminCommand;
import dev.elysium.item.command.ItemCommand;
import dev.elysium.item.item.ItemManager;
import dev.elysium.item.accessory.AccessoryManager;
import dev.elysium.item.listener.ArmorListener;
import dev.elysium.item.mechanic.MechanicEngine;
import dev.elysium.item.mechanic.MarkManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ElysiumItem extends JavaPlugin implements Listener {

    private static ElysiumItem instance;

    private ItemManager  itemManager;
    private AccessoryManager  accessoryManager;
    private MechanicEngine mechanicEngine;
    private MarkManager markManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResource("items.yml",    false);
        saveResource("armors.yml",   false);
        saveResource("config.yml",   false);

        // Init
        accessoryManager  = new AccessoryManager(this);
        itemManager  = new ItemManager(this);
        mechanicEngine = new MechanicEngine(this);
        markManager = new MarkManager(this);

        // Commands
        getCommand("eitem").setExecutor(new ItemCommand(this));
        getCommand("trangbi").setExecutor(new dev.elysium.item.command.TrangBiCommand(this));
        getCommand("eitemadmin").setExecutor(new AdminCommand(this));

        // Listeners
        getServer().getPluginManager().registerEvents(new ArmorListener(this),   this);
        getServer().getPluginManager().registerEvents(new dev.elysium.item.mechanic.MechanicListener(this), this);
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("=== ElysiumItem v" + getDescription().getVersion() + " enabled! ===");
        getLogger().info("Items loaded: " + itemManager.getItemIds().size());
    }

    @Override
    public void onDisable() {
        if (accessoryManager != null) {
            accessoryManager.saveAll();
        }
        getLogger().info("ElysiumItem disabled.");
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (accessoryManager != null) {
            accessoryManager.removeSlots(e.getPlayer().getUniqueId());
        }
    }

    public static ElysiumItem getInstance() { return instance; }
    public ItemManager        getItemManager()  { return itemManager; }
    public AccessoryManager   getAccessoryManager() { return accessoryManager; }
    public MechanicEngine     getMechanicEngine() { return mechanicEngine; }
    public MarkManager        getMarkManager()    { return markManager; }
}