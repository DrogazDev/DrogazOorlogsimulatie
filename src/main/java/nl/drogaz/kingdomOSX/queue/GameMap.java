package nl.drogaz.kingdomOSX.queue;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum GameMap {

    VALLEY ("Vallei",    Material.GRASS_BLOCK,       "Een klassiek slagveld in een vallei."),
    CASTLE ("Kasteel",   Material.STONE_BRICKS,       "Beleg het kasteel en verdedig je stellingen."),
    DESERT ("Woestijn",  Material.SAND,               "Hete arena diep in de woestijn."),
    WINTER ("Winter",    Material.PACKED_ICE,          "Besneeuwd slagveld in het hoge noorden."),
    RUINS  ("Ruïnes",    Material.MOSSY_COBBLESTONE,   "Eeuwenoude ruïnes vol gevaar."),
    OCEAN  ("Oceaan",    Material.BLUE_STAINED_GLASS,  "Eilandenstrijd op volle zee.");

    private final String displayName;
    private final Material material;
    private final String description;

    GameMap(String displayName, Material material, String description) {
        this.displayName = displayName;
        this.material = material;
        this.description = description;
    }
}
