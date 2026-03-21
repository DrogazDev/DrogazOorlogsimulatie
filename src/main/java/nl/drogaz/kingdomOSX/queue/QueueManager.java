package nl.drogaz.kingdomOSX.queue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import nl.drogaz.kingdomOSX.Main;
import nl.drogaz.kingdomOSX.miscellaneous.StyledComponent;
import nl.drogaz.kingdomOSX.party.Party;
import nl.drogaz.kingdomOSX.party.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class QueueManager {

    private final Main plugin;
    private final PartyManager partyManager;

    /** Party-IDs die publiek in de queue staan (solo spelers kunnen joinen). */
    private final Set<UUID> queuedParties = new HashSet<>();

    /** Openstaande PvP-verzoeken: targetPartyId → request. */
    private final Map<UUID, PvpRequest> pvpRequests = new HashMap<>();

    public QueueManager(Main plugin, PartyManager partyManager) {
        this.plugin = plugin;
        this.partyManager = partyManager;
    }

    // ── Publieke queue ────────────────────────────────────────────────────────────

    public void addToQueue(UUID partyId) {
        queuedParties.add(partyId);
    }

    public void removeFromQueue(UUID partyId) {
        queuedParties.remove(partyId);
        pvpRequests.entrySet().removeIf(e ->
                e.getKey().equals(partyId) || e.getValue().requesterPartyId().equals(partyId));
    }

    public boolean isInQueue(UUID partyId) {
        return queuedParties.contains(partyId);
    }

    public Set<UUID> getQueuedParties() {
        return Collections.unmodifiableSet(queuedParties);
    }

    // ── Party Split ───────────────────────────────────────────────────────────────

    public void startPartySplit(Party party, GameMap map) {
        removeFromQueue(party.getId());
        // Placeholder – hier wordt later de echte game-logica aangeroepen.
        for (UUID uuid : party.getMembers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(StyledComponent.style(
                    "<green>Party Split gestart op map: <white>" + map.getDisplayName() + "<green>! (binnenkort beschikbaar)"));
        }
    }

    // ── Party vs Party ────────────────────────────────────────────────────────────

    public void sendPvpRequest(Party requester, UUID targetPartyId, GameMap map) {
        if (pvpRequests.containsKey(targetPartyId)) return; // al een openstaand verzoek

        pvpRequests.put(targetPartyId, new PvpRequest(requester.getId(), targetPartyId, map));

        Party target = partyManager.getPartyById(targetPartyId);
        if (target == null) { pvpRequests.remove(targetPartyId); return; }

        Player targetOwner = Bukkit.getPlayer(target.getOwner());
        if (targetOwner == null) { pvpRequests.remove(targetPartyId); return; }

        Component acceptBtn = Component.text(" [ACCEPTEER] ", NamedTextColor.GREEN, TextDecoration.BOLD)
                .hoverEvent(HoverEvent.showText(Component.text("Accepteer het PvP-verzoek")))
                .clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player p)
                        Bukkit.getScheduler().runTask(plugin, () -> acceptPvpRequest(targetPartyId));
                }));

        Component denyBtn = Component.text("[WEIGER]", NamedTextColor.RED, TextDecoration.BOLD)
                .hoverEvent(HoverEvent.showText(Component.text("Weiger het PvP-verzoek")))
                .clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player p)
                        Bukkit.getScheduler().runTask(plugin, () -> declinePvpRequest(targetPartyId, p));
                }));

        targetOwner.sendMessage(
                MiniMessage.miniMessage()
                        .deserialize(requester.getColor().miniMessage() + requester.getName()
                                + " <gray>daagt <white>" + target.getColor().miniMessage() + target.getName()
                                + " <gray>uit op map: <white>" + map.getDisplayName() + "<gray>!")
                        .append(acceptBtn)
                        .append(denyBtn)
        );
    }

    public void acceptPvpRequest(UUID targetPartyId) {
        PvpRequest req = pvpRequests.remove(targetPartyId);
        if (req == null) return;

        Party requester = partyManager.getPartyById(req.requesterPartyId());
        Party target    = partyManager.getPartyById(req.targetPartyId());
        if (requester == null || target == null) return;

        startPvpGame(requester, target, req.map());
    }

    public void declinePvpRequest(UUID targetPartyId, Player decliner) {
        PvpRequest req = pvpRequests.remove(targetPartyId);
        if (req == null) return;

        decliner.sendMessage(StyledComponent.style("<yellow>PvP-verzoek geweigerd."));

        Party requester = partyManager.getPartyById(req.requesterPartyId());
        if (requester != null) {
            Player requesterOwner = Bukkit.getPlayer(requester.getOwner());
            if (requesterOwner != null) {
                requesterOwner.sendMessage(StyledComponent.style(
                        "<yellow>" + decliner.getName() + "'s party <gray>heeft het PvP-verzoek geweigerd."));
            }
        }
    }

    public boolean hasPendingRequest(UUID targetPartyId) {
        return pvpRequests.containsKey(targetPartyId);
    }

    private void startPvpGame(Party party1, Party party2, GameMap map) {
        removeFromQueue(party1.getId());
        removeFromQueue(party2.getId());
        // Placeholder – hier wordt later de echte game-logica aangeroepen.
        String msg = "<green>Party vs Party gestart! <white>" + party1.getName()
                + " <gray>vs <white>" + party2.getName()
                + " <gray>op <white>" + map.getDisplayName() + "<gray>! (binnenkort beschikbaar)";
        for (UUID uuid : party1.getMembers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(StyledComponent.style(msg));
        }
        for (UUID uuid : party2.getMembers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(StyledComponent.style(msg));
        }
    }
}
