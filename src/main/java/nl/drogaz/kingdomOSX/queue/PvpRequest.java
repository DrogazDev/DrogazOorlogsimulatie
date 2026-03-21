package nl.drogaz.kingdomOSX.queue;

import java.util.UUID;

public record PvpRequest(UUID requesterPartyId, UUID targetPartyId, GameMap map) {}
