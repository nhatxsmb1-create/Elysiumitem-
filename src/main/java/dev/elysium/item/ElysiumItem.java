package dev.elysium.item;

import dev.elysium.item.command.AdminCommand;
import dev.elysium.item.command.ItemCommand;
import dev.elysium.item.gui.GuiListener;
import dev.elysium.item.item.ItemManager;
import dev.elysium.item.accessory.AccessoryManager;
import dev.elysium.item.listener.ArmorListener;
import dev.elysium.item.mechanic.MechanicEngine;
import org.bukkit.plugin.java.JavaPlugin;

public class ElysiumItem extends JavaPlugin {

    private static ElysiumItem instance;

    private ItemManager  itemManager;
    private AccessoryManager  accessoryManager;
    private MechanicEngine mechanicEngine;

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

        // Commands
        getCommand("eitem").setExecutor(new ItemCommand(this));
        getCommand("trangbi").setExecutor(new dev.elysium.item.command.TrangBiCommand(this));
        getCommand("eitemadmin").setExecutor(new AdminCommand(this));

        // Listeners
        getServer().getPluginManager().registerEvents(new ArmorListener(this),   this);
        getServer().getPluginManager().registerEvents(new GuiListener(),          this);
        getServer().getPluginManager().registerEvents(new dev.elysium.item.mechanic.MechanicListener(this), this);

        getLogger().info("=== ElysiumItem v" + getDescription().getVersion() + " enabled! ===");
        getLogger().info("Items loaded: " + itemManager.getItemIds().size());
    }

    @Override
    public void onDisable() {
        getLogger().info("ElysiumItem disabled.");
    }

    public static ElysiumItem getInstance() { return instance; }
    public ItemManager        getItemManager()  { return itemManager; }
    public AccessoryManager   getAccessoryManager() { return accessoryManager; }
    public MechanicEngine     getMechanicEngine() { return mechanicEngine; }
}