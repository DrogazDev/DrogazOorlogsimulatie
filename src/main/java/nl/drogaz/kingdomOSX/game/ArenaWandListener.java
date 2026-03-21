package nl.drogaz.kingdomOSX.game;

import nl.drogaz.kingdomOSX.Main;
import nl.drogaz.kingdomOSX.miscellaneous.StyledComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class ArenaWandListener implements Listener {

    private final Main plugin;
    private final GameManager gameManager;

    public ArenaWandListener(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        NamespacedKey wandKey = new NamespacedKey(plugin, "arena_wand");
        if (!item.getItemMeta().getPersistentDataContainer().has(wandKey, PersistentDataType.BOOLEAN)) return;

        // Altijd cancellen zodat er geen blok geplaatst/kapot wordt
        event.setCancelled(true);

        Player player = event.getPlayer();
        Arena wizard = gameManager.getWizard(player.getUniqueId());

        if (wizard == null) {
            player.sendMessage(StyledComponent.style(
                    "<red>Geen actieve wizard. Gebruik <white>/arena create <naam></white> of <white>/arena edit <naam></white>."));
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) return;

        LocationData loc = LocationData.from(block.getLocation());

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            wizard.setPos1(loc);
            player.sendMessage(StyledComponent.style(
                    "<gold>[Arena Wand] <green>Pos1 ingesteld op: <white>" + loc.toBlockString()));
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            wizard.setPos2(loc);
            player.sendMessage(StyledComponent.style(
                    "<gold>[Arena Wand] <green>Pos2 ingesteld op: <white>" + loc.toBlockString()));
        }
    }
}
