package nl.drogaz.kingdomOSX.events;

import nl.drogaz.kingdomOSX.cosmetics.CosmeticsManager;
import nl.drogaz.kingdomOSX.party.PartyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final CosmeticsManager cosmeticsManager;
    private final PartyManager partyManager;

    public PlayerQuitListener(CosmeticsManager cosmeticsManager, PartyManager partyManager) {
        this.cosmeticsManager = cosmeticsManager;
        this.partyManager = partyManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        cosmeticsManager.unloadPlayer(player.getUniqueId());
        partyManager.leaveParty(player); // handelt ook owner-transfer en auto-disband af
    }
}
