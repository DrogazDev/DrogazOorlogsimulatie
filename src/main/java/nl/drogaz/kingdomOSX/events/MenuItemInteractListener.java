package nl.drogaz.kingdomOSX.events;

import nl.drogaz.kingdomOSX.Main;
import nl.drogaz.kingdomOSX.commands.admin.Moderator;
import nl.drogaz.kingdomOSX.party.PartyGui;
import nl.drogaz.kingdomOSX.queue.PlayGui;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class MenuItemInteractListener implements Listener {

    private final Main plugin;
    private final Moderator moderator;
    private final PartyGui partyGui;
    private final PlayGui playGui;

    public MenuItemInteractListener(Main plugin, Moderator moderator, PartyGui partyGui, PlayGui playGui) {
        this.plugin = plugin;
        this.moderator = moderator;
        this.partyGui = partyGui;
        this.playGui = playGui;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "menu_item");
        if (!pdc.has(key, PersistentDataType.STRING)) return;

        event.setCancelled(true);

        switch (pdc.get(key, PersistentDataType.STRING)) {
            case "play"      -> playGui.open(player);
            case "party"     -> partyGui.open(player);
            case "cosmetics" -> moderator.openCosmetics(player);
        }
    }
}
