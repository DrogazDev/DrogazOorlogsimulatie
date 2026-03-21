package nl.drogaz.kingdomOSX.game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

/**
 * Slaat alle gegevens van een arena op.
 * Pos1/Pos2 vormen de bounding box (bijv. voor spectator-terugplaats).
 * Spawn1/Spawn2 zijn de teamspawns.
 */
@Getter
@Setter
public class Arena {

    private final String key;          // interne identifier (lowercase, geen spaties)
    private String displayName;        // zichtbare naam
    private Material icon;             // GUI-icoon
    private LocationData spawn1;       // team 1 spawn
    private LocationData spawn2;       // team 2 spawn
    private LocationData pos1;         // selectiehoek 1 (wand links-klik)
    private LocationData pos2;         // selectiehoek 2 (wand rechts-klik)
    private boolean enabled;

    public Arena(String key) {
        this.key = key;
        this.displayName = key;
        this.icon = Material.GRASS_BLOCK;
        this.enabled = false;
    }

    /**
     * Arena is compleet als alle vier locaties zijn ingesteld.
     */
    public boolean isComplete() {
        return spawn1 != null && spawn2 != null && pos1 != null && pos2 != null;
    }
}
