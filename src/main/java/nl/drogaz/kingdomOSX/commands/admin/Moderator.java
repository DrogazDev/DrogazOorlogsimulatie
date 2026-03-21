package nl.drogaz.kingdomOSX.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.ScrollType;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import dev.triumphteam.gui.guis.ScrollingGui;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import nl.drogaz.kingdomOSX.cosmetics.CosmeticsManager;
import nl.drogaz.kingdomOSX.miscellaneous.StyledComponent;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.List;
import java.util.UUID;

@CommandAlias("m|moderator|mod")
@CommandPermission("kingdomos.moderator")
public class Moderator extends BaseCommand {

    private final CosmeticsManager cosmeticsManager;

    public Moderator(CosmeticsManager cosmeticsManager) {
        this.cosmeticsManager = cosmeticsManager;
    }

    @Default
    public void commandSyntax(Player player) {
        player.sendMessage("/m <option>");
    }

    @Subcommand("cosmetics")
    public void openCosmetics(Player player) {
        Gui cosmetics = Gui.gui()
                .title(Component.text("Cosmetics"))
                .rows(3)
                .disableAllInteractions()
                .create();

        cosmetics.getFiller().fill(ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).name(Component.text(" ")).asGuiItem());
        cosmetics.setItem(12, ItemBuilder.from(Material.DIAMOND_SWORD).name(StyledComponent.style("<gold>Loadout Cosmetics")).asGuiItem(e -> openLoadoutsCosmetics(player)));
        cosmetics.setItem(14, ItemBuilder.from(Material.FEATHER).name(StyledComponent.style("<gold>Effects")).asGuiItem(e -> player.sendMessage("Effecten komen later")));

        cosmetics.open(player);
    }

    public void openLoadoutsCosmetics(Player player) {
        UUID uuid = player.getUniqueId();

        ScrollingGui loadouts = Gui.scrolling()
                .title(Component.text("Loadout Cosmetics"))
                .rows(3)
                .disableAllInteractions()
                .scrollType(ScrollType.HORIZONTAL)
                .create();

        loadouts.getFiller().fillBorder(ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).name(Component.text(" ")).asGuiItem());

        loadouts.addItem(ItemBuilder.from(Material.DIAMOND_SWORD)
                .name(StyledComponent.style("<white>KB Zwaard"))
                .lore(StyledComponent.style(""), StyledComponent.style("<yellow>Huidige skin: <white>" + cosmeticsManager.get(uuid, "kb_sword", "Default")))
                .asGuiItem());

        loadouts.addItem(ItemBuilder.from(Material.DIAMOND_SWORD)
                .name(StyledComponent.style("<white>Non KB Zwaard"))
                .lore(StyledComponent.style(""), StyledComponent.style("<yellow>Huidige skin: <white>" + cosmeticsManager.get(uuid, "non_kb_sword", "Default")))
                .asGuiItem());

        loadouts.addItem(ItemBuilder.from(Material.BOW)
                .name(StyledComponent.style("<white>Punch Boog"))
                .lore(StyledComponent.style(""), StyledComponent.style("<yellow>Huidige skin: <white>" + cosmeticsManager.get(uuid, "punch_bow", "Default")))
                .asGuiItem());

        loadouts.addItem(ItemBuilder.from(Material.BOW)
                .name(StyledComponent.style("<white>Non Punch Boog"))
                .lore(StyledComponent.style(""), StyledComponent.style("<yellow>Huidige skin: <white>" + cosmeticsManager.get(uuid, "non_punch_bow", "Default")))
                .asGuiItem());

        loadouts.addItem(ItemBuilder.from(Material.GOLDEN_APPLE)
                .name(StyledComponent.style("<white>Gouden Appels"))
                .lore(StyledComponent.style(""), StyledComponent.style("<yellow>Huidige skin: <white>" + cosmeticsManager.get(uuid, "golden_apples", "Default")))
                .asGuiItem());

        loadouts.addItem(ItemBuilder.from(Material.DIAMOND_HELMET)
                .name(StyledComponent.style("<white>Diamond Helmet"))
                .lore(StyledComponent.style(""), StyledComponent.style("<yellow>Huidige skin: <white>Default"), StyledComponent.style("<yellow>Huidige Trim: <white>" + cosmeticsManager.get(uuid, "helmet_trim", "Coast")))
                .glow().flags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ATTRIBUTES)
                .asGuiItem(e -> openArmorTrims(player, "helmet_trim")));

        loadouts.addItem(ItemBuilder.from(Material.DIAMOND_CHESTPLATE)
                .name(StyledComponent.style("<white>Diamond Chestplate"))
                .lore(StyledComponent.style(""), StyledComponent.style("<yellow>Huidige skin: <white>Default"), StyledComponent.style("<yellow>Huidige Trim: <white>" + cosmeticsManager.get(uuid, "chestplate_trim", "Coast")))
                .glow().flags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ATTRIBUTES)
                .asGuiItem(e -> openArmorTrims(player, "chestplate_trim")));

        loadouts.addItem(ItemBuilder.from(Material.DIAMOND_LEGGINGS)
                .name(StyledComponent.style("<white>Diamond Leggings"))
                .lore(StyledComponent.style(""), StyledComponent.style("<yellow>Huidige skin: <white>Default"), StyledComponent.style("<yellow>Huidige Trim: <white>" + cosmeticsManager.get(uuid, "leggings_trim", "Coast")))
                .glow().flags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ATTRIBUTES)
                .asGuiItem(e -> openArmorTrims(player, "leggings_trim")));

        loadouts.addItem(ItemBuilder.from(Material.DIAMOND_BOOTS)
                .name(StyledComponent.style("<white>Diamond Boots"))
                .lore(StyledComponent.style(""), StyledComponent.style("<yellow>Huidige skin: <white>Default"), StyledComponent.style("<yellow>Huidige Trim: <white>" + cosmeticsManager.get(uuid, "boots_trim", "Coast")))
                .glow().flags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ATTRIBUTES)
                .asGuiItem(e -> openArmorTrims(player, "boots_trim")));

        loadouts.addItem(ItemBuilder.from(Material.ARROW)
                .name(StyledComponent.style("<white>Arrow Trails"))
                .lore(StyledComponent.style("<yellow>Huidige Trail: <white>" + cosmeticsManager.get(uuid, "arrow_trail", "Flame")))
                .asGuiItem(e -> openArrowTrails(player)));

        loadouts.setItem(22, ItemBuilder.from(Material.BARRIER)
                .name(StyledComponent.style("<red>Terug</red>"))
                .asGuiItem(e -> openCosmetics(player)));

        paginatedHandler(loadouts, 21, 23);

        loadouts.open(player);
    }

    public void openArmorTrims(Player player, String cosmeticKey) {
        PaginatedGui armorTrims = Gui.paginated()
                .title(Component.text("Armor Trims"))
                .rows(6)
                .pageSize(45)
                .disableAllInteractions()
                .create();

        armorTrims.getFiller().fillBottom(ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).name(Component.text(" ")).asGuiItem());
        paginatedHandler(armorTrims, 47, 51);

        RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.TRIM_PATTERN)
                .stream()
                .forEach(trimPattern -> {
                    String patternName = trimPattern.getKey().getKey();
                    armorTrims.addItem(ItemBuilder.from(Material.PAPER)
                            .name(StyledComponent.style("<gray>" + patternName))
                            .asGuiItem(e -> {
                                cosmeticsManager.save(player.getUniqueId(), cosmeticKey, patternName);
                                player.sendMessage(StyledComponent.style("<green>Trim ingesteld op: <white>" + patternName));
                                openLoadoutsCosmetics(player);
                            }));
                });

        armorTrims.open(player);
    }

    public void openArrowTrails(Player player) {
        PaginatedGui arrowTrails = Gui.paginated()
                .title(Component.text("Arrow Trails"))
                .rows(6)
                .pageSize(45)
                .disableAllInteractions()
                .create();

        arrowTrails.getFiller().fillBottom(ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).name(Component.text(" ")).asGuiItem());
        paginatedHandler(arrowTrails, 47, 51);

        List.of(Particle.values()).forEach(particle -> {
            String particleName = particle.name().toLowerCase().replace("_", " ");
            arrowTrails.addItem(ItemBuilder.from(Material.PAPER)
                    .name(StyledComponent.style("<gray>" + particleName))
                    .asGuiItem(e -> {
                        cosmeticsManager.save(player.getUniqueId(), "arrow_trail", particleName);
                        player.sendMessage(StyledComponent.style("<green>Arrow trail ingesteld op: <white>" + particleName));
                        openLoadoutsCosmetics(player);
                    }));
        });

        arrowTrails.open(player);
    }

    public void paginatedHandler(PaginatedGui paginatedGui, int s1, int s2) {
        paginatedGui.setItem(s1, ItemBuilder.from(Material.ARROW).name(StyledComponent.style("<gold>Vorige</gold>")).asGuiItem(e -> paginatedGui.previous()));
        paginatedGui.setItem(s2, ItemBuilder.from(Material.ARROW).name(StyledComponent.style("<gold>Volgende</gold>")).asGuiItem(e -> paginatedGui.next()));
    }
}
