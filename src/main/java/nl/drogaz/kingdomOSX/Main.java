package nl.drogaz.kingdomOSX;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import nl.drogaz.kingdomOSX.commands.admin.ArenaCommand;
import nl.drogaz.kingdomOSX.commands.admin.Moderator;
import nl.drogaz.kingdomOSX.cosmetics.CosmeticsManager;
import nl.drogaz.kingdomOSX.database.DatabaseManager;
import nl.drogaz.kingdomOSX.events.MenuItemInteractListener;
import nl.drogaz.kingdomOSX.events.PlayerJoinListener;
import nl.drogaz.kingdomOSX.events.PlayerQuitListener;
import nl.drogaz.kingdomOSX.game.ArenaWandListener;
import nl.drogaz.kingdomOSX.game.GameManager;
import nl.drogaz.kingdomOSX.miscellaneous.InputManager;
import nl.drogaz.kingdomOSX.party.PartyGui;
import nl.drogaz.kingdomOSX.party.PartyManager;
import nl.drogaz.kingdomOSX.queue.PlayGui;
import nl.drogaz.kingdomOSX.queue.QueueManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class Main extends JavaPlugin {

    @Getter private PaperCommandManager commandManager;

    private DatabaseManager databaseManager;
    private PartyManager partyManager;

    @Override
    public void onEnable() {
        // Database
        databaseManager = new DatabaseManager();
        try {
            databaseManager.initialize(this);
        } catch (SQLException e) {
            getLogger().severe("Kon de database niet initialiseren: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Managers
        CosmeticsManager cosmeticsManager = new CosmeticsManager(databaseManager);
        partyManager = new PartyManager(this);
        InputManager inputManager = new InputManager(this);
        QueueManager queueManager = new QueueManager(this, partyManager);
        GameManager gameManager = new GameManager(this);

        partyManager.setQueueManager(queueManager);

        // GUI's & commando's
        Moderator moderator = new Moderator(cosmeticsManager);
        PartyGui partyGui = new PartyGui(this, partyManager, inputManager);
        PlayGui playGui = new PlayGui(partyManager, queueManager);

        commandManager = new PaperCommandManager(this);

        // Tab-completion voor arena keys
        commandManager.getCommandCompletions().registerCompletion("arenas",
                c -> gameManager.getArenas().keySet());

        commandManager.registerCommand(moderator);
        commandManager.registerCommand(new ArenaCommand(this, gameManager));

        // Events
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, cosmeticsManager), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(cosmeticsManager, partyManager), this);
        getServer().getPluginManager().registerEvents(new MenuItemInteractListener(this, moderator, partyGui, playGui), this);
        getServer().getPluginManager().registerEvents(inputManager, this);
        getServer().getPluginManager().registerEvents(new ArenaWandListener(this, gameManager), this);
    }

    @Override
    public void onDisable() {
        if (partyManager != null) partyManager.shutdown();
        if (databaseManager != null) databaseManager.close();
    }
}
