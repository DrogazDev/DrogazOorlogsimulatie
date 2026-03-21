package nl.drogaz.kingdomOSX.party;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import nl.drogaz.kingdomOSX.Main;
import nl.drogaz.kingdomOSX.miscellaneous.StyledComponent;
import nl.drogaz.kingdomOSX.queue.QueueManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PartyManager {

    private final Main plugin;
    private final Map<UUID, Party> parties = new HashMap<>();          // partyId → Party
    private final Map<UUID, UUID> playerToParty = new HashMap<>();    // playerUuid → partyId
    private final Map<UUID, UUID> pendingInvites = new HashMap<>();   // invitedUuid → partyId
    private BukkitTask actionBarTask;
    private QueueManager queueManager; // setter-injection om circular constructie te vermijden

    public void setQueueManager(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    public PartyManager(Main plugin) {
        this.plugin = plugin;
        startActionBarTask();
    }

    // ── Action Bar ──────────────────────────────────────────────────────────────

    private void startActionBarTask() {
        actionBarTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Party party : parties.values()) {
                var bar = MiniMessage.miniMessage()
                        .deserialize("&fParty: " + party.getColor().miniMessage() + party.getName())
                        .decoration(TextDecoration.ITALIC, false);
                for (UUID uuid : party.getMembers()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) p.sendActionBar(bar);
                }
            }
        }, 0L, 40L);
    }

    // ── Party lifecycle ──────────────────────────────────────────────────────────

    public Party createParty(Player owner) {
        Party party = new Party(owner.getUniqueId(), owner.getName() + "'s Party");
        parties.put(party.getId(), party);
        playerToParty.put(owner.getUniqueId(), party.getId());
        return party;
    }

    public void disbandParty(UUID partyId) {
        Party party = parties.get(partyId);
        if (party == null) return;

        for (UUID uuid : new ArrayList<>(party.getMembers())) {
            playerToParty.remove(uuid);
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendMessage(StyledComponent.style("<red>De party is gedisband."));
                PartyGui.updatePartyItem(plugin, p, null);
            }
        }
        pendingInvites.entrySet().removeIf(e -> e.getValue().equals(partyId));
        if (queueManager != null) queueManager.removeFromQueue(partyId);
        parties.remove(partyId);
    }

    public void leaveParty(Player player) {
        pendingInvites.remove(player.getUniqueId()); // uitnodiging verwijderen als speler weglogt
        UUID partyId = playerToParty.remove(player.getUniqueId());
        if (partyId == null) return;

        Party party = parties.get(partyId);
        if (party == null) return;

        boolean wasOwner = party.isOwner(player.getUniqueId());
        party.removeMember(player.getUniqueId());

        if (party.isEmpty()) {
            if (queueManager != null) queueManager.removeFromQueue(partyId);
            parties.remove(partyId);
            return;
        }

        // Als party onder 2 leden zakt, uit de queue halen
        if (party.getMembers().size() < 2 && queueManager != null) {
            queueManager.removeFromQueue(partyId);
        }

        // Eigenaarschap overdragen als de owner weggaat
        if (wasOwner) {
            UUID newOwner = party.getMembers().get(0);
            party.setOwner(newOwner);
            Player newOwnerPlayer = Bukkit.getPlayer(newOwner);
            if (newOwnerPlayer != null) {
                newOwnerPlayer.sendMessage(StyledComponent.style("<gold>Je bent nu de party owner!"));
                PartyGui.updatePartyItem(plugin, newOwnerPlayer, party);
            }
        }

        // Resterende leden informeren
        for (UUID uuid : party.getMembers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendMessage(StyledComponent.style("<yellow>" + player.getName() + " <gray>heeft de party verlaten."));
            }
        }
    }

    // ── Member beheer ────────────────────────────────────────────────────────────

    public void kickMember(Party party, UUID targetUuid) {
        party.removeMember(targetUuid);
        playerToParty.remove(targetUuid);

        Player target = Bukkit.getPlayer(targetUuid);
        if (target != null) {
            target.sendMessage(StyledComponent.style("<red>Je bent uit de party gezet."));
            PartyGui.updatePartyItem(plugin, target, null);
        }

        String targetName = target != null ? target.getName() : targetUuid.toString();
        for (UUID uuid : party.getMembers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(StyledComponent.style("<yellow>" + targetName + " <gray>is uit de party gezet."));
        }

        if (party.isEmpty()) {
            if (queueManager != null) queueManager.removeFromQueue(party.getId());
            parties.remove(party.getId());
        } else if (party.getMembers().size() < 2 && queueManager != null) {
            queueManager.removeFromQueue(party.getId());
        }
    }

    public void promoteOwner(Party party, UUID newOwnerUuid) {
        UUID oldOwner = party.getOwner();
        party.setOwner(newOwnerUuid);

        Player newOwnerPlayer = Bukkit.getPlayer(newOwnerUuid);
        Player oldOwnerPlayer = Bukkit.getPlayer(oldOwner);

        if (newOwnerPlayer != null) {
            newOwnerPlayer.sendMessage(StyledComponent.style("<gold>Je bent gepromoot tot party owner!"));
            PartyGui.updatePartyItem(plugin, newOwnerPlayer, party);
        }
        if (oldOwnerPlayer != null) {
            oldOwnerPlayer.sendMessage(StyledComponent.style("<yellow>Je bent niet langer de party owner."));
            PartyGui.updatePartyItem(plugin, oldOwnerPlayer, party);
        }

        String newOwnerName = newOwnerPlayer != null ? newOwnerPlayer.getName() : newOwnerUuid.toString();
        for (UUID uuid : party.getMembers()) {
            if (!uuid.equals(newOwnerUuid) && !uuid.equals(oldOwner)) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) p.sendMessage(StyledComponent.style("<yellow>" + newOwnerName + " <gray>is de nieuwe party owner."));
            }
        }
    }

    // ── Uitnodigingen ─────────────────────────────────────────────────────────────

    public void invitePlayer(Party party, Player owner, Player target) {
        if (playerToParty.containsKey(target.getUniqueId())) {
            owner.sendMessage(StyledComponent.style("<red>" + target.getName() + " zit al in een party."));
            return;
        }
        if (pendingInvites.containsKey(target.getUniqueId())) {
            owner.sendMessage(StyledComponent.style("<red>" + target.getName() + " heeft al een openstaande uitnodiging."));
            return;
        }

        pendingInvites.put(target.getUniqueId(), party.getId());
        owner.sendMessage(StyledComponent.style("<green>Uitnodiging gestuurd naar <white>" + target.getName() + "."));

        Component acceptBtn = Component.text(" [ACCEPTEER] ", NamedTextColor.GREEN, TextDecoration.BOLD)
                .hoverEvent(HoverEvent.showText(Component.text("Klik om de uitnodiging te accepteren")))
                .clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player p)
                        Bukkit.getScheduler().runTask(plugin, () -> acceptInvite(p));
                }));

        Component denyBtn = Component.text("[WEIGER]", NamedTextColor.RED, TextDecoration.BOLD)
                .hoverEvent(HoverEvent.showText(Component.text("Klik om de uitnodiging te weigeren")))
                .clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player p)
                        Bukkit.getScheduler().runTask(plugin, () -> declineInvite(p));
                }));

        target.sendMessage(
                MiniMessage.miniMessage()
                        .deserialize("<gold>" + owner.getName() + " <gray>nodigt je uit voor party: "
                                + party.getColor().miniMessage() + party.getName())
                        .append(acceptBtn)
                        .append(denyBtn)
        );
    }

    public void acceptInvite(Player player) {
        UUID partyId = pendingInvites.remove(player.getUniqueId());
        if (partyId == null) {
            player.sendMessage(StyledComponent.style("<red>Je hebt geen openstaande uitnodiging."));
            return;
        }
        Party party = parties.get(partyId);
        if (party == null) {
            player.sendMessage(StyledComponent.style("<red>De party bestaat niet meer."));
            return;
        }

        party.addMember(player.getUniqueId());
        playerToParty.put(player.getUniqueId(), partyId);
        PartyGui.updatePartyItem(plugin, player, party);

        player.sendMessage(StyledComponent.style("<green>Je hebt de party gejoined!"));
        for (UUID uuid : party.getMembers()) {
            if (!uuid.equals(player.getUniqueId())) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) p.sendMessage(StyledComponent.style("<yellow>" + player.getName() + " <gray>heeft de party gejoined."));
            }
        }
    }

    public void declineInvite(Player player) {
        UUID partyId = pendingInvites.remove(player.getUniqueId());
        if (partyId == null) {
            player.sendMessage(StyledComponent.style("<red>Je hebt geen openstaande uitnodiging."));
            return;
        }
        player.sendMessage(StyledComponent.style("<yellow>Uitnodiging geweigerd."));
        Party party = parties.get(partyId);
        if (party != null) {
            Player owner = Bukkit.getPlayer(party.getOwner());
            if (owner != null) {
                owner.sendMessage(StyledComponent.style("<yellow>" + player.getName() + " <gray>heeft de uitnodiging geweigerd."));
            }
        }
    }

    // ── Queries ──────────────────────────────────────────────────────────────────

    public Party getPartyByPlayer(UUID playerUuid) {
        UUID partyId = playerToParty.get(playerUuid);
        return partyId != null ? parties.get(partyId) : null;
    }

    public Party getPartyById(UUID partyId) {
        return parties.get(partyId);
    }

    public Collection<Party> getAllParties() {
        return parties.values();
    }

    // ── Publieke party joinen (vanuit queue) ──────────────────────────────────────

    public void joinPublicParty(Player player, UUID partyId) {
        Party party = parties.get(partyId);
        if (party == null) {
            player.sendMessage(StyledComponent.style("<red>Deze party bestaat niet meer."));
            return;
        }
        if (playerToParty.containsKey(player.getUniqueId())) {
            player.sendMessage(StyledComponent.style("<red>Je zit al in een party."));
            return;
        }

        party.addMember(player.getUniqueId());
        playerToParty.put(player.getUniqueId(), partyId);
        PartyGui.updatePartyItem(plugin, player, party);

        player.sendMessage(StyledComponent.style("<green>Je hebt de party gejoined!"));
        for (UUID uuid : party.getMembers()) {
            if (!uuid.equals(player.getUniqueId())) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) p.sendMessage(StyledComponent.style(
                        "<yellow>" + player.getName() + " <gray>heeft de party gejoined."));
            }
        }
    }

    // ── Shutdown ─────────────────────────────────────────────────────────────────

    public void shutdown() {
        if (actionBarTask != null) actionBarTask.cancel();
    }
}
