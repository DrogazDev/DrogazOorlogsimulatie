package nl.drogaz.kingdomOSX.party;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Party {

    private final UUID id;

    @Setter private UUID owner;
    private final List<UUID> members; // volgorde bewaard; owner staat erin
    @Setter private String name;
    @Setter private TeamColor color;

    public Party(UUID owner, String defaultName) {
        this.id = UUID.randomUUID();
        this.owner = owner;
        this.members = new ArrayList<>(List.of(owner));
        this.name = defaultName;
        this.color = TeamColor.WHITE;
    }

    public boolean isOwner(UUID uuid) {
        return owner.equals(uuid);
    }

    public void addMember(UUID uuid) {
        if (!members.contains(uuid)) members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }
}
