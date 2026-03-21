package nl.drogaz.kingdomOSX.party;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import nl.drogaz.kingdomOSX.Main;
import nl.drogaz.kingdomOSX.miscellaneous.InputManager;
import nl.drogaz.kingdomOSX.miscellaneous.StyledComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class PartyGui {

    private final Main plugin;
    private final PartyManager partyManager;
    private final InputManager inputManager;

    private static final GuiItem GLASS = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE)
            .name(Component.text(" ")).asGuiItem();

    public PartyGui(Main plugin, PartyManager partyManager, InputManager inputManager) {
        this.plugin = plugin;
        this.partyManager = partyManager;
        this.inputManager = inputManager;
    }

    // ── Entrypoint ───────────────────────────────────────────────────────────────

    public void open(Player player) {
        Party party = partyManager.getPartyByPlayer(player.getUniqueId());
        if (party == null) {
            Party newParty = partyManager.createParty(player);
            updatePartyItem(plugin, player, newParty);
            openManageGui(player, newParty);
        } else {
            openManageGui(player, party);
        }
    }

    // ── Party beheer ─────────────────────────────────────────────────────────────
    //
    // Layout (5 rijen, 45 slots):
    //   Rij 1 (0-8):   Rand
    //   Rij 2 (9-17):  Rand | Hernoem(10) | Kleur(11) | Glas(12-16) | Rand
    //   Rij 3 (18-26): Rand | Leden pagina (19-25)                   | Rand
    //   Rij 4 (27-35): Rand | Leden pagina (28-34)                   | Rand
    //   Rij 5 (36-44): Rand | ◀(38) | Rand | Vorige(39) | Actie(40) | Volgende(41) | Rand | ▶(42) | Rand
    //
    // pageSize = 14  (7 slots × 2 rijen voor leden)

    public void openManageGui(Player player, Party party) {
        PaginatedGui gui = Gui.paginated()
                .title(Component.text("Party Beheer"))
                .rows(5)
                .pageSize(14)
                .disableAllInteractions()
                .create();

        boolean isOwner = party.isOwner(player.getUniqueId());

        // Rand vullen
        gui.getFiller().fillBorder(GLASS);

        // Rij 2: hernoem + kleur + glas
        gui.setItem(10, renameItem(isOwner, party, e -> {
            if (!isOwner) return;
            player.closeInventory();
            player.sendMessage(StyledComponent.style("<yellow>Typ de nieuwe party naam in de chat:"));
            inputManager.awaitInput(player, input -> {
                if (input.isBlank()) {
                    player.sendMessage(StyledComponent.style("<red>De naam mag niet leeg zijn."));
                } else if (input.length() > 32) {
                    player.sendMessage(StyledComponent.style("<red>Naam mag maximaal 32 tekens zijn."));
                } else {
                    party.setName(input);
                    player.sendMessage(StyledComponent.style("<green>Party naam: <white>" + input));
                }
                openManageGui(player, party);
            });
        }));

        gui.setItem(11, colorItem(isOwner, party, e -> {
            if (isOwner) openColorGui(player, party);
        }));

        if (isOwner) {
            gui.setItem(12, ItemBuilder.from(Material.NAME_TAG)
                    .name(StyledComponent.style("<green>Speler Uitnodigen"))
                    .lore(
                            StyledComponent.style(""),
                            StyledComponent.style("<gray>Nodig een online speler uit voor je party.")
                    )
                    .asGuiItem(e -> {
                        player.closeInventory();
                        player.sendMessage(StyledComponent.style("<yellow>Typ de naam van de speler die je wilt uitnodigen:"));
                        inputManager.awaitInput(player, name -> {
                            Player target = Bukkit.getPlayerExact(name);
                            if (target == null) {
                                player.sendMessage(StyledComponent.style("<red>Speler <white>" + name + " <red>is niet online."));
                            } else if (target.equals(player)) {
                                player.sendMessage(StyledComponent.style("<red>Je kunt jezelf niet uitnodigen."));
                            } else {
                                partyManager.invitePlayer(party, player, target);
                            }
                            openManageGui(player, party);
                        });
                    }));
        } else {
            gui.setItem(12, GLASS);
        }

        for (int i = 13; i <= 16; i++) gui.setItem(i, GLASS);

        // Leden (vult slots 19-25 en 28-34 via addItem)
        for (UUID memberUuid : party.getMembers()) {
            String rawName = Bukkit.getOfflinePlayer(memberUuid).getName();
            String memberName = rawName != null ? rawName : memberUuid.toString();
            boolean isMemberOwner = party.isOwner(memberUuid);
            final UUID finalUuid = memberUuid;

            List<Component> lore = new ArrayList<>();
            lore.add(StyledComponent.style(""));
            lore.add(isMemberOwner
                    ? StyledComponent.style("<gold>Party Owner")
                    : StyledComponent.style("<gray>Party Lid"));
            if (isOwner && !isMemberOwner) {
                lore.add(StyledComponent.style(""));
                lore.add(StyledComponent.style("<yellow>Klik voor opties."));
            }

            gui.addItem(ItemBuilder.from(isMemberOwner ? Material.NETHER_STAR : Material.PAPER)
                    .name(StyledComponent.style((isMemberOwner ? "<gold>⭐ " : "<white>") + memberName))
                    .lore(lore)
                    .asGuiItem(e -> {
                        if (isOwner && !finalUuid.equals(player.getUniqueId())) {
                            openMemberOptionsGui(player, party, finalUuid, memberName);
                        }
                    }));
        }

        // Onderste balk (overrides rand slots)
        gui.setItem(39, ItemBuilder.from(Material.ARROW)
                .name(StyledComponent.style("<gold>Vorige"))
                .asGuiItem(e -> gui.previous()));
        gui.setItem(41, ItemBuilder.from(Material.ARROW)
                .name(StyledComponent.style("<gold>Volgende"))
                .asGuiItem(e -> gui.next()));

        if (isOwner) {
            gui.setItem(40, ItemBuilder.from(Material.BARRIER)
                    .name(StyledComponent.style("<red>Disband Party"))
                    .lore(StyledComponent.style(""), StyledComponent.style("<gray>Heft de party op."))
                    .asGuiItem(e -> {
                        partyManager.disbandParty(party.getId());
                        player.closeInventory();
                    }));
        } else {
            gui.setItem(40, ItemBuilder.from(Material.RED_DYE)
                    .name(StyledComponent.style("<red>Verlaat Party"))
                    .lore(StyledComponent.style(""), StyledComponent.style("<gray>Verlaat de party."))
                    .asGuiItem(e -> {
                        partyManager.leaveParty(player);
                        player.closeInventory();
                    }));
        }

        gui.open(player);
    }

    // ── Kleur kiezen ─────────────────────────────────────────────────────────────

    private void openColorGui(Player player, Party party) {
        PaginatedGui gui = Gui.paginated()
                .title(Component.text("Team Kleur"))
                .rows(4)
                .pageSize(18)
                .disableAllInteractions()
                .create();

        gui.getFiller().fillBorder(GLASS);

        for (TeamColor color : TeamColor.values()) {
            gui.addItem(ItemBuilder.from(color.getMaterial())
                    .name(StyledComponent.style(color.miniMessage() + color.getDisplayName()))
                    .lore(
                            StyledComponent.style(""),
                            party.getColor() == color
                                    ? StyledComponent.style("<green>✔ Actief")
                                    : StyledComponent.style("<gray>Klik om te selecteren.")
                    )
                    .asGuiItem(e -> {
                        party.setColor(color);
                        for (UUID uuid : party.getMembers()) {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null) updatePartyItem(plugin, p, party);
                        }
                        player.sendMessage(StyledComponent.style(
                                "<green>Team kleur: " + color.miniMessage() + color.getDisplayName()));
                        openManageGui(player, party);
                    }));
        }

        gui.setItem(30, ItemBuilder.from(Material.ARROW).name(StyledComponent.style("<gold>Vorige")).asGuiItem(e -> gui.previous()));
        gui.setItem(32, ItemBuilder.from(Material.ARROW).name(StyledComponent.style("<gold>Volgende")).asGuiItem(e -> gui.next()));
        gui.setItem(31, ItemBuilder.from(Material.BARRIER).name(StyledComponent.style("<red>Terug")).asGuiItem(e -> openManageGui(player, party)));

        gui.open(player);
    }

    // ── Lid opties ───────────────────────────────────────────────────────────────

    private void openMemberOptionsGui(Player player, Party party, UUID targetUuid, String targetName) {
        Gui gui = Gui.gui()
                .title(Component.text(targetName))
                .rows(1)
                .disableAllInteractions()
                .create();

        gui.getFiller().fill(GLASS);

        gui.setItem(2, ItemBuilder.from(Material.ARROW)
                .name(StyledComponent.style("<gray>Terug"))
                .asGuiItem(e -> openManageGui(player, party)));

        gui.setItem(5, ItemBuilder.from(Material.NETHER_STAR)
                .name(StyledComponent.style("<gold>Promoot tot Owner"))
                .lore(StyledComponent.style(""), StyledComponent.style("<gray>Maakt " + targetName + " de nieuwe party owner."))
                .asGuiItem(e -> {
                    partyManager.promoteOwner(party, targetUuid);
                    player.closeInventory();
                }));

        gui.setItem(7, ItemBuilder.from(Material.BARRIER)
                .name(StyledComponent.style("<red>Kick uit Party"))
                .lore(StyledComponent.style(""), StyledComponent.style("<gray>Verwijdert " + targetName + " uit de party."))
                .asGuiItem(e -> {
                    partyManager.kickMember(party, targetUuid);
                    openManageGui(player, party);
                }));

        gui.open(player);
    }

    // ── Item builders ─────────────────────────────────────────────────────────────

    private GuiItem renameItem(boolean isOwner, Party party, Consumer<InventoryClickEvent> action) {
        return ItemBuilder.from(Material.WRITABLE_BOOK)
                .name(StyledComponent.style("<yellow>Party Naam hernoemen"))
                .lore(
                        StyledComponent.style(""),
                        StyledComponent.style("<gray>Huidige naam: <white>" + party.getName()),
                        StyledComponent.style(""),
                        isOwner ? StyledComponent.style("<yellow>Klik om te hernoemen.")
                                : StyledComponent.style("<gray>Alleen de owner kan hernoemen.")
                )
                .asGuiItem(action::accept);
    }

    private GuiItem colorItem(boolean isOwner, Party party, Consumer<InventoryClickEvent> action) {
        return ItemBuilder.from(party.getColor().getMaterial())
                .name(StyledComponent.style("<yellow>Team Kleur"))
                .lore(
                        StyledComponent.style(""),
                        StyledComponent.style("<gray>Huidige kleur: "
                                + party.getColor().miniMessage() + party.getColor().getDisplayName()),
                        StyledComponent.style(""),
                        isOwner ? StyledComponent.style("<yellow>Klik om te wijzigen.")
                                : StyledComponent.style("<gray>Alleen de owner kan dit wijzigen.")
                )
                .asGuiItem(action::accept);
    }

    // ── Hotbar item updaten ───────────────────────────────────────────────────────

    public static void updatePartyItem(Main plugin, Player player, Party party) {
        ItemStack item;
        ItemMeta meta;

        if (party == null) {
            item = new ItemStack(Material.LIME_DYE);
            meta = item.getItemMeta();
            meta.displayName(StyledComponent.style("<green>Maak Party aan"));
            meta.lore(List.of(
                    StyledComponent.style(""),
                    StyledComponent.style("<gray>Klik om een nieuwe party aan te maken.")
            ));
        } else if (party.isOwner(player.getUniqueId())) {
            item = new ItemStack(Material.NETHER_STAR);
            meta = item.getItemMeta();
            meta.displayName(StyledComponent.style("<gold>Beheer Party"));
            meta.lore(List.of(
                    StyledComponent.style(""),
                    StyledComponent.style("<gray>Naam: " + party.getColor().miniMessage() + party.getName()),
                    StyledComponent.style("<gray>Leden: <white>" + party.getMembers().size()),
                    StyledComponent.style(""),
                    StyledComponent.style("<yellow>Klik om je party te beheren.")
            ));
        } else {
            item = new ItemStack(Material.PAPER);
            meta = item.getItemMeta();
            meta.displayName(StyledComponent.style("<yellow>Party"));
            meta.lore(List.of(
                    StyledComponent.style(""),
                    StyledComponent.style("<gray>Naam: " + party.getColor().miniMessage() + party.getName()),
                    StyledComponent.style("<gray>Leden: <white>" + party.getMembers().size()),
                    StyledComponent.style(""),
                    StyledComponent.style("<yellow>Klik om de party te bekijken.")
            ));
        }

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "menu_item"),
                PersistentDataType.STRING,
                "party"
        );
        item.setItemMeta(meta);
        player.getInventory().setItem(4, item);
    }
}
