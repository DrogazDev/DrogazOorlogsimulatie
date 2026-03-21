package nl.drogaz.kingdomOSX.queue;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import nl.drogaz.kingdomOSX.miscellaneous.StyledComponent;
import nl.drogaz.kingdomOSX.party.Party;
import nl.drogaz.kingdomOSX.party.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayGui {

    private final PartyManager partyManager;
    private final QueueManager queueManager;

    private static final GuiItem GLASS = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE)
            .name(Component.text(" ")).asGuiItem();

    public PlayGui(PartyManager partyManager, QueueManager queueManager) {
        this.partyManager = partyManager;
        this.queueManager = queueManager;
    }

    // ── Entrypoint ────────────────────────────────────────────────────────────────

    public void open(Player player) {
        Party party = partyManager.getPartyByPlayer(player.getUniqueId());

        if (party == null) {
            openNoPartyGui(player);
            return;
        }
        if (!party.isOwner(player.getUniqueId())) {
            openMemberWaitGui(player, party);
            return;
        }
        if (party.getMembers().size() < 2) {
            openNotEnoughMembersGui(player);
            return;
        }
        openOwnerPlayGui(player, party);
    }

    // ── Geen party: beschikbare parties joinen ────────────────────────────────────
    //
    // Layout (4 rijen, fillBorder → interior: 10-16, 19-25 = 14 slots, pageSize=14)

    private void openNoPartyGui(Player player) {
        PaginatedGui gui = Gui.paginated()
                .title(Component.text("Beschikbare Parties"))
                .rows(4)
                .pageSize(14)
                .disableAllInteractions()
                .create();

        gui.getFiller().fillBorder(GLASS);

        boolean anyAdded = false;
        for (UUID partyId : queueManager.getQueuedParties()) {
            Party party = partyManager.getPartyById(partyId);
            if (party == null) continue;

            String rawOwner = Bukkit.getOfflinePlayer(party.getOwner()).getName();
            String ownerName = rawOwner != null ? rawOwner : "Onbekend";
            final UUID finalId = partyId;

            gui.addItem(ItemBuilder.from(Material.NETHER_STAR)
                    .name(StyledComponent.style(party.getColor().miniMessage() + party.getName()))
                    .lore(
                            StyledComponent.style(""),
                            StyledComponent.style("<gray>Owner: <white>" + ownerName),
                            StyledComponent.style("<gray>Leden: <white>" + party.getMembers().size()),
                            StyledComponent.style(""),
                            StyledComponent.style("<yellow>Klik om te joinen.")
                    )
                    .asGuiItem(e -> {
                        partyManager.joinPublicParty(player, finalId);
                        player.closeInventory();
                    }));
            anyAdded = true;
        }

        if (!anyAdded) {
            gui.setItem(13, ItemBuilder.from(Material.BARRIER)
                    .name(StyledComponent.style("<red>Geen beschikbare parties"))
                    .lore(
                            StyledComponent.style(""),
                            StyledComponent.style("<gray>Er zijn momenteel geen open parties.")
                    )
                    .asGuiItem());
        }

        gui.setItem(30, ItemBuilder.from(Material.ARROW).name(StyledComponent.style("<gold>Vorige")).asGuiItem(e -> gui.previous()));
        gui.setItem(32, ItemBuilder.from(Material.ARROW).name(StyledComponent.style("<gold>Volgende")).asGuiItem(e -> gui.next()));

        gui.open(player);
    }

    // ── Niet genoeg leden ─────────────────────────────────────────────────────────

    private void openNotEnoughMembersGui(Player player) {
        Gui gui = Gui.gui()
                .title(Component.text("Spelen"))
                .rows(1)
                .disableAllInteractions()
                .create();

        gui.getFiller().fill(GLASS);
        gui.setItem(4, ItemBuilder.from(Material.BARRIER)
                .name(StyledComponent.style("<red>Niet genoeg party leden om een game te starten"))
                .lore(
                        StyledComponent.style(""),
                        StyledComponent.style("<gray>Je hebt minimaal 2 spelers nodig in je party.")
                )
                .asGuiItem());

        gui.open(player);
    }

    // ── Niet-owner lid ────────────────────────────────────────────────────────────

    private void openMemberWaitGui(Player player, Party party) {
        Gui gui = Gui.gui()
                .title(Component.text("Spelen"))
                .rows(1)
                .disableAllInteractions()
                .create();

        gui.getFiller().fill(GLASS);
        gui.setItem(4, ItemBuilder.from(Material.CLOCK)
                .name(StyledComponent.style("<yellow>Wacht op de party owner"))
                .lore(
                        StyledComponent.style(""),
                        StyledComponent.style("<gray>Party: " + party.getColor().miniMessage() + party.getName()),
                        StyledComponent.style("<gray>Leden: <white>" + party.getMembers().size()),
                        StyledComponent.style(""),
                        StyledComponent.style("<gray>De owner moet een game starten.")
                )
                .asGuiItem());

        gui.open(player);
    }

    // ── Owner: kies game modus ────────────────────────────────────────────────────
    //
    // Layout (3 rijen):
    //   Rij 1: rand
    //   Rij 2: ═ ═ Party-Split(11) ═ Party-vs-Party(13) ═ Queue-Toggle(15) ═ ═
    //   Rij 3: rand

    private void openOwnerPlayGui(Player player, Party party) {
        Gui gui = Gui.gui()
                .title(Component.text("Spelen"))
                .rows(3)
                .disableAllInteractions()
                .create();

        gui.getFiller().fill(GLASS);

        // Party Split
        gui.setItem(11, ItemBuilder.from(Material.DIAMOND_SWORD)
                .name(StyledComponent.style("<aqua>Party Split"))
                .lore(
                        StyledComponent.style(""),
                        StyledComponent.style("<gray>Splits de party willekeurig in twee teams."),
                        StyledComponent.style("<gray>Leden: <white>" + party.getMembers().size())
                )
                .asGuiItem(e -> openMapSelectGui(player, party,
                        () -> openOwnerPlayGui(player, party),
                        map -> {
                            queueManager.startPartySplit(party, map);
                            player.closeInventory();
                        })));

        // Party vs Party
        gui.setItem(13, ItemBuilder.from(Material.GOLDEN_SWORD)
                .name(StyledComponent.style("<gold>Party vs Party"))
                .lore(
                        StyledComponent.style(""),
                        StyledComponent.style("<gray>Daag een andere party uit voor een gevecht.")
                )
                .asGuiItem(e -> openPartyVsPartyGui(player, party)));

        // Queue toggle
        boolean inQueue = queueManager.isInQueue(party.getId());
        gui.setItem(15, ItemBuilder.from(inQueue ? Material.RED_DYE : Material.LIME_DYE)
                .name(inQueue
                        ? StyledComponent.style("<red>Uit queue halen")
                        : StyledComponent.style("<green>In queue zetten"))
                .lore(
                        StyledComponent.style(""),
                        inQueue
                                ? StyledComponent.style("<gray>Verbergt je party voor solo spelers.")
                                : StyledComponent.style("<gray>Maakt je party zichtbaar voor solo spelers.")
                )
                .asGuiItem(e -> {
                    if (inQueue) {
                        queueManager.removeFromQueue(party.getId());
                        player.sendMessage(StyledComponent.style("<yellow>Party uit de queue gehaald."));
                    } else {
                        queueManager.addToQueue(party.getId());
                        player.sendMessage(StyledComponent.style("<green>Party in de queue gezet! Solo spelers kunnen nu joinen."));
                    }
                    openOwnerPlayGui(player, party); // refresh
                }));

        gui.open(player);
    }

    // ── Map selectie ──────────────────────────────────────────────────────────────
    //
    // Layout (4 rijen, fillBorder → interior: 10-16, 19-25 = 14 slots, pageSize=14)

    private void openMapSelectGui(Player player, Party party, Runnable backAction, Consumer<GameMap> onSelect) {
        PaginatedGui gui = Gui.paginated()
                .title(Component.text("Kies een map"))
                .rows(4)
                .pageSize(14)
                .disableAllInteractions()
                .create();

        gui.getFiller().fillBorder(GLASS);

        for (GameMap map : GameMap.values()) {
            gui.addItem(ItemBuilder.from(map.getMaterial())
                    .name(StyledComponent.style("<white>" + map.getDisplayName()))
                    .lore(
                            StyledComponent.style(""),
                            StyledComponent.style("<gray>" + map.getDescription()),
                            StyledComponent.style(""),
                            StyledComponent.style("<yellow>Klik om te selecteren.")
                    )
                    .asGuiItem(e -> onSelect.accept(map)));
        }

        gui.setItem(30, ItemBuilder.from(Material.ARROW).name(StyledComponent.style("<gold>Vorige")).asGuiItem(e -> gui.previous()));
        gui.setItem(32, ItemBuilder.from(Material.ARROW).name(StyledComponent.style("<gold>Volgende")).asGuiItem(e -> gui.next()));
        gui.setItem(31, ItemBuilder.from(Material.BARRIER).name(StyledComponent.style("<red>Terug")).asGuiItem(e -> backAction.run()));

        gui.open(player);
    }

    // ── Party vs Party: kies tegenstander ────────────────────────────────────────
    //
    // Layout (5 rijen, fillBorder → interior: 10-16, 19-25, 28-34 = 21 slots, pageSize=21)

    private void openPartyVsPartyGui(Player player, Party party) {
        PaginatedGui gui = Gui.paginated()
                .title(Component.text("Party vs Party"))
                .rows(5)
                .pageSize(21)
                .disableAllInteractions()
                .create();

        gui.getFiller().fillBorder(GLASS);

        List<Party> available = partyManager.getAllParties().stream()
                .filter(p -> !p.getId().equals(party.getId()))
                .filter(p -> p.getMembers().size() >= 2)
                .toList();

        if (available.isEmpty()) {
            gui.setItem(22, ItemBuilder.from(Material.BARRIER)
                    .name(StyledComponent.style("<red>Geen beschikbare parties"))
                    .lore(
                            StyledComponent.style(""),
                            StyledComponent.style("<gray>Er zijn geen andere parties met 2+ leden.")
                    )
                    .asGuiItem());
        } else {
            for (Party target : available) {
                String rawOwner = Bukkit.getOfflinePlayer(target.getOwner()).getName();
                String ownerName = rawOwner != null ? rawOwner : "Onbekend";
                final UUID targetId = target.getId();
                boolean hasPending = queueManager.hasPendingRequest(targetId);

                gui.addItem(ItemBuilder.from(Material.NETHER_STAR)
                        .name(StyledComponent.style(target.getColor().miniMessage() + target.getName()))
                        .lore(
                                StyledComponent.style(""),
                                StyledComponent.style("<gray>Owner: <white>" + ownerName),
                                StyledComponent.style("<gray>Leden: <white>" + target.getMembers().size()),
                                StyledComponent.style(""),
                                hasPending
                                        ? StyledComponent.style("<yellow>⚔ Verzoek al verstuurd.")
                                        : StyledComponent.style("<yellow>Klik om uit te dagen.")
                        )
                        .asGuiItem(e -> {
                            if (hasPending) return;
                            openMapSelectGui(player, party,
                                    () -> openPartyVsPartyGui(player, party),
                                    map -> {
                                        queueManager.sendPvpRequest(party, targetId, map);
                                        player.sendMessage(StyledComponent.style(
                                                "<green>PvP-verzoek verstuurd naar <white>" + ownerName + "<green>!"));
                                        player.closeInventory();
                                    });
                        }));
            }
        }

        gui.setItem(39, ItemBuilder.from(Material.ARROW).name(StyledComponent.style("<gold>Vorige")).asGuiItem(e -> gui.previous()));
        gui.setItem(41, ItemBuilder.from(Material.ARROW).name(StyledComponent.style("<gold>Volgende")).asGuiItem(e -> gui.next()));
        gui.setItem(40, ItemBuilder.from(Material.BARRIER).name(StyledComponent.style("<red>Terug")).asGuiItem(e -> openOwnerPlayGui(player, party)));

        gui.open(player);
    }
}
