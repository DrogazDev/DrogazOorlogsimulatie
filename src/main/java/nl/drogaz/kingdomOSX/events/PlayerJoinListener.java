package nl.drogaz.kingdomOSX.events;

import nl.drogaz.kingdomOSX.Main;
import nl.drogaz.kingdomOSX.cosmetics.CosmeticsManager;
import nl.drogaz.kingdomOSX.miscellaneous.StyledComponent;
import nl.drogaz.kingdomOSX.party.PartyGui;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class PlayerJoinListener implements Listener {

    private final Main plugin;
    private final CosmeticsManager cosmeticsManager;

    public PlayerJoinListener(Main plugin, CosmeticsManager cosmeticsManager) {
        this.plugin = plugin;
        this.cosmeticsManager = cosmeticsManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        cosmeticsManager.loadPlayer(player.getUniqueId());
        giveMenuItems(player);
    }

    private void giveMenuItems(Player player) {
        player.getInventory().clear();

        // Slot 0 — Play
        ItemStack play = new ItemStack(Material.COMPASS);
        ItemMeta playMeta = play.getItemMeta();
        playMeta.displayName(StyledComponent.style("<green>Spelen"));
        playMeta.lore(List.of(
                StyledComponent.style(""),
                StyledComponent.style("<gray>Klik om een game te starten of te joinen.")
        ));
        playMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "menu_item"),
                PersistentDataType.STRING,
                "play"
        );
        play.setItemMeta(playMeta);
        player.getInventory().setItem(0, play);

        // Slot 4 — Party (geen party bij join → "Maak Party aan")
        PartyGui.updatePartyItem(plugin, player, null);

        // Slot 6 — Cosmetics
        ItemStack cosmetics = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta cosmeticsMeta = cosmetics.getItemMeta();
        cosmeticsMeta.displayName(StyledComponent.style("<aqua>Cosmetics"));
        cosmeticsMeta.lore(List.of(
                StyledComponent.style(""),
                StyledComponent.style("<yellow>Klik om je cosmetics aan te passen")
        ));
        cosmeticsMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "menu_item"),
                PersistentDataType.STRING,
                "cosmetics"
        );
        cosmetics.setItemMeta(cosmeticsMeta);
        player.getInventory().setItem(6, cosmetics);
    }
}
