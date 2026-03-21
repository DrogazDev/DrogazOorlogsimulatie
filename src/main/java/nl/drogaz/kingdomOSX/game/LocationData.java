package nl.drogaz.kingdomOSX.game;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * Serialiseerbare locatie — sla op in YAML, herstel naar Bukkit Location.
 */
public record LocationData(String world, double x, double y, double z, float yaw, float pitch) {

    public Location toBukkit() {
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    public static LocationData from(Location loc) {
        return new LocationData(
                loc.getWorld().getName(),
                loc.getX(), loc.getY(), loc.getZ(),
                loc.getYaw(), loc.getPitch()
        );
    }

    /** Leesbare string voor status-output. */
    public String toShortString() {
        return String.format("%.1f, %.1f, %.1f (yaw=%.0f) [%s]", x, y, z, (double) yaw, world);
    }

    /** Blok-coördinaten (voor pos1/pos2 display). */
    public String toBlockString() {
        return String.format("%d, %d, %d [%s]", (int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z), world);
    }
}
