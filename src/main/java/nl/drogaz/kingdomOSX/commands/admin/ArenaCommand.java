package nl.drogaz.kingdomOSX.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import nl.drogaz.kingdomOSX.Main;
import nl.drogaz.kingdomOSX.game.Arena;
import nl.drogaz.kingdomOSX.game.GameManager;
import nl.drogaz.kingdomOSX.game.LocationData;
import nl.drogaz.kingdomOSX.miscellaneous.StyledComponent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

@CommandAlias("arena")
@CommandPermission("kingdomosx.admin")
public class ArenaCommand extends BaseCommand {

    private final Main plugin;
    private final GameManager gameManager;

    public ArenaCommand(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    // ── Help ──────────────────────────────────────────────────────────────────────

    @Default
    @HelpCommand
    public void onHelp(Player player) {
        player.sendMessage(StyledComponent.style("<gold>━━━━━━━━━━━ Arena Commando's ━━━━━━━━━━━"));
        player.sendMessage(StyledComponent.style(" <yellow>/arena create <key>      <gray>- Start wizard voor nieuwe arena"));
        player.sendMessage(StyledComponent.style(" <yellow>/arena edit <key>        <gray>- Bewerk bestaande arena"));
        player.sendMessage(StyledComponent.style(" <yellow>/arena wand              <gray>- Geeft de selectiestok"));
        player.sendMessage(StyledComponent.style(" <yellow>/arena setspawn1         <gray>- Stel team 1 spawn in (jouw positie)"));
        player.sendMessage(StyledComponent.style(" <yellow>/arena setspawn2         <gray>- Stel team 2 spawn in (jouw positie)"));
        player.sendMessage(StyledComponent.style(" <yellow>/arena setname <naam>    <gray>- Stel weergavenaam in"));
        player.sendMessage(StyledComponent.style(" <yellow>/arena seticon <mat>     <gray>- Stel GUI-icoon in (bijv. GRASS_BLOCK)"));
        player.sendMessage(StyledComponent.style(" <yellow>/arena status            <gray>- Toon wizard-status"));
        player.sendMessage(StyledComponent.style(" <yellow>/arena save              <gray>- Sla arena op"));
        player.sendMessage(StyledComponent.style(" <yellow>/arena cancel            <gray>- Annuleer wizard"));
        player.sendMessage(StyledComponent.style(" <yellow>/arena list              <gray>- Toon alle arenas"));
        player.sendMessage(StyledComponent.style(" <yellow>/arena info <key>        <gray>- Toon arena-details"));
        player.sendMessage(StyledComponent.style(" <yellow>/arena delete <key>      <gray>- Verwijder arena"));
        player.sendMessage(StyledComponent.style("<gold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    // ── Wizard starten ────────────────────────────────────────────────────────────

    @Subcommand("create")
    @Description("Maak een nieuwe arena aan")
    @Syntax("<key>")
    public void create(Player player, String key) {
        key = key.toLowerCase().replaceAll("[^a-z0-9_]", "_");

        if (gameManager.arenaExists(key)) {
            player.sendMessage(StyledComponent.style(
                    "<red>Arena <white>" + key + " <red>bestaat al. Gebruik <white>/arena edit " + key + "</white> om te bewerken."));
            return;
        }

        Arena arena = gameManager.startWizard(player.getUniqueId(), key);
        giveWand(player);
        sendStatus(player, arena);
        player.sendMessage(StyledComponent.style(
                "<green>Wizard gestart! Gebruik de <white>selectiestok</white> (links/rechts-klik) om pos1/pos2 te selecteren."));
    }

    @Subcommand("edit")
    @Description("Bewerk een bestaande arena")
    @Syntax("<key>")
    @CommandCompletion("@arenas")
    public void edit(Player player, String key) {
        if (!gameManager.arenaExists(key)) {
            player.sendMessage(StyledComponent.style("<red>Arena <white>" + key + " <red>bestaat niet."));
            return;
        }

        Arena arena = gameManager.startWizard(player.getUniqueId(), key);
        giveWand(player);
        sendStatus(player, arena);
        player.sendMessage(StyledComponent.style("<yellow>Wizard gestart voor bestaande arena <white>" + key + "</white>."));
    }

    // ── Wand ─────────────────────────────────────────────────────────────────────

    @Subcommand("wand")
    @Description("Geeft de arena-selectiestok")
    public void wand(Player player) {
        if (!gameManager.hasWizard(player.getUniqueId())) {
            player.sendMessage(StyledComponent.style(
                    "<red>Geen actieve wizard. Gebruik eerst <white>/arena create <naam></white>."));
            return;
        }
        giveWand(player);
    }

    // ── Spawn instellen ───────────────────────────────────────────────────────────

    @Subcommand("setspawn1|s1")
    @Description("Stel team 1 spawn in op jouw huidige positie")
    public void setSpawn1(Player player) {
        Arena wizard = requireWizard(player);
        if (wizard == null) return;

        wizard.setSpawn1(LocationData.from(player.getLocation()));
        player.sendMessage(StyledComponent.style(
                "<gold>[Arena] <green>Spawn 1 ingesteld op: <white>" + LocationData.from(player.getLocation()).toShortString()));
        sendStatus(player, wizard);
    }

    @Subcommand("setspawn2|s2")
    @Description("Stel team 2 spawn in op jouw huidige positie")
    public void setSpawn2(Player player) {
        Arena wizard = requireWizard(player);
        if (wizard == null) return;

        wizard.setSpawn2(LocationData.from(player.getLocation()));
        player.sendMessage(StyledComponent.style(
                "<gold>[Arena] <green>Spawn 2 ingesteld op: <white>" + LocationData.from(player.getLocation()).toShortString()));
        sendStatus(player, wizard);
    }

    // ── Naam & icoon ──────────────────────────────────────────────────────────────

    @Subcommand("setname")
    @Description("Stel de weergavenaam van de arena in")
    @Syntax("<naam>")
    public void setName(Player player, String name) {
        Arena wizard = requireWizard(player);
        if (wizard == null) return;

        wizard.setDisplayName(name);
        player.sendMessage(StyledComponent.style("<gold>[Arena] <green>Naam ingesteld op: <white>" + name));
        sendStatus(player, wizard);
    }

    @Subcommand("seticon")
    @Description("Stel het GUI-icoon in (materiaalnaam)")
    @Syntax("<materiaal>")
    public void setIcon(Player player, String materialName) {
        Arena wizard = requireWizard(player);
        if (wizard == null) return;

        Material mat = Material.matchMaterial(materialName.toUpperCase());
        if (mat == null) {
            player.sendMessage(StyledComponent.style(
                    "<red>Ongeldig materiaal: <white>" + materialName + "<red>. Gebruik bijv. <white>GRASS_BLOCK</white>."));
            return;
        }

        wizard.setIcon(mat);
        player.sendMessage(StyledComponent.style("<gold>[Arena] <green>Icoon ingesteld op: <white>" + mat.name()));
        sendStatus(player, wizard);
    }

    // ── Status, opslaan & annuleren ───────────────────────────────────────────────

    @Subcommand("status")
    @Description("Toon de huidige wizard-status")
    public void status(Player player) {
        Arena wizard = requireWizard(player);
        if (wizard == null) return;
        sendStatus(player, wizard);
    }

    @Subcommand("save")
    @Description("Sla de arena op (vereist dat alle velden ingesteld zijn)")
    public void save(Player player) {
        Arena wizard = requireWizard(player);
        if (wizard == null) return;

        if (!wizard.isComplete()) {
            player.sendMessage(StyledComponent.style("<red>Arena is nog niet compleet. Stel alle velden hieronder in:"));
            sendStatus(player, wizard);
            return;
        }

        wizard.setEnabled(true);
        gameManager.saveArena(wizard);
        gameManager.cancelWizard(player.getUniqueId());
        player.sendMessage(StyledComponent.style(
                "<green>Arena <white>" + wizard.getDisplayName() + "</white> opgeslagen! ✔"));
    }

    @Subcommand("cancel")
    @Description("Annuleer de actieve wizard (wijzigingen worden niet opgeslagen)")
    public void cancel(Player player) {
        if (!gameManager.hasWizard(player.getUniqueId())) {
            player.sendMessage(StyledComponent.style("<red>Geen actieve wizard."));
            return;
        }
        gameManager.cancelWizard(player.getUniqueId());
        player.sendMessage(StyledComponent.style("<yellow>Arena wizard geannuleerd."));
    }

    // ── Overzicht ─────────────────────────────────────────────────────────────────

    @Subcommand("list")
    @Description("Toon alle arenas")
    public void list(Player player) {
        if (gameManager.getAllArenas().isEmpty()) {
            player.sendMessage(StyledComponent.style("<gray>Er zijn nog geen arenas aangemaakt."));
            return;
        }
        player.sendMessage(StyledComponent.style("<gold>━━━━━━━━━━━ Arenas ━━━━━━━━━━━"));
        for (Arena arena : gameManager.getAllArenas()) {
            String status = arena.isEnabled()
                    ? "<green>[Actief]"
                    : (arena.isComplete() ? "<yellow>[Compleet]" : "<red>[Incompleet]");
            player.sendMessage(StyledComponent.style(
                    " <white>" + arena.getKey() + " <gray>→ <white>" + arena.getDisplayName()
                            + " " + status + " <dark_gray>(" + arena.getIcon().name() + ")"));
        }
        player.sendMessage(StyledComponent.style("<gold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    @Subcommand("info")
    @Description("Toon details van een specifieke arena")
    @Syntax("<key>")
    @CommandCompletion("@arenas")
    public void info(Player player, String key) {
        Arena arena = gameManager.getArena(key);
        if (arena == null) {
            player.sendMessage(StyledComponent.style("<red>Arena <white>" + key + " <red>niet gevonden."));
            return;
        }
        sendStatus(player, arena);
    }

    @Subcommand("delete")
    @Description("Verwijder een arena permanent")
    @Syntax("<key>")
    @CommandCompletion("@arenas")
    public void delete(Player player, String key) {
        if (!gameManager.arenaExists(key)) {
            player.sendMessage(StyledComponent.style("<red>Arena <white>" + key + " <red>niet gevonden."));
            return;
        }
        gameManager.deleteArena(key);
        player.sendMessage(StyledComponent.style("<yellow>Arena <white>" + key + " <yellow>verwijderd."));
    }

    // ── Hulpfuncties ──────────────────────────────────────────────────────────────

    private Arena requireWizard(Player player) {
        Arena wizard = gameManager.getWizard(player.getUniqueId());
        if (wizard == null) {
            player.sendMessage(StyledComponent.style(
                    "<red>Geen actieve wizard. Gebruik <white>/arena create <naam></white> of <white>/arena edit <naam></white>."));
        }
        return wizard;
    }

    private void giveWand(Player player) {
        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();
        meta.displayName(StyledComponent.style("<gold>Arena Selectiestok"));
        meta.lore(List.of(
                StyledComponent.style(""),
                StyledComponent.style("<gray>Linker-klik blok  <white>→ Pos1"),
                StyledComponent.style("<gray>Rechter-klik blok <white>→ Pos2")
        ));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "arena_wand"),
                PersistentDataType.BOOLEAN,
                true
        );
        wand.setItemMeta(meta);
        player.getInventory().addItem(wand);
        player.sendMessage(StyledComponent.style("<gold>Selectiestok gegeven. <gray>Links = Pos1 | Rechts = Pos2."));
    }

    /**
     * Toont een overzichtelijk status-scherm van de arena in de wizard.
     */
    private void sendStatus(Player player, Arena arena) {
        player.sendMessage(StyledComponent.style("<gold>━━━━━ Arena Wizard: <white>" + arena.getKey() + " <gold>━━━━━"));

        player.sendMessage(StyledComponent.style(
                " <gray>Naam:    " + field(arena.getDisplayName() != null && !arena.getDisplayName().equals(arena.getKey()),
                        arena.getDisplayName(), "Niet ingesteld")));

        player.sendMessage(StyledComponent.style(
                " <gray>Icoon:   " + field(arena.getIcon() != Material.GRASS_BLOCK || arena.getPos1() != null,
                        arena.getIcon().name(), "GRASS_BLOCK (standaard)")));

        player.sendMessage(StyledComponent.style(
                " <gray>Pos1:    " + locField(arena.getPos1())));

        player.sendMessage(StyledComponent.style(
                " <gray>Pos2:    " + locField(arena.getPos2())));

        player.sendMessage(StyledComponent.style(
                " <gray>Spawn 1: " + locField(arena.getSpawn1())));

        player.sendMessage(StyledComponent.style(
                " <gray>Spawn 2: " + locField(arena.getSpawn2())));

        boolean ready = arena.isComplete();
        player.sendMessage(StyledComponent.style(
                " <gray>Gereed:  " + (ready ? "<green>✔ Gebruik /arena save om op te slaan."
                        : "<red>✘ Stel alle ontbrekende velden in.")));

        player.sendMessage(StyledComponent.style("<gold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private String locField(LocationData loc) {
        return loc != null
                ? "<green>✔ <white>" + loc.toBlockString()
                : "<red>✘ Niet ingesteld";
    }

    private String field(boolean set, String value, String fallback) {
        return set
                ? "<green>✔ <white>" + value
                : "<yellow>○ <white>" + fallback;
    }
}
