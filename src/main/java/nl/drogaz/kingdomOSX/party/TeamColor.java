package nl.drogaz.kingdomOSX.party;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum TeamColor {

    // Standaard kleuren
    WHITE      ("Wit",          "white",       Material.WHITE_WOOL),
    YELLOW     ("Geel",         "yellow",       Material.YELLOW_WOOL),
    GOLD       ("Goud",         "gold",         Material.ORANGE_WOOL),
    ORANGE     ("Oranje",       "#FF922B",      Material.ORANGE_CONCRETE),
    RED        ("Rood",         "red",          Material.RED_WOOL),
    DARK_RED   ("Donkerrood",   "dark_red",     Material.RED_CONCRETE),
    GREEN      ("Groen",        "green",        Material.LIME_WOOL),
    DARK_GREEN ("Donkergroen",  "dark_green",   Material.GREEN_WOOL),
    AQUA       ("Aqua",         "aqua",         Material.LIGHT_BLUE_WOOL),
    DARK_AQUA  ("Donker Aqua",  "dark_aqua",    Material.CYAN_WOOL),
    BLUE       ("Blauw",        "blue",         Material.BLUE_WOOL),
    DARK_BLUE  ("Donkerblauw",  "dark_blue",    Material.BLUE_CONCRETE),
    LIGHT_PURPLE("Lichtpaars",  "light_purple", Material.PINK_WOOL),
    DARK_PURPLE("Donkerpaars",  "dark_purple",  Material.PURPLE_WOOL),
    GRAY       ("Grijs",        "gray",         Material.LIGHT_GRAY_WOOL),
    DARK_GRAY  ("Donkergrijs",  "dark_gray",    Material.GRAY_WOOL),

    // Hex kleuren
    CORAL      ("Koraal",       "#FF6B6B",      Material.RED_STAINED_GLASS),
    MINT       ("Mint",         "#6BCB77",      Material.LIME_STAINED_GLASS),
    SKY_BLUE   ("Hemelsblauw",  "#4D96FF",      Material.LIGHT_BLUE_STAINED_GLASS),
    VIOLET     ("Violet",       "#CC5DE8",      Material.PURPLE_STAINED_GLASS),
    PINK       ("Roze",         "#FF85A1",      Material.PINK_STAINED_GLASS),
    LIME_HEX   ("Limoen",       "#A9E34B",      Material.LIME_CONCRETE),
    PEACH      ("Perzik",       "#FFB347",      Material.ORANGE_STAINED_GLASS),
    TEAL       ("Teal",         "#20C997",      Material.CYAN_STAINED_GLASS);

    private final String displayName;
    private final String colorTag;
    private final Material material;

    TeamColor(String displayName, String colorTag, Material material) {
        this.displayName = displayName;
        this.colorTag = colorTag;
        this.material = material;
    }

    public String miniMessage() {
        return "<" + colorTag + ">";
    }

    public static TeamColor fromKey(String key) {
        for (TeamColor c : values()) {
            if (c.name().equalsIgnoreCase(key)) return c;
        }
        return WHITE;
    }
}
